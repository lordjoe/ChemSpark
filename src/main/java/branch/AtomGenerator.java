package branch;

import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.silent.*;
import org.openscience.cdk.tools.manipulator.*;
import validate.*;

import java.util.*;


public class AtomGenerator {

    // code for better inderstanding of intermedaite states slewis
    public static final boolean VERBOSE = false;

    public static void showArrayAndIndex(int index, List<Augmentation<IAtomContainer>> augment) {
        System.out.println("==== " + index + " ===================");
        for (Augmentation<IAtomContainer> aa : augment) {
            System.out.println(aa);
        }
        System.out.println("=======================");

    }

    public static void showValueAndIndex(int index, Augmentation<IAtomContainer> aa, boolean handled) {
        if(!handled)
            return;
        String action = handled ? "handled" : "ignored" ;
        System.out.println("====  " + action + "  " + index + " ===================");
        System.out.println(aa);
        System.out.println("=======================");

    }


    private AtomAugmentor augmentor;

    private Handler handler;

    private int maxIndex;

    private int hMax;

    private HCountValidator hCountValidator;

    public AtomGenerator(String elementFormula, Handler handler) {
        List<String> elementSymbols = new ArrayList<String>();
        this.hMax = parseFormula(elementFormula, elementSymbols);
        this.hCountValidator = new HCountValidator();
        hCountValidator.setHCount(hMax);
        hCountValidator.setElementSymbols(elementSymbols);

        this.augmentor = new AtomAugmentor(elementSymbols);
        this.handler = handler;
        this.maxIndex = elementSymbols.size() - 1;
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
        augment(augmentor.getInitial(), 0);
        AtomAugmentation.showTries();
    }

    public void run(IAtomContainer initial) {
        // XXX index = atomCount?
        augment(new AtomAugmentation(initial), initial.getAtomCount() - 1);
    }

    private void augment(Augmentation<IAtomContainer> parent, int index) {
        if (index >= maxIndex) {
            IAtomContainer atomContainer = parent.getAugmentedMolecule();
//            AtomContainerPrinter.print(atomContainer);
            if (hCountValidator.isValidMol(atomContainer, maxIndex + 1)) {
                handler.handle(atomContainer);
                if (VERBOSE)
                    showValueAndIndex(index, parent, true);
            }
            else {
                if (VERBOSE)
                    showValueAndIndex(index, parent, false);
            }
            return;
        }

        List<Augmentation<IAtomContainer>> augment = augmentor.augment(parent);     // refactor slewis
        if (false && VERBOSE)
            showArrayAndIndex(index, augment);

        for (Augmentation<IAtomContainer> augmentation : augment) {
            if (augmentation.isCanonical()) {
                augment(augmentation, index + 1);
            }
        }
    }
}
