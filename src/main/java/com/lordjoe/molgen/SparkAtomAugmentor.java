package com.lordjoe.molgen;

import branch.*;
import com.lordjoe.distributed.*;
import group.*;
import org.apache.spark.api.java.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.silent.*;

import java.util.*;

/**
 * copy of AtomAugmentor
 */
public class SparkAtomAugmentor implements SparkAugmentor<IAtomContainer> {
    
    /**
     * The elements (in order) used to make molecules for this run.
     */
    private List<String> elementSymbols;
    
    private IChemObjectBuilder abuilder = SilentChemObjectBuilder.getInstance();
    
    private SaturationCalculator saturationCalculator;
    
    public SparkAtomAugmentor(String elementString) {
        elementSymbols = new ArrayList<String>();
        for (int i = 0; i < elementString.length(); i++) {
            elementSymbols.add(String.valueOf(elementString.charAt(i)));
        }
        this.saturationCalculator = new SaturationCalculator(elementSymbols);
    }
    
    public SparkAtomAugmentor(List<String> elementSymbols) {
        this.elementSymbols = elementSymbols;
        this.saturationCalculator = new SaturationCalculator(elementSymbols);
    }

    public IChemObjectBuilder getBuilder() {
        if(abuilder == null)
            abuilder = SilentChemObjectBuilder.getInstance();
        return abuilder;
    }

    /**
     * @return the initial structure
     */
    public Augmentation<IAtomContainer> getInitial() {
        String elementSymbol = elementSymbols.get(0);
        IAtom initialAtom = getBuilder().newInstance(IAtom.class, elementSymbol);
        return new AtomAugmentation(initialAtom);
    }

    @Override
     public List<Augmentation<IAtomContainer>> augment(Augmentation<IAtomContainer> parent) {
         IAtomContainer atomContainer = parent.getAugmentedMolecule();
         List<Augmentation<IAtomContainer>> augmentations = new ArrayList<Augmentation<IAtomContainer>>();
         String elementSymbol = getNextElementSymbol(parent);
        IChemObjectBuilder builder = getBuilder();
        for (int[] bondOrders : getBondOrderArrays(atomContainer)) {
             IAtom atomToAdd = builder.newInstance(IAtom.class, elementSymbol);
             augmentations.add(new AtomAugmentation(atomContainer, atomToAdd, bondOrders));
         }

         return augmentations;
     }

     private String getNextElementSymbol(Augmentation<IAtomContainer> parent) {
         int index = parent.getAugmentedMolecule().getAtomCount();
         if (index < elementSymbols.size()) {
             return elementSymbols.get(index);
         } else {
             return "C"; // XXX TODO...
         }
     }

    // added to get problem size
    public int getBondOrderCount(IAtomContainer atomContainer)
    {
       return   getBondOrderArrays(  atomContainer).size();
    }

    private List<int[]> getBondOrderArrays(IAtomContainer atomContainer) {
         AtomDiscretePartitionRefiner refiner = new AtomDiscretePartitionRefiner();
         PermutationGroup autG = refiner.getAutomorphismGroup(atomContainer);
         int atomCount = atomContainer.getAtomCount();

         // these are the atom indices that can have bonds added
         int[] saturationCapacity = saturationCalculator.getSaturationCapacity(atomContainer);
         List<Integer> baseSet = getUndersaturatedSet(atomCount, saturationCapacity);

         int maxDegreeSumForCurrent = saturationCalculator.getMaxBondOrderSum(atomCount);
         int maxDegreeForCurrent = saturationCalculator.getMaxBondOrder(atomCount);

         List<int[]> representatives = new ArrayList<int[]>();
         for (int[] bondOrderArray : saturationCalculator.getBondOrderArrays(
                 baseSet, atomCount, maxDegreeSumForCurrent, maxDegreeForCurrent, saturationCapacity)) {
             if (isMinimal(bondOrderArray, autG)) {
                 representatives.add(bondOrderArray);
             }
         }
         int[] emptySet = new int[atomCount];
         representatives.add(emptySet);
         return representatives;
     }






     public JavaRDD<Augmentation<IAtomContainer>>  sparkAugment(List<Augmentation<IAtomContainer>> augment) {
            if (AtomGenerator.VERBOSE)
                   AtomGenerator.showArrayAndIndex(0, augment);

         JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
         return currentContext.parallelize(augment);
    }
    

    private boolean isMinimal(int[] bondOrderArray, PermutationGroup autG) {
        String oStr = Arrays.toString(bondOrderArray);
        for (Permutation p : autG.all()) {
//            System.out.println("comparing " + oStr + " and " + p + " of " + Arrays.toString(bondOrderArray));
            String pStr = Arrays.toString(permute(bondOrderArray, p));
            if (oStr.compareTo(pStr) < 0) {
                return false;
            }
        }
        return true;
    }
    
    private int[] permute(int[] a, Permutation p) {
        int[] pA = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            pA[p.get(i)] = a[i];
        }
        return pA;
    }
    
    private List<Integer> getUndersaturatedSet(int atomCount, int[] saturationCapacity) {
        List<Integer> baseSet = new ArrayList<Integer>();
        
        // get the amount each atom is under-saturated
        for (int index = 0; index < atomCount; index++) {
            if (saturationCapacity[index] > 0) {
                baseSet.add(index);
            }
        }
        return baseSet;
    }
    
   

}
