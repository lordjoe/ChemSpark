package com.lordjoe.molgen;

import augment.atom.*;
import com.lordjoe.distributed.*;
import org.apache.spark.api.java.*;

import java.util.*;

/**
 * com.lordjoe.molgen.SparkAtomAugmentor
 * copy of AtomAugmentor augmented to return results as a JavaRDD as well as a list
 * SLewis
 */
public class SparkAtomAugmentor extends AtomAugmentor implements ISparkAugmentor {


    public SparkAtomAugmentor(final String elementString) {
        super(elementString);
    }

    public SparkAtomAugmentor(final List<String> elementSymbols) {
        super(elementSymbols);
    }



     public JavaRDD<AtomAugmentation>  sparkAugment(List<AtomAugmentation>  augment) {
              JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
         return currentContext.parallelize(augment);
    }
    

}
