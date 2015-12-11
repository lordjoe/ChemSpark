package test.test.generate.symmetry;

import generate.*;
import handler.*;
import io.*;
import org.junit.*;
import org.openscience.cdk.interfaces.*;
import test.test.generate.*;

import java.util.*;

public class DuplicateTests extends BaseTest {
    
    public void testForDuplicates(String formula) {
        DuplicateCountingHandler handler = new DuplicateCountingHandler();
        generateNFromAtom(formula, ListerMethod.SYMMETRIC, LabellerMethod.SIGNATURE, ValidatorMethod.SIGNATURE, handler);
        Map<String, List<IAtomContainer>> dupMap = handler.getDupMap();
        for (String sig : dupMap.keySet()) {
            List<IAtomContainer> dups = dupMap.get(sig);
            for (IAtomContainer ac : dups) {
                AtomContainerPrinter.print(ac);
            }
            System.out.println("----------");
        }
        return; // added slewis
    }
    
    @Test
    public void testC6H4() {
        testForDuplicates("C6H4");
    }
    
    @Test
    public void testC7H8() {
        testForDuplicates("C7H8");
    }
    
    @Test
    public void testC7H2() {
        testForDuplicates("C7H2");
    }

}
