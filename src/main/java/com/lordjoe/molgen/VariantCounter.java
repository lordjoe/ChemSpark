package com.lordjoe.molgen;

//
///**
// * com.lordjoe.molgen.VariantCounter
// * User: Steve
// * Date: 12/2/2015
// */
//public class VariantCounter {
//    public static final int SPARK_CONFIG_INDEX = 0;
//    public static final int FORMULA_INDEX = 1;
//
//
//    /**
//     * call with args like or20080320_s_silac-lh_1-1_11short.mzxml in Sample2
//     *
//     * @param args
//     */
//    public static void main(String[] args) throws Exception {
//
//        Point2d forceClassLoad1 = new Point2d(0,0);
//        Vector2d forceClassLoad2 = new Vector2d(0,0);
//          // force a nasty class to load
//        IChemObjectBuilder forceClassLoad =   SilentChemObjectBuilder.getInstance();
//
//        String formula = args[FORMULA_INDEX];
//
//        SparkAtomAugmentor forceAtomLoad = new  SparkAtomAugmentor(formula);
//
//        Augmentation<IAtomContainer, AtomExtension> initial = forceAtomLoad.getInitial();
//        IAtomContainer augmentedMolecule = initial.getAugmentedMolecule();
//       SparkAtomGenerator generator = new SparkAtomGenerator(formula);
//        int count = generator.getBondOrderCount(augmentedMolecule);
//        SparkUtilities.setDefaultNumberPartitions(count);
//
//        Logger rootLogger = Logger.getRootLogger();
//        rootLogger.setLevel(Level.WARN);
//        ElapsedTimer timer = new ElapsedTimer();
//        ElapsedTimer totalTime = new ElapsedTimer();
//
//        SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);
//
//        Properties sparkProperties = SparkUtilities.getSparkProperties();
//
//
//
//
//        SparkConf sparkConf = new SparkConf();
//  //      sparkConf.set("spark.default.parallelism","20");
//
//
//        Option<String> option = sparkConf.getOption("spark.master");
//        if (!option.isDefined()) {   // use local over nothing
//            sparkConf.setMaster("local[*]");
//        }
//        sparkConf.setAppName("Count instances of " + formula);
//        // use Kryo
//        sparkConf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer");
//        // pass in a registrar class - see below
//        sparkConf.set("spark.kryo.registrator","com.lordjoe.distributed.hydra.HydraKryoSerializer");
//        // force kryo to throw an exception when it tries to serialize
//        // an unregistered class
//        sparkConf.set("spark.kryo.registrationRequired","true");
//
//
////        SparkAccumulatorCountingHandler handler = new SparkAccumulatorCountingHandler(formula);
// //       generator.addHandler(handler);
//
//        generator.run();
//
//        long varientCount = generator.getCount();
//        System.out.println("Found " + varientCount + " Varients of " + formula);
//        timer.showElapsed(formula); ;
//     //   AtomAugmentation.showTries();
//        SparkAccumulators.showAccumulators(timer,false);
//
//
//    }
//
//}
