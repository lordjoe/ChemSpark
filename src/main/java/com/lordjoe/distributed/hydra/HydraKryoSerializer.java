package com.lordjoe.distributed.hydra;

import com.esotericsoftware.kryo.*;
import org.apache.spark.serializer.*;

import javax.annotation.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * com.lordjoe.distributed.hydra.HydraKryoSerializer
 * User: Steve
 * Date: 10/28/2014
 */
public class HydraKryoSerializer implements KryoRegistrator, Serializable {


    public HydraKryoSerializer() {
    }

    /**
     * register a class indicated by name
     *
     * @param kryo
     * @param s       name of a class - might not exist
     * @param handled Set of classes already handles
     */
    protected void doRegistration(@Nonnull Kryo kryo, @Nonnull String s) {
        Class c;
        try {
            c = Class.forName(s);
            doRegistration(kryo, c);
        }
        catch (ClassNotFoundException e) {
            System.err.println("Kryo - class not found " + s);
            return;
        }
    }

    /**
     * register a class
     *
     * @param kryo
     * @param s       name of a class - might not exist
     * @param handled Set of classes already handles
     */
    protected void doRegistration(final Kryo kryo, final Class pC) {
        if (kryo != null) {
            kryo.register(pC);
            // also register arrays of that class
            Class arrayType = Array.newInstance(pC, 0).getClass();
            kryo.register(arrayType);
        }
    }


    /**
     * do the real work of registering all classes
     *
     * @param kryo
     */
    @Override
    public void registerClasses(@Nonnull Kryo kryo) {
        kryo.register(Object[].class);
        kryo.register(scala.Tuple2[].class);
        kryo.register(scala.Tuple3[].class);
        kryo.register(int[].class);
        kryo.register(double[].class);
        kryo.register(short[].class);
        kryo.register(long[].class);
        kryo.register(byte[].class);
        kryo.register(Class[].class);
        kryo.register(long[].class);
        kryo.register(boolean[].class);
        kryo.register(String[].class);
        kryo.register(String[].class);
        Class cls = scala.reflect.ClassTag.class;
        kryo.register(cls);
        //    kryo.register(scala.reflect.ClassTag$$anon$1.class);

//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
        doRegistration(kryo, "java.util.HashMap");
        doRegistration(kryo, "java.util.ArrayList");
        doRegistration(kryo, "org.apache.spark.util.collection.CompactBuffer");
        doRegistration(kryo, "scala.collection.mutable.WrappedArray$ofRef");
        doRegistration(kryo, "java.util.HashMap");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$1");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$IdentityFunction");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$TupleValues");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.SparkFileOpener");
          doRegistration(kryo, "org.slf4j.impl.Log4jLoggerAdapter");
        doRegistration(kryo, "org.systemsbiology.hadoop.DelegatingFileStreamOpener");
        doRegistration(kryo, "org.systemsbiology.hadoop.FileStreamOpener");
        doRegistration(kryo, "org.systemsbiology.hadoop.HadoopMajorVersion");
        doRegistration(kryo, "org.systemsbiology.hadoop.StreamOpeners$ResourceStreamOpener");
        doRegistration(kryo, "org.systemsbiology.xtandem.XTandemMain");

        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$1");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$IdentityFunction");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$TupleValues");
         doRegistration(kryo, "com.lordjoe.distributed.spark.accumulators.SparkAccumulators");
        doRegistration(kryo, "com.lordjoe.utilities.ElapsedTimer");
        doRegistration(kryo, "org.systemsbiology.xtandem.XTandemMain");


        doRegistration(kryo, "com.lordjoe.distributed.spark.accumulators.AccumulatorUtilities");
    }


}


