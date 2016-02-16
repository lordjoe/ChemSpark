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
public class SparkAtomAugmentor implements ISparkAugmentor {

    private AtomAugmentor augmentor;

    public AtomAugmentor getAugmentor() {
        return augmentor;
    }

    public void setAugmentor(AtomAugmentor augmentor) {
        this.augmentor = augmentor;
    }

    public SparkAtomAugmentor(final String elementString) {
        augmentor = new AtomAugmentor(elementString);
    }

    public SparkAtomAugmentor(final List<String> elementSymbols) {
        augmentor = new AtomAugmentor(elementSymbols);
    }


    public JavaRDD<AtomAugmentation> sparkAugment(List<AtomAugmentation> augment) {
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
        JavaRDD<AtomAugmentation> ret = currentContext.parallelize(augment);
         return ret;
    }


    @Override
    public List<AtomAugmentation> augment(AtomAugmentation parent) {
        return augmentor.augment(parent);
    }
}
