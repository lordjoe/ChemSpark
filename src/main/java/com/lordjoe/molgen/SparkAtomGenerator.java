package com.lordjoe.molgen;

/**
 * com.lordjoe.molgen.SparkAtomGenerator
 * User: Steve
 * Date: 2/3/2016
 */

import augment.*;
import augment.atom.*;
import augment.constraints.*;
import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.accumulators.*;
import handler.*;
import org.apache.spark.api.java.*;
import org.openscience.cdk.interfaces.*;
import org.systemsbiology.xtandem.*;
import util.molecule.*;
import validate.*;

import java.util.*;


public class SparkAtomGenerator implements AugmentingGenerator<IAtomContainer> {

    private final String elementFormula;

    private SparkAtomAugmentor augmentor;

    private handler.Handler<IAtomContainer> handler;

    private int maxIndex;

    private transient HCountValidator hCountValidatorX;

//    private AtomCanonicalChecker canonicalChecker;

    private ElementConstraints initialConstraints;

    private ElementConstraintSource initialStateSource;

    private int counter;

    public SparkAtomGenerator(String elementFormula, handler.Handler<IAtomContainer> handler) {
        // XXX - parse the formula once and pass down the parser!
        this.elementFormula = elementFormula;
        this.initialConstraints = new ElementConstraints(elementFormula);

        initialStateSource = new ElementConstraintSource(initialConstraints);
        HCountValidator hCountValidator = getHCountValidator();
        this.augmentor = new SparkAtomAugmentor(hCountValidator.getElementSymbols());
        //      this.canonicalChecker = new AtomCanonicalChecker();
        this.handler = handler;
        this.maxIndex = hCountValidator.getElementSymbols().size() - 1;
    }


    public int getCounter() {
        return counter;
    }

    public void setCounter(final int pCounter) {
        counter = pCounter;
    }

    public void run() {
        Iterable<IAtomContainer> iAtomContainers = initialStateSource.get();
        List<IAtomContainer> l = (List<IAtomContainer>) iAtomContainers;
        if (l.size() != 1)
            throw new IllegalStateException("must start with 1 container"); // ToDo change
        IAtomContainer start = l.get(0);
        run(start);
    }

    public void run(IAtomContainer initial) {
        ElementConstraints remaining = initialConstraints;    // TODO
        run(new AtomAugmentation(initial, remaining), 0);
    }

    public static final int MAX_PARITIONS = 3200;     // was 120 - lets try more

    public void run(AtomAugmentation init, int index) {
        List<AtomAugmentation> augment = new ArrayList<AtomAugmentation>();
        for (IAtomContainer start : initialStateSource.get()) {
            String symbol = start.getAtom(0).getSymbol();
            ElementConstraints minus = initialConstraints.minus(symbol);
            AtomAugmentation parent = new AtomAugmentation(start, minus);
            List<AtomAugmentation> oneAugment = augmentor.augment(parent);
            augment.addAll(oneAugment);
        }
        //      List<AtomAugmentation> augment = augmentor.augment(init);
        int numberAtoms = augment.size();
        int numberPartitions = numberAtoms;
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();

        IsCanonical test = new IsCanonical();

        JavaRDD<AtomAugmentation> aug1 = augmentor.sparkAugment(augment);
        for (int i = index + 1; i < maxIndex; i++) {
            if (i < maxIndex - 2) {
                aug1 = aug1.flatMap(new HandleTwoLevelsAugmentation(test));
                numberPartitions = recomputePartitions(numberAtoms, numberPartitions);
                numberPartitions = recomputePartitions(numberAtoms, numberPartitions);
                i++;
            } else {
                aug1 = aug1.flatMap(new HandleOneLevelAugmentation(i, test));
                numberPartitions = recomputePartitions(numberAtoms, numberPartitions);
            }
            if (i < (maxIndex - 1))
                aug1 = aug1.repartition(numberPartitions); // spread the work
        }
        long[] counts = new long[1];
        long theCount = 0;
        aug1 = SparkUtilities.persistAndCount("Before Filter", aug1, counts);
        theCount = counts[0];

        IsMoleculeConnected moleculeConnected = new IsMoleculeConnected(elementFormula, maxIndex);
        aug1 = aug1.filter(moleculeConnected);

        aug1 = SparkUtilities.persistAndCount("After Connected Filter", aug1, counts);
        theCount = counts[0];
        if (false)
            showStructures(aug1, theCount);

        setCounter((int) theCount);
        System.out.println("Count is " + theCount);
    }

    public int recomputePartitions(int numberAtoms, int numberPartitions) {
        if (numberPartitions < MAX_PARITIONS) {
            numberPartitions *= numberAtoms;
        } else {
            numberPartitions = (int) (1.3 * numberPartitions);
        }
        return numberPartitions;
    }

    public void showStructures(JavaRDD<AtomAugmentation> aug1, long theCount) {
        List<AtomAugmentation> collect = aug1.collect();
        int count2 = collect.size();
        if (theCount != count2)
            throw new IllegalStateException("different Answers");

        for (AtomAugmentation atomAugmentation : collect) {
            System.out.println(CDKUtilities.atomAugmentationToString(atomAugmentation));
        }
    }

//
//    private void augment(AtomAugmentation parent, int index) {
//
//        counter++;
//        if (index >= maxIndex) {
//            IAtomContainer atomContainer = parent.getAugmentedObject();
//            if (hCountValidator.isValidMol(atomContainer, maxIndex + 1)) {
//                handler.handle(atomContainer);
////                System.out.println("SOLN " + io.AtomContainerPrinter.toString(atomContainer));
//            }
//            return;
//        }
//
//        List<AtomAugmentation> augment = augmentor.augment(parent);
//        for (AtomAugmentation augmentation : augment) {
//            if (canonicalChecker.isCanonical(augmentation)) {
////                report("C", augmentation);
//                augment(augmentation, index + 1);
//            }
//            else {
////                report("N", augmentation);
//            }
//        }
//    }

    private void report(String cOrN, AtomAugmentation augmentation) {
        System.out.println(counter + " " + cOrN + " "
                + io.AtomContainerPrinter.toString(augmentation.getAugmentedObject()));
    }

    public int getCount() {
        return counter;
    }

    @Override
    public void finish() {
        handler.finish();
    }

    @Override
    public Handler<IAtomContainer> getHandler() {
        return handler;
    }

    public HCountValidator getHCountValidator() {
        if (hCountValidatorX == null) {
            hCountValidatorX = new HCountValidator(elementFormula);
        }
        return hCountValidatorX;
    }

    /**
     * call handler for all valid molecules and return false (no more processing )
     * return true for all other cases
     */
    private static class IsMoleculeConnected extends AbstractLoggingFunction<AtomAugmentation, Boolean> {
        private final String elementFormula;
        private final int maxIndex;
        private transient HCountValidator hCountValidator;

        public IsMoleculeConnected(String elementFormula, int maxIndex) {
            this.elementFormula = elementFormula;
            this.maxIndex = maxIndex;
        }

        @Override
        public Boolean doCall(final AtomAugmentation v1) throws Exception {
            IAtomContainer atomContainer = v1.getAugmentedObject();
            if (hCountValidator == null)
                hCountValidator = new HCountValidator(elementFormula);
            return hCountValidator.isValidMol(atomContainer, maxIndex + 1);
        }


    }

    /**
     * call handler for all valid molecules and return false (no more processing )
     * return true for all other cases
     */
    private class IsMoleculeValid extends AbstractLoggingFunction<AtomAugmentation, Boolean> {


        @Override
        public Boolean doCall(final AtomAugmentation v1) throws Exception {
            IAtomContainer atomContainer = v1.getAugmentedObject();
            return handleValidConnectedMolecule(atomContainer);
        }


    }

    protected Boolean handleValidConnectedMolecule(final IAtomContainer pAtomContainer) {
        HCountValidator hCountValidator = getHCountValidator();
        boolean validMol = hCountValidator.isValidMol(pAtomContainer, maxIndex + 1);
        return validMol;
    }

    /**
     * call handler for all valid molecules and return false (no more processing )
     * return true for all other cases
     */
    private class IsHydrogensCorrect extends AbstractLoggingFunction<AtomAugmentation, Boolean> {


        @Override
        public Boolean doCall(final AtomAugmentation v1) throws Exception {
            IAtomContainer atomContainer = v1.getAugmentedObject();
            return handleHydrogensCorrect(atomContainer);
        }


    }

    protected Boolean handleHydrogensCorrect(final IAtomContainer pAtomContainer) {
        HCountValidator hCountValidator = getHCountValidator();
        boolean validMol = hCountValidator.hydrogensCorrect(pAtomContainer);
        return validMol;
    }

    /**
     * call handler for canonical - I hope to use this to profile time in canonical tests
     */
    private static class IsCanonical extends AbstractLoggingFunction<AtomAugmentation, Boolean> {

        private transient AtomCanonicalChecker canonicalChecker;

        private AtomCanonicalChecker getCannonicalChecker() {
            if (canonicalChecker == null)
                canonicalChecker = new AtomCanonicalChecker();
            return canonicalChecker;
        }

        @Override
        public Boolean doCall(final AtomAugmentation x) throws Exception {
            return getCannonicalChecker().isCanonical(x);
        }


    }


    private class HandleOneLevelAugmentation extends AbstractLoggingFlatMapFunction<AtomAugmentation, AtomAugmentation> {
        public final int index;
        private final IsCanonical canonicalTest;
        //      private transient AtomCanonicalChecker canonicalChecker;

        public HandleOneLevelAugmentation(final int pIndex, IsCanonical canonicalTest) {
            index = pIndex;
            this.canonicalTest = canonicalTest;
        }

//        private AtomCanonicalChecker getCannonicalChecker()
//        {
//           if(canonicalChecker == null)
//               canonicalChecker =  new AtomCanonicalChecker();
//            return canonicalChecker;
//        }

        @Override
        public Iterable<AtomAugmentation> doCall(final AtomAugmentation t) throws Exception {

            HCountValidator hCountValidator = getHCountValidator();
            List<AtomAugmentation> ret = new ArrayList<AtomAugmentation>();
            if (index >= maxIndex) {
                IAtomContainer atomContainer = t.getAugmentedObject();
                boolean validMol = hCountValidator.isValidMol(atomContainer, maxIndex + 1);
                if (validMol) {
                    ret.add(t);
                }
                return ret;
            }

            List<AtomAugmentation> augment = augmentor.augment((AtomAugmentation) t);
//               if (false && AtomGenerator.VERBOSE)
//                    AtomGenerator.showArrayAndIndex(index, augment);

            for (AtomAugmentation x : augment) {
                if (canonicalTest.call(x)) {
                    ret.add(x);
                } else {
                    //  x.isCanonical(); // repeat to check
                    XTandemUtilities.breakHere();
                }
            }
            return ret;
        }
    }


    private class HandleTwoLevelsAugmentation extends AbstractLoggingFlatMapFunction<AtomAugmentation, AtomAugmentation> {
        private final IsCanonical canonicalTest;


        public HandleTwoLevelsAugmentation(IsCanonical test) {
            canonicalTest = test;
        }


        @Override
        public Iterable<AtomAugmentation> doCall(final AtomAugmentation t) throws Exception {

            List<AtomAugmentation> ret = new ArrayList<AtomAugmentation>();

            List<AtomAugmentation> atomAugmentations = augmentOneLevel(t);
            for (AtomAugmentation ax : atomAugmentations) {
                ret.addAll(augmentOneLevel(ax));
            }
            return ret;
        }

        public List<AtomAugmentation> augmentOneLevel(AtomAugmentation t) {
            try {
                List<AtomAugmentation> ret = new ArrayList<>();
                List<AtomAugmentation> augment = augmentor.augment(t);
//               if (false && AtomGenerator.VERBOSE)
//                    AtomGenerator.showArrayAndIndex(index, augment);

                for (AtomAugmentation x : augment) {
                    if (canonicalTest.call(x)) {
                        ret.add(x);
                    } else {
                        //  x.isCanonical(); // repeat to check
                        XTandemUtilities.breakHere();
                    }
                }
                return ret;
            } catch (Exception e) {
                throw new RuntimeException(e);

            }
        }
    }


    protected Boolean handleValidMolecule(final IAtomContainer pAtomContainer) {
        HCountValidator hCountValidator = getHCountValidator();
        boolean validMol = hCountValidator.isValidMol(pAtomContainer, maxIndex + 1);
        return validMol;
    }
}
