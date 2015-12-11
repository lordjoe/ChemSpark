package com.lordjoe.distributed;

import com.lordjoe.distributed.test.*;

import static com.lordjoe.distributed.SparkMapReduce.*;

/**
 * com.lordjoe.distributed.SparkWordCount
 * User: Steve
 * Date: 9/12/2014
 */
public class SparkWordCountTest {

    // works but runs too long
    //@Test
    public void testWordCount() {
         WordCountOperator.validateWordCount(FACTORY);
    }


}

