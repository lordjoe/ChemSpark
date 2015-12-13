package validate;

import generate.*;
import org.openscience.cdk.atomtype.*;
import org.openscience.cdk.exception.*;
import org.openscience.cdk.graph.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.silent.*;
import org.openscience.cdk.tools.*;
import org.openscience.cdk.tools.manipulator.*;

import java.util.*;

/**
 * Validate a molecule as having the correct number of hydrogens.
 * 
 * XXX it extends base child lister to get access to the BOS stuff - not nice design...
 * 
 * @author maclean
 * Modified SLewis to help serialization
 */
public class HCountValidator extends BaseChildLister implements MoleculeValidator {
    
    private int hCount;
    
    private int maxBOS;

//    private transient Accumulator<Long> failCount;
//    private transient Accumulator<Long> failConnectivity;
//     private transient Accumulator<Long> failHydrogens;

    /**
     * For each position, the maximum number of hydrogens that could be added.
     */
    private int[] maxHAdd;
    
    /**
     * For each position, the maximum number of hydrogens that could be removed.
     */
    private int[] maxHRem;
    
    private transient IAtomTypeMatcher a_matcher; // make lazy Slewis
    private transient SaturationChecker a_satCheck; // SLewis be lazy to help serialization
    private transient CDKHydrogenAdder a_hAdder; // SLewis be lazy to help serialization



    public IAtomTypeMatcher getMatcher() {
        if(a_matcher == null)    {
            IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
            a_matcher = CDKAtomTypeMatcher.getInstance(builder);
         }
        return a_matcher;
    }

    public HCountValidator() {
        hCount = 0;
 //        matcher = new StructGenMatcher();
		a_satCheck = null; //new SaturationChecker();
//        failConnectivity = AccumulatorUtilities.getInstance().createAccumulator("failConnectivity");
//        failHydrogens = AccumulatorUtilities.getInstance().createAccumulator("failHydrogens");
    }

//    public Accumulator<Long> getFailCount() {
//        if(failCount == null) {
//            failCount = AccumulatorUtilities.getAccumulator("failCount");
//        }
//        return failCount;
//    }
//
//    public Accumulator<Long> getFailConnectivity() {
//        if(failConnectivity == null) {
//            failConnectivity = AccumulatorUtilities.getAccumulator("failConnectivity");
//        }
//        return failConnectivity;
//    }
//
//    public Accumulator<Long> getFailHydrogens() {
//        if(failHydrogens == null) {
//               failHydrogens = AccumulatorUtilities.getAccumulator("failHydrogens");
//
//        }
//        return failHydrogens;
//    }
//

    
    public boolean isConnected(IAtomContainer atomContainer) {
        Object connectedProperty = atomContainer.getProperty("IS_CONNECTED");
        if (connectedProperty == null) {
            return true; // assume connected
        } else {
            if ((Boolean) connectedProperty) {
                return true;
            } else {
                boolean connected = ConnectivityChecker.isConnected(atomContainer);
                if (connected) {
                    atomContainer.setProperty("IS_CONNECTED", true);    
                } else {
                    atomContainer.setProperty("IS_CONNECTED", false);
                }
                return connected;
            }
        }
    }
    
    public void checkConnectivity(IAtomContainer atomContainer) {
        isConnected(atomContainer);
    }

    private transient int incorrectCount = 0;
    private transient int incorrectConnect = 0;
    private transient int incorrectHydrogens = 0;

    public boolean isValidMol(IAtomContainer atomContainer, int size) {
        if(!isValidMolConnected(  atomContainer,   size))
            return false;
        return isHydrogensCorrect(atomContainer);
    }

    public boolean isHydrogensCorrect(final IAtomContainer atomContainer) {
        boolean b = hydrogensCorrect(atomContainer);
        if(!b) {
//            getFailHydrogens().add(1L);
//            System.err.println("Hydrogens not correct " + incorrectHydrogens++);
            return false;
        }
        return true;
    }

    public boolean isValidMolConnected(IAtomContainer atomContainer, int size) {
 //        System.out.print("validating " + test.AtomContainerPrinter.toString(atomContainer));
         int atomCount = atomContainer.getAtomCount();


         boolean countCorrect = atomCount == size;
         if(!countCorrect) {
  //           System.err.println("count not correct " + incorrectCount++);
             return false;
         }
          boolean connected = isConnected(atomContainer);
         if(!connected) {
  //           getFailConnectivity().add(1L);
  //           System.err.println("connect not correct " + incorrectConnect++);
             return false;
         }
           return true;
     }


    public CDKHydrogenAdder getHAdder() {
        if(a_hAdder == null) {
            IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
            a_hAdder = CDKHydrogenAdder.getInstance(builder);
        }

        return a_hAdder;
    }



    public SaturationChecker getSatCheck() {
        if(a_satCheck == null) {
             a_satCheck = new SaturationChecker();
        }

        return a_satCheck;
    }


    private boolean omgHCorrect(IAtomContainer acprotonate) {
		try {
            IAtomTypeMatcher matcher = getMatcher();
            for (IAtom atom : acprotonate.atoms()) {
				AtomTypeManipulator.configure(atom, matcher.findMatchingAtomType(acprotonate, atom));
			}
            getHAdder().addImplicitHydrogens(acprotonate);
			if (AtomContainerManipulator.getTotalHydrogenCount(acprotonate) == hCount) {
				return getSatCheck().isSaturated(acprotonate);
			}
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
    }

    private boolean hydrogensCorrect(IAtomContainer atomContainer) {
        try {
            int actualCount = 0;
            IAtomTypeMatcher matcher = getMatcher();
            for (IAtom atom : atomContainer.atoms()) {
                IAtomType atomType = matcher.findMatchingAtomType(atomContainer, atom);
                if (atomType == null || atomType.getAtomTypeName().equals("X")) {
//                	System.out.println(
//                			"+ 0 NULL " 
//                					+ " for atom " + atomContainer.getAtomNumber(atom)
//                					+ " in " +  AtomContainerPrinter.toString(atomContainer)
//                			);
                } else {
                	int count = 
                			atomType.getFormalNeighbourCount() - 
                			atomContainer.getConnectedAtomsCount(atom); 
//                	System.out.println(
//                			"+" + count
//                			+ " " + atomType.getAtomTypeName() 
//                			+ " for atom " + atomContainer.getAtomNumber(atom)
//                			+ " in " +  AtomContainerPrinter.toString(atomContainer)
//                			);
                	actualCount += count;
//                    actualCount += atom.getImplicitHydrogenCount();
                }
                if (actualCount > hCount) {
                    return false;
                }
            }

//            System.out.println("count for " + AtomContainerPrinter.toString(atomContainer) + " = " + actualCount);
            return actualCount == hCount;
        } catch (CDKException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void setHCount(int hCount) {
        this.hCount = hCount;
    }

    @Override
    public boolean canExtend(IAtomContainer atomContainer) {
        int implH = 0;
        for (IAtom atom : atomContainer.atoms()) {
        	Integer hCount = atom.getImplicitHydrogenCount(); 
            implH += (hCount == null)? 0 : hCount;
        }
        int index = atomContainer.getAtomCount() - 1;
        int hAdd = maxHAdd[index];
        int hRem = maxHRem[index];
        int min = implH - hRem;
        int max = implH + hAdd;
        boolean extensible = (min <= hCount && hCount <= max); 
//        String acp = test.AtomContainerPrinter.toString(atomContainer);
//        System.out.println(implH 
//                    + "\t" + hAdd
//                    + "\t" + hRem
//                    + "\t" + min 
//                    + "\t" + max
//                    + "\t" + extensible
//                    + "\t" + acp);
//        return true;
        return extensible;
    }

    @Override
    public void setImplicitHydrogens(IAtomContainer parent) {
        for (IAtom atom : parent.atoms()) {
            int maxBos = super.getMaxBondOrderSum(atom.getSymbol());
            int neighbourCount = parent.getConnectedAtomsCount(atom);
            atom.setImplicitHydrogenCount(maxBos - neighbourCount);
        }
    }

    @Override
    public void setElementSymbols(List<String> elementSymbols) {
        super.setElementSymbols(elementSymbols);
        maxBOS = 0;
        int size = elementSymbols.size();
        int[] bosList = new int[size];
        for (int index = 0; index < elementSymbols.size(); index++) {
            String elementSymbol = elementSymbols.get(index);
            maxBOS += super.getMaxBondOrderSum(elementSymbol);
            bosList[index] = maxBOS;
        }
        
        maxHAdd = new int[size];
        maxHRem = new int[size];
        for (int index = 0; index < elementSymbols.size(); index++) {
            // if all remaining atoms were added with max valence,
            // how many implicit hydrogens would be removed?
            maxHRem[index] = maxBOS - bosList[index]; 
            
            // if the minimum number of connections were made (a tree),
            // how many hydrogens could be added
            maxHAdd[index] = (maxBOS - bosList[index]) - (size - index);
        }
//        System.out.println("Add" + java.util.Arrays.toString(maxHAdd));
//        System.out.println("Rem" + java.util.Arrays.toString(maxHRem));
//        
//        System.out.println("ImplH" + "\t" + "hAdd" + "\t" + "hRem" + "\t"
//                          + "min" + "\t" + "max" + "\t" + "extend?" + "\t" + "acp");
    }
}
