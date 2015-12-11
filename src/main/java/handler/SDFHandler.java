package handler;

import org.openscience.cdk.exception.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.*;

import java.io.*;

/**
 * Prints the generated molecules to a print stream, defaulting to System out. 
 * 
 * @author maclean
 *
 */
public class SDFHandler implements GenerateHandler {
	
	private SDFWriter writer;
	private final boolean sysOutWriter;  // added slewis to say if writer is System.out
	
	public SDFHandler() {
        writer = new SDFWriter(System.out);
		sysOutWriter = true;    // added slewis
    }
	
	public SDFHandler(String outfile) throws IOException {
	    writer = new SDFWriter(new FileWriter(outfile));
		sysOutWriter = false;     // added slewis
	}

	@Override
	public void handle(IAtomContainer parent, IAtomContainer child) {
	    try {
            writer.write(child);
        } catch (CDKException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	@Override
    public void finish() {
	    try {
			if(!sysOutWriter)  // added slewis - do not cluse System.out
	        	writer.close();
	    } catch (IOException ioe) {
	        // TODO
	    }
    }
}
