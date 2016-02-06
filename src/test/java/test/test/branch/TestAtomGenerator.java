package test.test.branch;

import augment.atom.*;
import com.lordjoe.molgen.*;
import handler.*;
import handler.molecule.*;
import io.*;
import org.junit.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.silent.*;

import java.util.*;

import static io.AtomContainerPrinter.*;

public class TestAtomGenerator {

    private IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();

    private void run(String elementFormula, String fromString, Handler handler) {
        SparkAtomGenerator gen = new SparkAtomGenerator(elementFormula, handler);
        gen.run(AtomContainerPrinter.fromString(fromString, builder));
    }

    private void print(String elementFormula) {
        AtomGenerator gen = new AtomGenerator(elementFormula, new PrintStreamHandler(System.out));
        gen.run();
    }

    private void printFrom(String elementFormula, String fromString) {
        run(elementFormula, fromString, new PrintStreamHandler(System.out));
    }

    private int countFrom(String elementFormula, String fromString) {
        CountingHandler handler = new CountingHandler(false);
        SparkAtomGenerator gen = new SparkAtomGenerator(elementFormula, handler);
        gen.run(fromString(fromString, builder));
        return handler.getCount();
    }

    @Test
    public void testFromCCSingle() {
        printFrom("C4H6", "C0C1 0:1(1)");
    }

    @Test
    public void testC4O() {
        print("C4O");
    }


    @Test
    public void testFromCCDouble() {
        printFrom("CCCC", "C0C1 0:1(2)");
    }

    @Test
    public void testToFours() {
        int count  = countFrom("C4H6", "C0C1 0:1(1)");
            count += countFrom("C4H6", "C0C1 0:1(2)");
            count += countFrom("C4H6", "C0C1 0:1(3)");
        System.out.println(count);
    }

    @Test
    public void testC4H8() {
        SparkAtomGenerator gen = new SparkAtomGenerator("C4H8", new PrintStreamHandler(System.out));
        gen.run();
    }

    @Test
    public void testCH5N() {
        SparkAtomGenerator gen = new SparkAtomGenerator("CH5N", new PrintStreamHandler(System.out));
        gen.run();
    }

    @Test
    public void testCH5NFromAtom() {
        new SparkAtomGenerator("CH5N", new PrintStreamHandler(System.out)).run();
    }

    @Test
    public void testC2H2N2FromAtom() {
        new SparkAtomGenerator("C2H2N2", new PrintStreamHandler(System.out)).run();
    }

    private void printDups(DuplicateHandler handler) {
        Map<String, List<IAtomContainer>> map = handler.getDupMap();
        int count = 0;
        for (String key : map.keySet()) {
            List<IAtomContainer> dups = map.get(key);
            if (dups.size() == 1) {
                System.out.println(count + "\t1\t" + AtomContainerPrinter.toString(dups.get(0)));
            } else {
                System.out.println(count + "\t" + dups.size());
            }
            count++;
        }
    }

    @Test
    public void testDups() {
        DuplicateHandler handler = new DuplicateHandler();
        run("C4H6", "C0C1 0:1(1)", handler);
        run("C4H6", "C0C1 0:1(2)", handler);
        run("C4H6", "C0C1 0:1(3)", handler);
        printDups(handler);
    }

}
