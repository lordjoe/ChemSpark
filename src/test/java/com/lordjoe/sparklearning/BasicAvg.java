package com.lordjoe.sparklearning;

/**
 * com.lordjoe.sparklearning.BasicAvg
 * User: Steve
 * Date: 11/5/2015
 */
/**
 * Illustrates how to compute an average using aggregate in Java
 */

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

import java.io.*;
import java.util.*;

public final class BasicAvg {
  public static class AvgCount implements Serializable {
    public AvgCount(int total, int num) {
	    total_ = total;
	    num_ = num;
    }
    public int total_;
    public int num_;
    public float avg() {
	    return total_ / (float) num_;
    }
  }

  public static void main(String[] args) throws Exception {

    Function2 foo = null;
    String master;
    if (args.length > 0) {
	    master = args[0];
    } else {
	    master = "local";
    }

    String spark_home = System.getenv("SPARK_HOME");
    String jars = System.getenv("JARS");
    JavaSparkContext sc = new JavaSparkContext(
      master, "basicavg", spark_home, jars);
    JavaRDD<Integer> rdd = sc.parallelize(Arrays.asList(1, 2, 3, 4));
    Function2<AvgCount, Integer, AvgCount> addAndCount = new Function2<AvgCount, Integer, AvgCount>() {
	    @Override
	    public AvgCount call(AvgCount a, Integer x) {
        a.total_ += x;
        a.num_ += 1;
        return a;
	    }
    };
    Function2<AvgCount, AvgCount, AvgCount> combine = new Function2<AvgCount, AvgCount, AvgCount>() {
	    @Override
	    public AvgCount call(AvgCount a, AvgCount b) {
        a.total_ += b.total_;
        a.num_ += b.num_;
        return a;
	    }
    };
    AvgCount initial = new AvgCount(0,0);
    AvgCount result = rdd.aggregate(initial, addAndCount, combine);
    System.out.println(result.avg());
    sc.stop();
  }
}

