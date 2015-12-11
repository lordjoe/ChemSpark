package branch;

import group.*;
import io.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.interfaces.IBond.*;
import setorbit.*;

import java.util.*;

/**
 * An {@link Augmentation} of an atom container by atom.
 * 
 * @author maclean
 *
 */
public class AtomAugmentation implements Augmentation<IAtomContainer> {
    
    private IAtomContainer augmentedMolecule;
    
    /**
     * An array the size of the parent, of bond orders to add
     */
    private int[] augmentation;
    
    /**
     * Construct the initial state.
     * 
     * @param initialAtom
     */
    public AtomAugmentation(IAtom initialAtom) {
        augmentedMolecule = initialAtom.getBuilder().newInstance(IAtomContainer.class);
        augmentedMolecule.addAtom(initialAtom);
        augmentedMolecule.setProperty("IS_CONNECTED", false);
    }
    
    public AtomAugmentation(IAtomContainer initialContainer) {
        augmentedMolecule = initialContainer;   // TODO : could clone...
    }

    /**
     * added SLewis to add visibility
     * @return
     */
    public String toString() {
       return  AtomContainerPrinter.toString(augmentedMolecule);
    }
    
    /**
     * Make an augmentation from a parent, an atom, and a set of bonds. The augmentation
     * array is a list like {0, 1, 0, 2} which means add a single bond to atom 1 and 
     * a double to atom 3, connecting both to the new atom. 
     *  
     * @param parent the atom container to augment
     * @param atomToAdd the additional atom
     * @param augmentation a list of bond orders to augment
     */
    public AtomAugmentation(IAtomContainer parent, IAtom atomToAdd, int[] augmentation) {
        this.augmentation = augmentation;
        this.augmentedMolecule = make(parent, atomToAdd, augmentation);
    }
    
    public IAtomContainer getAugmentedMolecule() {
        return augmentedMolecule;
    }

    private IAtomContainer make(IAtomContainer parent, IAtom atomToAdd, int[] augmentation) {
        try {
            int lastIndex = augmentation.length;
            IAtomContainer child = (IAtomContainer) parent.clone();
            child.addAtom(atomToAdd);

            for (int index = 0; index < augmentation.length; index++) {
                int value = augmentation[index];
                if (value > 0) {
                    Order order;
                    switch (value) {
                        case 1: order = Order.SINGLE; break;
                        case 2: order = Order.DOUBLE; break;
                        case 3: order = Order.TRIPLE; break;
                        default: order = Order.SINGLE;
                    }
                    child.addBond(index, lastIndex, order);
                    IAtom partner = child.getAtom(index);
                    Integer hCount = partner.getImplicitHydrogenCount();
                    int partnerCount = (hCount == null)? 0 : hCount;
                    partner.setImplicitHydrogenCount(partnerCount - value);
                }
            }
            return child;
        } catch (CloneNotSupportedException cnse) {
            // TODO
            return null;
        }
    }

    @Override
    public boolean isCanonical() {
        if (augmentedMolecule.getAtomCount() <= 2 || augmentedMolecule.getBondCount() == 0) {
            return true;
        }
        AtomDiscretePartitionRefiner refiner = new AtomDiscretePartitionRefiner();
        refiner.getAutomorphismGroup(augmentedMolecule);
        
        BondDiscretePartitionRefiner bondRefiner = new BondDiscretePartitionRefiner();
        try {
            PermutationGroup autBondH = bondRefiner.getAutomorphismGroup(augmentedMolecule);
        
//        Permutation labelling = refiner.getBest().invert();
            Permutation labelling = refiner.getBest();
//        System.out.println("labelling " + labelling);
            List<Integer> connectedBonds = getConnectedBonds(augmentedMolecule, labelling);
            List<Integer> augmentation = getLastAdded();
            return inOrbit(connectedBonds, augmentation, autBondH);
        } catch (Exception e) {
            System.out.println(e + "\t" + AtomContainerPrinter.toString(augmentedMolecule));
            return false;
        }
    }
    
    private List<Integer> getLastAdded() {
        int last = augmentedMolecule.getAtomCount() - 1;
        List<Integer> bondIndices = new ArrayList<Integer>();
        for (int bondIndex = 0; bondIndex < augmentedMolecule.getBondCount(); bondIndex++) {
            IBond bond = augmentedMolecule.getBond(bondIndex);
            IAtom a0 = bond.getAtom(0);
            IAtom a1 = bond.getAtom(1);
            int a0n = augmentedMolecule.getAtomNumber(a0);
            int a1n = augmentedMolecule.getAtomNumber(a1);
            if (a0n == last || a1n == last) {
                bondIndices.add(bondIndex);
            }
        }
        return bondIndices;
    }

    public static int numberSuccess;
    public static int numberFailures;
    public static void showTries() {
        System.out.println("Cannonical " +  numberSuccess + " fail" +  numberFailures + " tries " + (numberSuccess + numberFailures));
       }

    private boolean inOrbit(List<Integer> connectedBonds, List<Integer> augmentation, PermutationGroup autH) {
//        System.out.println("connected bonds " + connectedBonds + " aug " + augmentation);
        if (connectedBonds.size() == 0) {
            int size = augmentation.size();
            boolean ret = size == 0;
            if(ret) {
                numberSuccess++;
                return true;    /// Slewis break out cases
            }
            else {
                numberFailures++;
                return false;
            }
        }
        
        // this should not really be necessary...
        if (autH.getSize() == 0) {
            boolean ret = connectedBonds.equals(augmentation);
            if(ret) {
                numberSuccess++;
                return true;    /// Slewis break out cases
            }
            else {
                numberFailures++;
                return false;
            }
           }
        
        SetOrbit orbit = new BruteForcer().getInOrbit(connectedBonds, autH);
        for (List<Integer> subset : orbit) {
//            System.out.println("subset "  + subset);
            if (subset.equals(augmentation)) {
                numberSuccess++;
                return true;
            }
        }
        numberFailures++;
        return false;
    }
    
    private List<Integer> getConnectedBonds(IAtomContainer h, Permutation labelling) {
        List<Integer> connected = new ArrayList<Integer>();
        int chosen = labelling.get(h.getAtomCount() - 1);
//        System.out.println("chosen " + chosen + " under labelling " + labelling);
        IAtom chosenAtom = h.getAtom(chosen);
        for (int bondIndex = 0; bondIndex < h.getBondCount(); bondIndex++) {
            IBond bond = h.getBond(bondIndex); 
            if (bond.contains(chosenAtom)) {
                connected.add(bondIndex);
            }
        }
//        System.out.println("connected " + Arrays.toString(connected));
        return connected;
    }

}
