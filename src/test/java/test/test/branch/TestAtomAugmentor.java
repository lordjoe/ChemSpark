package test.test.branch;

import augment.*;
import augment.atom.*;
import augment.constraints.*;
import com.lordjoe.molgen.*;
import io.*;
import org.junit.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.signature.*;
import org.openscience.cdk.silent.*;

import java.util.*;

public class TestAtomAugmentor {

    // changed SLewis
       //private static IChemObjectBuilder builderX = SilentChemObjectBuilder.getInstance();

        public static IChemObjectBuilder getBuilder() {
            return  FastChemObjectBuilder.getInstance();   // changed SLewis for control
        }


    private List<AtomAugmentation> gen(String elementString, String startingGraph) {
        SparkAtomAugmentor augmentor = new SparkAtomAugmentor(elementString);
        ElementConstraints elements = new ElementConstraints(elementString);
        AtomAugmentation start = new AtomAugmentation(AtomContainerPrinter.fromString(startingGraph, getBuilder()), elements);
        List<AtomAugmentation> augment = augmentor.augment(start);
        return augment;
    }

    private void print(Iterable<AtomAugmentation> augmentations) {
        int index = 0;
        CanonicalChecker<AtomAugmentation> checker = new AtomCanonicalChecker();
        for (AtomAugmentation augmentation : augmentations) {
//            System.out.print(index + "\t");
//            System.out.print(checker.isCanonical(augmentation) + "\t");
//            AtomContainerPrinter.print(augmentation.getBase());
            index++;
        }
    }

    @Test
    public void testCCSingle() {
        print(gen("CCC", "C0C1 0:1(1)"));
    }

    @Test
    public void testCCDouble() {
        print(gen("CCC", "C0C1 0:1(2)"));
    }

    @Test
    public void testCCTriple() {
        print(gen("CCC", "C0C1 0:1(3)"));
    }

    private void findDups(List<AtomAugmentation> augmentations) {
        Map<String, AtomAugmentation> canonical = new HashMap<String, AtomAugmentation>();
        CanonicalChecker<AtomAugmentation> checker = new AtomCanonicalChecker();
        for (AtomAugmentation augmentation : augmentations) {
            if (checker.isCanonical(augmentation)) {
                IAtomContainer mol = augmentation.getAugmentedObject();
                String sig = new MoleculeSignature(mol).toCanonicalString();
                if (canonical.containsKey(sig)) {
                    System.out.println("dup " + AtomContainerPrinter.toString(mol));
                } else {
                    canonical.put(sig, augmentation);
                }
            }
        }
        print(canonical.values());
    }

    @Test
    public void testThreesFromCCBonds() {
        List<AtomAugmentation> augmentations =
                new ArrayList<AtomAugmentation>();
        augmentations.addAll(gen("CCC", "C0C1 0:1(1)"));
        augmentations.addAll(gen("CCC", "C0C1 0:1(2)"));
        augmentations.addAll(gen("CCC", "C0C1 0:1(3)"));

        findDups(augmentations);
    }

    @Test
    public void testFoursFromCCCLine() {
        findDups(gen("CCCC", "C0C1C2 0:1(1),0:2(1)"));
    }

    @Test
    public void testFoursFromCCCTriangle() {
        findDups(gen("CCCC", "C0C1C2 0:1(1),0:2(1),1:2(1)"));
    }

}
