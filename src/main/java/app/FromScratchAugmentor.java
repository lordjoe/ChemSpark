package app;

import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.silent.*;

import java.util.*;

/**
 * Start from a list of element symbols only.
 * 
 * @author maclean
 *
 */
public class FromScratchAugmentor {
    
    private static IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();

    /**
     * @param generator the augmenting generator to use
     * @param heavyAtomCount the count of heavy atoms
     */
    public static void run(AugmentingGenerator generator, int heavyAtomCount) {
        List<String> symbols = generator.getElementSymbols();
        String firstSymbol = symbols.get(0);
        IAtomContainer startingAtom = makeAtomInAtomContainer(firstSymbol, builder);
        generator.extend(startingAtom, heavyAtomCount);
        return; // added slewis to break on finish
    }
    
    private static IAtomContainer makeAtomInAtomContainer(String elementSymbol, IChemObjectBuilder builder) {
        IAtomContainer ac = builder.newInstance(IAtomContainer.class);
        ac.addAtom(builder.newInstance(IAtom.class,(elementSymbol)));
        return ac;
    }

}
