package app;

import handler.*;
import org.apache.commons.cli.*;
import org.openscience.cdk.exception.*;

import java.io.*;

public class AMG {
    
    public static void main(String[] args) throws CDKException, IOException {
        ArgumentHandler argsH = new ArgumentHandler();
        try {
            argsH.processArguments(args);
            run(argsH);
        } catch (ParseException pe) {
            // TODO
        }
    }
    
    public static void run(ArgumentHandler argsH) throws CDKException, IOException {
        try {
            AugmentingGenerator generator = AugmentingGeneratorFactory.build(argsH);
            int heavyAtomCount = generator.getHeavyAtomCount();

            if (argsH.isAugmentingFile() || argsH.isComparingToFile()) {
                String inputFile = argsH.getInputFilepath();
                if (inputFile == null) {
                    error("No input file specified");
                    return;
                } else {
                    if (argsH.isAugmentingFile()) {
                        DataFormat inputFormat = argsH.getInputFormat();
                        if (inputFormat == DataFormat.MOL) {
                            SingleInputAugmentor.run(argsH, inputFile, generator, heavyAtomCount);
                        } else {
                            MutipleInputAugmentor.run(argsH, inputFile, generator, heavyAtomCount);
                        }
                    } else {
                        FromScratchAugmentor.run(generator, heavyAtomCount);
                    }
                }
            } else if (argsH.isStartingFromScratch()) {
                FromScratchAugmentor.run(generator, heavyAtomCount);
            }
            generator.finish();
        }
        catch (IOException e) {
            throw e;

        }
        catch (CDKException e) {
            throw e;

        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
         }
        return; // break here slewis
    }
    
    private static void error(String text) {
        System.out.println(text);
    }
}
