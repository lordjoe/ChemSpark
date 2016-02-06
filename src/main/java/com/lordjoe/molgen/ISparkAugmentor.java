package com.lordjoe.molgen;

import augment.*;
import augment.atom.*;
import org.apache.spark.api.java.*;

import java.util.*;

/**
 * com.lordjoe.molgen.SparkAugmentor
 * User: Steve
 * Date: 12/2/2015
 */
public interface ISparkAugmentor extends Augmentor<AtomAugmentation>  {
    public JavaRDD<AtomAugmentation>  sparkAugment(List<AtomAugmentation> augment);

}
