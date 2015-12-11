package generate;

import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.signature.*;

import java.util.*;


public class BondChildLister extends BaseChildLister {

    private static final IBond.Order[] orders = {
            IBond.Order.SINGLE,
            IBond.Order.DOUBLE,
            IBond.Order.TRIPLE
    };

    // SLewis refactor to look at bad case
    public List<IAtomContainer> listChildren(IAtomContainer parent, int size) {
        try {
            List<IAtomContainer> iAtomContainers = listChildrenInternal(parent, size);
            return iAtomContainers;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // try again to look in detail
            List<IAtomContainer> iAtomContainers = listChildrenInternal(parent, size);
            throw e;

        }
        catch (Exception e) {
              // try again to look in detail
              List<IAtomContainer> iAtomContainers = listChildrenInternal(parent, size);
              throw new RuntimeException(e);

          }
      }

    // SLewis refactor to look at bad case this is the old  listChildren
    private List<IAtomContainer> listChildrenInternal(IAtomContainer parent, int size) {
        int n = parent.getAtomCount();
        List<IAtomContainer> children = new ArrayList<IAtomContainer>();
        List<String> certificates = new ArrayList<String>();
        int[] satCap = getSaturationCapacity(parent);
        boolean bad = false;
        int max = Math.min(n + 1, size);
        for (IBond.Order order : orders) {
            int o = orderToInt(order);
            for (int i = 0; i < n; i++) {
                if (satCap[i] < o) continue;
                if (!orderValid(i, o)) continue;
                if(i >= parent.getAtomCount())
                    bad = true;  // break here we have a problem slewis

                for (int j = i + 1; j < max; j++) {


                    if (j < n && satCap[j] < o)
                        continue;
                    if (!orderValid(j, o))
                        continue;
                    if(j >= parent.getAtomCount())
                          bad = true;  // break here we have a problem slewis

                    IAtom atomI = parent.getAtom(i);
                    IAtom atomJ = parent.getAtom(j);
                    if (parent.getBond(atomI, atomJ) != null) continue;
                    IAtomContainer child = makeChild(parent, i, j, order);
                    MoleculeSignature molSig = new MoleculeSignature(child);
                    String cert = molSig.toCanonicalString();
                    if (!certificates.contains(cert)) {
                        certificates.add(cert);
                        children.add(child);
//                        System.out.println(test.AtomContainerPrinter.toString(child));
                    }
                }
            }
        }
        return children;
    }

    private int orderToInt(IBond.Order order) {
        switch (order) {
            case SINGLE:
                return 1;
            case DOUBLE:
                return 2;
            case TRIPLE:
                return 3;
            default:
                return 0;
        }
    }

    private boolean orderValid(int atomIndex, int order) {
        int maxBondOrder = super.getMaxBondOrder(atomIndex);
        return maxBondOrder >= order;
    }

    public IAtomContainer makeChild(
            IAtomContainer parent, int atomIndexI, int atomIndexJ, IBond.Order order) {
        try {
            IAtomContainer child = (IAtomContainer) parent.clone();
            if (atomIndexJ >= parent.getAtomCount()) {
                String atomSymbol = getElementSymbols().get(atomIndexJ);
                child.addAtom(child.getBuilder().newInstance(IAtom.class, atomSymbol));
            }

            child.addBond(atomIndexI, atomIndexJ, order);
//            System.out.println(java.util.Arrays.toString(bondOrderArr) + "\t" 
//                    + test.AtomContainerPrinter.toString(child));
            return child;
        }
        catch (CloneNotSupportedException cnse) {
            // TODO
            return null;
        }
    }

    public boolean isFinished(IAtomContainer atomContainer, int size) {
        if (atomContainer.getAtomCount() < size) {
            return false;
        }
        else {
            int[] satCap = getSaturationCapacity(atomContainer);
            int freeCount = 0;
            for (int i = 0; i < satCap.length; i++) {
                if (satCap[i] > 0) freeCount++;
            }
            return freeCount < 2;
        }
    }

}
