package com.lordjoe.molgen;

/**
 * com.lordjoe.molgen.SparkAtomGenerator
 * User: Steve
 * Date: 12/2/2015
 */

import branch.*;
import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.accumulators.*;
import org.apache.spark.api.java.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.silent.*;
import org.openscience.cdk.tools.manipulator.*;
import org.systemsbiology.xtandem.*;
import validate.*;

import java.io.*;
import java.util.*;

/**
 * similar to AtomGenerator but allows multiple handlers and
 * is immutable
 */
public class SparkAtomGenerator implements Serializable {


    private final SparkAtomAugmentor augmentor;

    private final List<Handler> handlers = new ArrayList<Handler>();

    private final int maxIndex;

    private final int hMax;

    private int count;


    private HCountValidator hCountValidator;

    public SparkAtomGenerator(String elementFormula, Handler handler) {
        this(elementFormula);
        addHandler(handler);
    }

    public SparkAtomGenerator(String elementFormula) {
        List<String> elementSymbols = new ArrayList<String>();
        this.hMax = parseFormula(elementFormula, elementSymbols);
        this.hCountValidator = new HCountValidator();
        hCountValidator.setHCount(hMax);
        hCountValidator.setElementSymbols(elementSymbols);

        this.augmentor = new SparkAtomAugmentor(elementSymbols);
        this.maxIndex = elementSymbols.size() - 1;
    }

    public int getCount() {
        return count;
    }

    // added to get problem size
    public int getBondOrderCount(IAtomContainer atomContainer) {
        return augmentor.getBondOrderCount(atomContainer);
    }


    public void setCount(final int pCount) {
        count = pCount;
    }

    public void addHandler(Handler handler) {
        handlers.add(handler);

    }

    public List<Handler> getHandlers() {
        return new ArrayList<Handler>(handlers);
    }

    private int parseFormula(String elementFormula, List<String> elementSymbols) {
        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
        IMolecularFormula formula =
                MolecularFormulaManipulator.getMolecularFormula(elementFormula, builder);
        List<IElement> elements = MolecularFormulaManipulator.elements(formula);

        // count the number of non-heavy atoms
        int hCount = 0;
        for (IElement element : elements) {
            String symbol = element.getSymbol();
            int count = MolecularFormulaManipulator.getElementCount(formula, element);
            if (symbol.equals("H")) {
                hCount = count;
            }
            else {
                for (int i = 0; i < count; i++) {
                    elementSymbols.add(symbol);
                }
            }
        }
        Collections.sort(elementSymbols);

        return hCount;
    }

    public void run() {
        Augmentation<IAtomContainer> initial = augmentor.getInitial();
        run(initial, 0);

    }

    public static final int MAX_PARITIONS = 120;

    public void run(Augmentation<IAtomContainer> init, int index) {
        List<Augmentation<IAtomContainer>> augment = augmentor.augment(init);
        int numberAtoms = augment.size();
        int numberPartitions = numberAtoms;
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();

        JavaRDD<Augmentation<IAtomContainer>> aug1 = augmentor.sparkAugment(augment);
        IsMoleculeValid moleculeValid = new IsMoleculeValid();
        for (int i = index + 1; i <  maxIndex; i++) {
            aug1 = aug1.flatMap(new HandleOneLevelAugmentation(i));
            if(numberPartitions < MAX_PARITIONS)   {
                numberPartitions *=  numberAtoms;
                aug1 = aug1.repartition(numberPartitions); // spread the work
             }
        }
        long[] counts = new long[1];
        aug1 = SparkUtilities.persistAndCount("Before Filter", aug1, counts);

        System.out.println("Before Filter" + counts[0]);
        aug1 = aug1.filter(moleculeValid);

        aug1 = SparkUtilities.persistAndCount("After Filter", aug1, counts);

        System.out.println("After Filter" + counts[0]);
        setCount((int) aug1.count()); // now force execution
        System.out.println("Count is " + getCount());
    }

    private void augment(Augmentation<IAtomContainer> parent, int index) {
        if (index >= maxIndex) {
            IAtomContainer atomContainer = parent.getAugmentedMolecule();
//            AtomContainerPrinter.print(atomContainer);
            if (hCountValidator.isValidMol(atomContainer, maxIndex + 1)) {
                for (Handler handler : handlers) {
                    handler.handle(atomContainer);
                }

            }
            return;
        }

        throw new UnsupportedOperationException("Fix This"); // ToDo
//          for (Augmentation<IAtomContainer> augmentation : augment) {
//            if (augmentation.isCanonical()) {
//                augment(augmentation, index + 1);
//            }
//        }
    }

    private static class SumGoodForms extends AbstractLoggingFunction2<Integer, Augmentation<IAtomContainer>, Integer> {
        @Override
        public Integer doCall(final Integer v1, final Augmentation<IAtomContainer> v2) throws Exception {
            return null;
        }
    }

    private class HandleOneLevelAugmentation extends AbstractLoggingFlatMapFunction<Augmentation<IAtomContainer>, Augmentation<IAtomContainer>> {
        public final int index;

        public HandleOneLevelAugmentation(final int pIndex) {
            index = pIndex;
        }

        @Override
        public Iterable<Augmentation<IAtomContainer>> doCall(final Augmentation<IAtomContainer> t) throws Exception {

            List<Augmentation<IAtomContainer>> ret = new ArrayList<Augmentation<IAtomContainer>>();
            if (index >= maxIndex) {
                IAtomContainer atomContainer = t.getAugmentedMolecule();
                boolean validMol = hCountValidator.isValidMol(atomContainer, maxIndex + 1);
                if (validMol) {
                    for (Handler handler : handlers) {
                        handler.handle(atomContainer);
                    }
                    if (AtomGenerator.VERBOSE)
                        AtomGenerator.showValueAndIndex(index, t, true);
                }
                else {
                    if (AtomGenerator.VERBOSE)
                        AtomGenerator.showValueAndIndex(index, t, false);
                }
                return ret;
            }

            List<Augmentation<IAtomContainer>> augment = augmentor.augment(t);
            if (false && AtomGenerator.VERBOSE)
                AtomGenerator.showArrayAndIndex(index, augment);

            for (Augmentation<IAtomContainer> x : augment) {
                if (x.isCanonical()) {
                    ret.add(x);
                }
                else {
                    //  x.isCanonical(); // repeat to check
                    XTandemUtilities.breakHere();
                }
            }
            return ret;
        }
    }

    /**
     * call handler for all valid molecules and return false (no more processing )
     * return true for all other cases
     */
    private class IsMoleculeValid extends AbstractLoggingFunction<Augmentation<IAtomContainer>, Boolean> {


        @Override
        public Boolean doCall(final Augmentation<IAtomContainer> v1) throws Exception {
            IAtomContainer atomContainer = v1.getAugmentedMolecule();
            return handleValidMolecule(atomContainer);
        }


    }

    protected Boolean handleValidMolecule(final IAtomContainer pAtomContainer) {
        boolean validMol = hCountValidator.isValidMol(pAtomContainer, maxIndex + 1);
        return validMol;
    }
}
