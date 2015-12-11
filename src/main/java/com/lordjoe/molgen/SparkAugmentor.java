package com.lordjoe.molgen;

import branch.*;
import org.apache.spark.api.java.*;
import org.openscience.cdk.interfaces.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.molgen.SparkAugmentor
 * User: Steve
 * Date: 12/2/2015
 */
public interface SparkAugmentor<T> extends Augmentor<IAtomContainer>,Serializable {
    public JavaRDD<Augmentation<T>> sparkAugment(List<Augmentation<IAtomContainer>> augment);

}
