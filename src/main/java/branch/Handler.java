package branch;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.*;

public interface Handler extends Serializable {      // slewis added Serializable
    
    public void handle(IAtomContainer atomContainer);
    
    public void finish();

}
