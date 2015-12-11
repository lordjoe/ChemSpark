package handler;


import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.smiles.*;

import java.io.*;
import java.util.*;

/**
 * Check each generated structure against a list of expected structures -
 * used for testing against results from other software.
 * 
 * @author maclean
 *
 */
public class ChecklistHandler implements GenerateHandler {
	
	/**
	 * These are the ones we expect to see
	 */
	private final List<String> expectedList;
	
	/**
	 * These are generated unexpectedly
	 */
	private final List<String> surpriseList;
	
	private final SmilesGenerator smilesGenerator;
	
	private final BitSet checklist;
	
	public ChecklistHandler(String inputFile) throws IOException {
		this.expectedList = new ArrayList<String>();
		// TODO : different input formats?
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line;
		while ((line = reader.readLine()) != null) {
			expectedList.add(line);
		}
		reader.close();
		smilesGenerator = PrintStreamStringHandler.getOnlySmilesGenerator(); //SmilesGenerator.unique();
		checklist = new BitSet(expectedList.size());
		this.surpriseList = new ArrayList<String>();
	}

	@Override
	public void handle(IAtomContainer parent, IAtomContainer child) {
		try {
			String observed = PrintStreamStringHandler.getOnlySmilesGenerator().createSMILES(child); // .create(child);
			int index = expectedList.indexOf(observed);
			if (index == -1) {
				surpriseList.add(observed);
			} else {
				checklist.set(index);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void finish() {
		for (int index = checklist.nextClearBit(0); index >= 0 && index < expectedList.size(); index = checklist.nextClearBit(index + 1)) {
			System.out.println("Missing " + expectedList.get(index));
		}
		for (String surprise : surpriseList) {
			System.out.println("Surprise " + surprise);
		}
	}

}
