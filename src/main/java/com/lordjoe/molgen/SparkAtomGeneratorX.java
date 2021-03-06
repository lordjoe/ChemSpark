package com.lordjoe.molgen;

/**
 * com.lordjoe.molgen.SparkAtomGenerator
 * User: Steve
 * Date: 12/2/2015
 */


///**
// * com.lordjoe.molgen.SparkAtomGenerator
// * similar to AtomGenerator but allows multiple handlers and
// * is immutable  and runs spark code
// * SLewis
// */
//public class SparkAtomGeneratorX implements   AugmentingGenerator<IAtomContainer>,Serializable {
//
//
//    private final SparkAtomAugmentor augmentor;
//
//    private Handler<IAtomContainer> handler;
//
//    private int maxIndex;
//
//    private HCountValidator hCountValidator;
//
//    private AtomCanonicalChecker canonicalChecker;
//
//    private ElementConstraints initialConstraints;
//
//    private ElementConstraintSource initialStateSource;
//
//    private int counter;
//
//    public SparkAtomGeneratorX(String elementFormula, Handler<IAtomContainer> handler) {
//        // XXX - parse the formula once and pass down the parser!
//        this.initialConstraints = new ElementConstraints(elementFormula);
//
//        this.hCountValidator = new HCountValidator(elementFormula);
//        initialStateSource = new ElementConstraintSource(initialConstraints);
//        this.augmentor = new SparkAtomAugmentor(hCountValidator.getElementSymbols());
//        this.canonicalChecker = new AtomCanonicalChecker();
//        this.handler = handler;
//        this.maxIndex = hCountValidator.getElementSymbols().size() - 1;
//    }
//
//    public void run() {
//        for (IAtomContainer start : initialStateSource.get()) {
//            String symbol = start.getAtom(0).getSymbol();
//            augment(new AtomAugmentation(start, initialConstraints.minus(symbol)), 0);
//        }
////        System.out.println("counter = " + counter);
//    }
//
//    public void run(IAtomContainer initial) {
//        // XXX index = atomCount?
//        ElementConstraints remaining = null;    // TODO
//        augment(new AtomAugmentation(initial, remaining), initial.getAtomCount() - 1);
//    }
//
//    private void augment(AtomAugmentation parent, int index) {
//
//        counter++;
//        if (index >= maxIndex) {
//            IAtomContainer atomContainer = parent.getAugmentedObject();
//            if (hCountValidator.isValidMol(atomContainer, maxIndex + 1)) {
//                handler.handle(atomContainer);
////                System.out.println("SOLN " + io.AtomContainerPrinter.toString(atomContainer));
//            }
//            return;
//        }
//
//        for (AtomAugmentation augmentation : augmentor.augment(parent)) {
//            if (canonicalChecker.isCanonical(augmentation)) {
////                report("C", augmentation);
//                augment(augmentation, index + 1);
//            } else {
////                report("N", augmentation);
//            }
//        }
//    }
//
//    private void report(String cOrN, AtomAugmentation augmentation) {
//        System.out.println(counter + " " + cOrN + " "
//            + io.AtomContainerPrinter.toString(augmentation.getAugmentedObject()));
//    }
//
//
//    private int count;
//
//
//    @Override
//    public void finish() {
//        handler.finish();
//    }
//
//    @Override
//    public Handler<IAtomContainer> getHandler() {
//        return handler;
//    }
//
//
//    public SparkAtomGeneratorX(String elementFormula) {
//        List<String> elementSymbols = new ArrayList<String>();
//        this.hMax = parseFormula(elementFormula, elementSymbols);
//        this.hCountValidator = new HCountValidator();
//        hCountValidator.setHCount(hMax);
//        hCountValidator.setElementSymbols(elementSymbols);
//        FormulaParser formulaParser = new FormulaParser(elementFormula);
//
//        this.augmentor = new SparkAtomAugmentor(elementSymbols );
//        this.maxIndex = elementSymbols.size() - 1;
//    }
//
//    public int getCount() {
//        return count;
//    }
//
//
//    public void setCount(final int pCount) {
//        count = pCount;
//    }
//     private int parseFormula(String elementFormula, List<String> elementSymbols) {
//        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
//        IMolecularFormula formula =
//                MolecularFormulaManipulator.getMolecularFormula(elementFormula, builder);
//        List<IElement> elements = MolecularFormulaManipulator.elements(formula);
//
//        // count the number of non-heavy atoms
//        int hCount = 0;
//        for (IElement element : elements) {
//            String symbol = element.getSymbol();
//            int count = MolecularFormulaManipulator.getElementCount(formula, element);
//            if (symbol.equals("H")) {
//                hCount = count;
//            }
//            else {
//                for (int i = 0; i < count; i++) {
//                    elementSymbols.add(symbol);
//                }
//            }
//        }
//        Collections.sort(elementSymbols);
//
//        return hCount;
//    }
//
//    public void run() {
//        Augmentation<IAtomContainer> initial = augmentor.augment()
//        run(initial, 0);
//
//    }
//
//    public static final int MAX_PARITIONS = 800;     // was 120 - lets try more
//
//    public void run(Augmentation<IAtomContainer> init, int index) {
//        List<Augmentation<IAtomContainer>> augment = augmentor.augment(init);
//        int numberAtoms = augment.size();
//        int numberPartitions = numberAtoms;
//        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
//
//        JavaRDD<Augmentation<IAtomContainer>> aug1 = augmentor.sparkAugment(augment);
//        for (int i = index + 1; i < maxIndex; i++) {
//            aug1 = aug1.flatMap(new HandleOneLevelAugmentation(i));
//            if (numberPartitions < MAX_PARITIONS) {
//                numberPartitions *= numberAtoms;
//             }
//            else {
//                 numberPartitions  = (int)(1.3 * numberPartitions);
//            }
//            if(i < (maxIndex - 1))
//                aug1 = aug1.repartition(numberPartitions); // spread the work
//        }
//        long[] counts = new long[1];
//        //  aug1 = SparkUtilities.persistAndCount("Before Filter", aug1, counts);
//
//
//        IsMoleculeConnected moleculeConnected = new IsMoleculeConnected();
//        aug1 = aug1.filter(moleculeConnected);
//        //    aug1 = SparkUtilities.persistAndCount("After Connected Filter", aug1, counts);
//
//        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
//        IAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(builder);
//        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(builder);
//        SaturationChecker satCheck = new SaturationChecker();
//   //     IAtomTypeMatcher matcher = hCountValidator.getMatcher();
//   //     SaturationChecker satCheck = hCountValidator.getSatCheck();
//    //    CDKHydrogenAdder hAdder = hCountValidator.getHAdder();
//
////        IsHydrogensCorrect hydrogensCorrect = new IsHydrogensCorrect();
// //       aug1 = aug1.filter(hydrogensCorrect);
//
//        // we could place a handler here
//        long count = aug1.count();  // force all the work here
// //       aug1 = SparkUtilities.persistAndCount("After Hydrogen Filter", aug1, counts);
//
//        setCount((int)count);
//
////        IsMoleculeValid moleculeValid = new IsMoleculeValid();
////        aug1 = aug1.filter(moleculeValid);
////
////        aug1 = SparkUtilities.persistAndCount("After Molecule Valid", aug1, counts);
//
////        setCount((int) aug1.count()); // now force execution
//        System.out.println("Count is " + getCount());
//    }
//
//    private void augment(Augmentation<IAtomContainer> parent, int index) {
//        if (index >= maxIndex) {
//            IAtomContainer atomContainer = parent.getBase();
////            AtomContainerPrinter.print(atomContainer);
//            if (hCountValidator.isValidMol(atomContainer, maxIndex + 1)) {
//                for (Handler handler : handlers) {
//                    handler.handle(atomContainer);
//                }
//
//            }
//            return;
//        }
//
//        throw new UnsupportedOperationException("Fix This"); // ToDo
////          for (Augmentation<IAtomContainer> augmentation : augment) {
////            if (augmentation.isCanonical()) {
////                augment(augmentation, index + 1);
////            }
////        }
//    }
//
//    private static class SumGoodForms extends AbstractLoggingFunction2<Integer, Augmentation<IAtomContainer>, Integer> {
//        @Override
//        public Integer doCall(final Integer v1, final Augmentation<IAtomContainer> v2) throws Exception {
//            return null;
//        }
//    }
//
//    private class HandleOneLevelAugmentation extends AbstractLoggingFlatMapFunction<Augmentation<IAtomContainer>, Augmentation<IAtomContainer>> {
//        public final int index;
//
//        public HandleOneLevelAugmentation(final int pIndex) {
//            index = pIndex;
//        }
//
//        @Override
//        public Iterable<Augmentation<IAtomContainer>> doCall(final Augmentation<IAtomContainer> t) throws Exception {
//
//            List<Augmentation<IAtomContainer>> ret = new ArrayList<Augmentation<IAtomContainer>>();
//            if (index >= maxIndex) {
//                IAtomContainer atomContainer = t.getBase();
//                boolean validMol = hCountValidator.isValidMol(atomContainer, maxIndex + 1);
//                if (validMol) {
//                    for (Handler handler : handlers) {
//                        handler.handle(atomContainer);
//                    }
//                    if (AtomGenerator.VERBOSE)
//                        AtomGenerator.showValueAndIndex(index, t, true);
//                }
//                else {
//                    if (AtomGenerator.VERBOSE)
//                        AtomGenerator.showValueAndIndex(index, t, false);
//                }
//                return ret;
//            }
//
//            List<Augmentation<IAtomContainer>> augment = augmentor.augment(t);
//            if (false && AtomGenerator.VERBOSE)
//                AtomGenerator.showArrayAndIndex(index, augment);
//
//            for (Augmentation<IAtomContainer> x : augment) {
//                if (true) { //canonicalChecker.isCanonical(augmentation) ) {
//                    ret.add(x);
//                }
//                else {
//                    //  x.isCanonical(); // repeat to check
//                    XTandemUtilities.breakHere();
//                }
//            }
//            return ret;
//        }
//    }
//
//    /**
//     * call handler for all valid molecules and return false (no more processing )
//     * return true for all other cases
//     */
//    private class IsMoleculeConnected extends AbstractLoggingFunction<Augmentation<IAtomContainer >, Boolean> {
//
//
//        @Override
//        public Boolean doCall(final Augmentation<IAtomContainer > v1) throws Exception {
//            IAtomContainer atomContainer = v1.getAugmentedObject();
//            return handleValidConnectedMolecule(atomContainer);
//        }
//
//
//    }
//
//    protected Boolean handleValidConnectedMolecule(final IAtomContainer pAtomContainer) {
//        boolean validMol = hCountValidator.isValidMol(pAtomContainer, maxIndex + 1);
//        return validMol;
//    }
//
////    /**
////     * call handler for all valid molecules and return false (no more processing )
////     * return true for all other cases
////     */
////    private class IsHydrogensCorrect extends AbstractLoggingFunction<Augmentation<IAtomContainer>, Boolean> {
////
////
////        @Override
////        public Boolean doCall(final Augmentation<IAtomContainer> v1) throws Exception {
////            IAtomContainer atomContainer = v1.getBase();
////            return handleHydrogensCorrect(atomContainer);
////        }
////
////
////    }
////
////    protected Boolean handleHydrogensCorrect(final IAtomContainer pAtomContainer) {
////        boolean validMol = hCountValidator.isHydrogensCorrect(pAtomContainer);
////        return validMol;
////    }
////
//
//    /**
//     * call handler for all valid molecules and return false (no more processing )
//     * return true for all other cases
//     */
//    private class IsMoleculeValid extends AbstractLoggingFunction<Augmentation<IAtomContainer >, Boolean> {
//
//
//        @Override
//        public Boolean doCall(final Augmentation<IAtomContainer > v1) throws Exception {
//            IAtomContainer atomContainer = v1.getBase();
//            return handleValidMolecule(atomContainer);
//        }
//
//
//    }
//
//    protected Boolean handleValidMolecule(final IAtomContainer pAtomContainer) {
//        boolean validMol = hCountValidator.isValidMol(pAtomContainer, maxIndex + 1);
//        return validMol;
//    }
//}
