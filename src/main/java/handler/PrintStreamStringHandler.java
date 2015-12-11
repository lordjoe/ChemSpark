package handler;

import io.*;
import org.openscience.cdk.exception.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.signature.*;
import org.openscience.cdk.smiles.*;

import java.io.*;



/**
 * Prints the generated molecules in a string form to a print 
 * stream, defaulting to System out. 
 * 
 * @author maclean
 *
 */
public class PrintStreamStringHandler implements GenerateHandler {

	private static transient SmilesGenerator onlySmilesGenerator;

	/* 1,4,9 needs this */
	public static SmilesGenerator getOnlySmilesGenerator() {
		if(onlySmilesGenerator == null)
			onlySmilesGenerator = new SmilesGenerator();
		return onlySmilesGenerator;
	}

	private PrintStream printStream;
	
	private SmilesGenerator smilesGenerator;
	
	private int count;
	
	private DataFormat format;
	
	private boolean shouldNumberLines;
	
	private boolean showParent;
	
	public PrintStreamStringHandler() {
		this(System.out, DataFormat.SMILES);
	}
	
	public PrintStreamStringHandler(PrintStream printStream, DataFormat format) {
	    this(printStream, format, false, false);
	}
	
	public PrintStreamStringHandler(PrintStream printStream, 
	                                DataFormat format, 
	                                boolean numberLines,
	                                boolean showParent) {
		this.printStream = printStream;
		this.format = format;
		if (format == DataFormat.SMILES) {
		    smilesGenerator = getOnlySmilesGenerator(); // changed slewis for old versiom SmilesGenerator.unique();
		}
		count = 0;
		this.shouldNumberLines = numberLines;
		this.showParent = showParent;
	}

	@Override
	public void handle(IAtomContainer parent, IAtomContainer child) {
	    String childString = getStringForm(child);
	  
	    if (showParent) {
	        String parentString = getStringForm(parent);
            printStream.println(count + "\t" + parentString + "\t" + childString); 
	    } else {
	        if (shouldNumberLines) { 
                printStream.println(count + "\t" + childString);
            } else {
                printStream.println(childString);
            }
	    }
	    count++;
	}
	
	@Override
    public void finish() {
		// Added SLewis to allow tests to keep running
		PrintStream sysOut = System.out;
		if(printStream == sysOut)
			return; // do not close System.out
		PrintStream sysErr = System.err;
		if(printStream == sysErr)
			return; // do not close System.out

	    printStream.close();
    }

    private String getStringForm(IAtomContainer atomContainer) {
	    if (format == DataFormat.SMILES) {   // added slewis
			try {
				return smilesGenerator.createSMILES(atomContainer); // smilesGenerator.create(atomContainer);
			}
			catch ( Exception e) {
				throw new RuntimeException(e);

			}
		} else if (format == DataFormat.SIGNATURE) {
	        MoleculeSignature childSignature = new MoleculeSignature(atomContainer);
	        return childSignature.toCanonicalString();
	    } else if (format == DataFormat.ACP) {
	        return AtomContainerPrinter.toString(atomContainer);
        } else {
            return "";  // XXX
        }
	}
}
