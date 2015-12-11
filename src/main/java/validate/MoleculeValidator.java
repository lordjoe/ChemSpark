package validate;

import org.openscience.cdk.interfaces.*;

import java.io.*;
import java.util.*;

public interface MoleculeValidator extends Serializable {
    
    public void checkConnectivity(IAtomContainer atomContainer);
    
    public boolean canExtend(IAtomContainer atomContainer);
	
	public boolean isValidMol(IAtomContainer atomContainer, int size);

	public void setHCount(int hCount);
	
    public void setImplicitHydrogens(IAtomContainer parent);

    public void setElementSymbols(List<String> elementSymbols);

}
