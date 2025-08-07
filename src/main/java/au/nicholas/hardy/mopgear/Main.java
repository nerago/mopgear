package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.TopCollectorReporting;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@SuppressWarnings({"CallToPrintStackTrace", "ThrowablePrintedToSystemOut", "SameParameterValue", "unused", "ExtractMethodRecommender"})
public class Main {

    private static final Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    static final Path cacheFile = directory.resolve("cache.json");
    private static final Path gearRetFile = directory.resolve("gear-ret.json");
    private static final Path gearProtFile = directory.resolve("gear-prot.json");
    private static final Path weightFileRetMine = directory.resolve("weight-mysim.json");
    //    private static final Path weightFileStandard = directory.resolve("weight-standard.json");
    private static final Path weightFileProtMine = directory.resolve("weight-prot-sim.json");
    public static final long BILLION = 1000 * 1000 * 1000;

    ItemCache itemCache;

    public static void main(String[] arg) throws IOException, ExecutionException, InterruptedException {
        new Main().run();
    }

    private void run() throws IOException, ExecutionException, InterruptedException {
        itemCache = new ItemCache(cacheFile);

        Instant startTime = Instant.now();

        try (ForkJoinPool myPool = new ForkJoinPool(12, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false, 20, 256, 10, null, 60, TimeUnit.SECONDS)) {
            myPool.submit(() -> exceptionalCheck(startTime)).get();
        }

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void exceptionalCheck(Instant startTime) {
        try {
//            multiSpecSpecifiedRating();
//            multiSpecSequential(startTime);

//        reforgeRet(startTime);
            reforgeProt(startTime);
//        rankSomething();
//        multiSpecReforge(startTime);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    private static ModelCombined standardRetModel() throws IOException {
        StatRatings statRatings = new StatRatingsWeights(weightFileRetMine, false, 1, 1);
        StatRequirements statRequirements = StatRequirements.ret();
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.ret());
    }

    private static ModelCombined standardProtModel() throws IOException {
        StatRatings statRatings = new StatRatingsWeights(weightFileProtMine, false, StatRatingsWeights.PROT_MULTIPLY, 1);
        StatRequirements statRequirements = StatRequirements.prot();
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.prot());
    }

    private ModelCombined nullMixedModel() {
        return new ModelCombined(null, StatRequirements.zero(), ReforgeRules.common());
    }

    private void rankSomething() throws IOException {
        ModelCombined model = standardRetModel();

//        rankAlternatives(new int [] {89530,81239,81567,81180,81568}); // necks
//        rankAlternatives(new int [] {81129,81234,82850,81571}); // cloak
//        rankAlternatives(new int [] {84036,81190,81687,81130,81086}); // belt
        rankAlternativesAsSingleItems(model, new int[]{84027, 81284, 81073, 81113, 82852}); // feet
    }

    private void reforgeRet(Instant startTime) throws IOException {
        ModelCombined model = standardRetModel();

//        reforgeProcess(gearRetFile, model, startTime, true);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);
//        reforgeProcessPlus(gearRetFile, model, startTime, true, 81130, false);
//        reforgeProcessPlus(gearRetFile, model, startTime, 82824, false);
//        reforgeProcessPlusPlus(model, startTime, 81251, 81694);
//        reforgeProcessRetFixed(model, startTime);
        findUpgradeSetup(gearRetFile, model);
    }

    private void reforgeProt(Instant startTime) throws IOException {
        ModelCombined model = standardProtModel();

//        reforgeProcessProtFixed(model, startTime, true);
//        reforgeProcessPlus(gearProtFile, model, startTime, true,90860, false);
//        reforgeProcessPlus2(model, startTime, 81696, 89823);
//        reforgeProcess(gearProtFile, model, startTime, true);
        findUpgradeSetup(gearProtFile, model);

        // so we could get a conclusive result from the ret, then set the common slots to fixed
    }

    private void findUpgradeSetup(Path file, ModelCombined model) throws IOException {
        int[] extraItemArray = valorItemsArray();
//        int[] extraItemArray = msvItemsArray();
//        int[] extraItemArray = pvpCrapArray();
        SlotEquip slot = SlotEquip.Ring2;

//        long runSize = 10000000; // quick runs
        long runSize = 100000000; // 4 min total runs
//        long runSize = 300000000; // 12 min total runs
//        long runSize = 1000000000; // 40 min runs

        ItemSet baseSet = reforgeProcessLight(file, model, runSize, true).get();
        double baseRating = model.calcRating(baseSet);
        System.out.printf("BASE RATING    = %.0f\n", baseRating);

        for (int extraItemId : extraItemArray) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
//            SlotEquip slot = extraItem.slot.toSlotEquip();
            System.out.println(extraItem);

            Optional<ItemSet> extraSet = reforgeProcessPlusCore(file, model, null, false, extraItemId, slot, true, runSize);
            if (extraSet.isPresent()) {
                double extraRating = model.calcRating(extraSet.get());
                double factor = extraRating / baseRating;
                System.out.printf("UPGRADE RATING = %.0f FACTOR = %1.3f\n", extraRating, factor);
            } else {
                System.out.print("UPGRADE SET NOT FOUND\n");
            }
        }
    }

    private int[] pvpCrapArray() {
        return new int[]{
                84807, 84794, 84806, 84810, 84822, 84834, 84851, 84870, 84915, 84949, 84950, 84986, 84891, 84985, 84892,
                84828, 84829, // rings
        };
    }

    private static int[] msvItemsArray() {
        return new int[]{
//                85922,85925,86134, // stone
//                85983,85984,85985, // feng
//                85991,85992,89817, // garaj
//                86075,86076,86080, // kings
                86130, 86140,/*86135,*/ // elegon - starcrusher gauntlets error on ret, only 97% prot
                86144, 86145, 89823  // will
        };
    }

    private static int[] valorItemsArray() {
        int neckParagonPale = 89066; // 1250
        int neckBloodseekers = 89064; // 1250
        int beltKlaxxiConsumer = 89056; // 1750
        int legKovokRiven = 89093; // 2500
        int backYiCloakCourage = 89075; // 1250
        int headYiLeastFavorite = 89216; // 2500
        int headVoiceAmpGreathelm = 89280; // 2500
        int chestDawnblade = 89420; // 2500
        int chestCuirassTwin = 89421; // 2500
        int gloveOverwhelmSwarm = 88746; // 1750
        int wristBattleShadow = 88880; // 1250
        int wristBraidedBlackWhite = 88879; // 1250
        int bootYulonGuardian = 88864; // 1750
        int bootTankissWarstomp = 88862; // 1750

        return new int[]{neckParagonPale, neckBloodseekers, beltKlaxxiConsumer, legKovokRiven, backYiCloakCourage, headYiLeastFavorite, headVoiceAmpGreathelm, chestDawnblade,
                chestCuirassTwin, gloveOverwhelmSwarm, wristBattleShadow, wristBraidedBlackWhite, bootYulonGuardian, bootTankissWarstomp};
    }

    private void rankAlternativesAsSingleItems(ModelCombined model, int[] itemIds) {
        List<ItemData> reforgedItems = Arrays.stream(itemIds)
                .mapToObj(x -> new EquippedItem(x, new int[0], null))
                .map(x -> ItemUtil.loadItem(itemCache, x, true))
                .flatMap(x -> Arrays.stream(Reforger.reforgeItem(model.reforgeRules(), x)))
                .sorted(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())))
                .toList();
        for (ItemData item : reforgedItems) {
            System.out.println(item + " " + model.calcRating(item.totalStatCopy()));
        }
    }

    private void multiSpecSequential(Instant startTime) throws IOException {
        ModelCombined modelNull = nullMixedModel();
        ModelCombined modelRet = standardRetModel();
        ModelCombined modelProt = standardProtModel();

        System.out.println("RET GEAR CURRENT");
        EnumMap<SlotEquip, ItemData[]> retMap = readAndLoad(true, gearRetFile, modelRet.reforgeRules());
        System.out.println("PROT GEAR CURRENT");
        EnumMap<SlotEquip, ItemData[]> protMap = readAndLoad(true, gearProtFile, modelProt.reforgeRules());
        ItemUtil.validateDualSets(retMap, protMap);
        EnumMap<SlotEquip, ItemData[]> commonMap = ItemUtil.commonInDualSet(retMap, protMap);

        Stream<ItemSet> commonStream = EngineStream.runSolverPartial(modelNull, commonMap, startTime, null);

        Stream<ItemSet> protStream = commonStream.map(r -> subSolveBoth(r, retMap, modelRet, protMap, modelProt)).filter(Objects::nonNull);

        Collection<ItemSet> best = protStream.collect(
                new TopCollectorReporting<>(s -> dualRating(s, modelRet, modelProt),
                        s -> reportBetter(s, modelRet, modelProt, retMap, protMap)));
        outputResult(best, modelProt, true);
    }

    private void reportBetter(ItemSet itemSet, ModelCombined modelRet, ModelCombined modelProt, EnumMap<SlotEquip, ItemData[]> itemsRet, EnumMap<SlotEquip, ItemData[]> itemsProt) {
        long rating = modelProt.calcRating(itemSet) + modelRet.calcRating(itemSet.otherSet);
        synchronized (System.out) {
            System.out.println(LocalDateTime.now());
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            itemSet.otherSet.outputSet(modelRet);
            ItemSet tweakRet = Tweaker.tweak(itemSet.otherSet, modelRet, itemsRet);
            if (tweakRet != itemSet.otherSet) {
                System.out.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");
                tweakRet.outputSet(modelRet);
            }
            System.out.println("--------------------------------------- " + rating);
            itemSet.outputSet(modelProt);
            ItemSet tweakProt = Tweaker.tweak(itemSet, modelProt, itemsProt);
            if (tweakProt != itemSet) {
                System.out.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");
                tweakProt.outputSet(modelProt);
            }
            System.out.println("#######################################");

        }
    }

    private void reportBetter(ItemSet itemSet, ModelCombined model) {
        long rating = model.calcRating(itemSet);
        System.out.println(LocalDateTime.now());
        System.out.println("#######################################");
        itemSet.outputSet(model);
    }

    private long dualRating(ItemSet set, ModelCombined modelRet, ModelCombined modelProt) {
        return modelRet.calcRating(set.otherSet) + modelProt.calcRating(set);
    }

    private ItemSet subSolveBoth(ItemSet chosenSet, EnumMap<SlotEquip, ItemData[]> retMap, ModelCombined modelRet, EnumMap<SlotEquip, ItemData[]> protMap, ModelCombined modelProt) {
        EnumMap<SlotEquip, ItemData> chosenMap = chosenSet.items;

//        System.out.println(chosenMap.values().stream().map(ItemData::toString).reduce("", String::concat));

        Optional<ItemSet> retSet = subSolvePart(retMap, modelRet, chosenMap, null);
        if (retSet.isPresent()) {
            Optional<ItemSet> protSet = subSolvePart(protMap, modelProt, chosenMap, retSet.get());
            return protSet.orElse(null);
        }
        return null;
    }

    private static Optional<ItemSet> subSolvePart(EnumMap<SlotEquip, ItemData[]> fullItemMap, ModelCombined model, EnumMap<SlotEquip, ItemData> chosenMap, ItemSet otherSet) {
        EnumMap<SlotEquip, ItemData[]> submitRetMap = fullItemMap.clone();
        ItemUtil.buildJobWithSpecifiedItemsFixed(chosenMap, submitRetMap);
        Stream<ItemSet> retStream = EngineStream.runSolverPartial(model, submitRetMap, null, otherSet);
        return EngineStream.findBest(model, retStream);
    }

    private Stream<? extends ItemSet> subSolveProt(ItemSet retSet, EnumMap<SlotEquip, ItemData[]> protMap, ModelCombined modelProt) {
        EnumMap<SlotEquip, ItemData> retMap = retSet.items;
        EnumMap<SlotEquip, ItemData[]> submitMap = protMap.clone();

        ItemUtil.buildJobWithCommonItemsFixed(retMap, submitMap);

        return EngineStream.runSolverPartial(modelProt, submitMap, null, retSet);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessProtFixed(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> presetReforge = new EnumMap<>(SlotEquip.class);
        presetReforge.put(SlotEquip.Head, Tuple.create(null, null));
        presetReforge.put(SlotEquip.Neck, Tuple.create(StatType.Crit, StatType.Expertise));
        //presetReforge.put(SlotEquip.Shoulder, Tuple.create(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Back, Tuple.create(StatType.Crit, StatType.Expertise));
        presetReforge.put(SlotEquip.Chest, Tuple.create(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Wrist, Tuple.create(StatType.Dodge, StatType.Mastery));
        presetReforge.put(SlotEquip.Ring2, Tuple.create(StatType.Crit, StatType.Mastery));
        presetReforge.put(SlotEquip.Trinket2, Tuple.create(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, Tuple.create(null, null));
//        presetReforge.put(SlotEquip.Offhand, Tuple.create(StatType.Parry, StatType.Hit));

        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);
//        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.standardItemsReforgedToMap(model.getReforgeRules(), items);
        Optional<ItemSet> bestSet = EngineStream.runSolver(model, map, startTime, null);

//        Collection<ItemSet> bestSets = EngineRandom.runSolver(model, map, null);

        outputResult(bestSet, model, detailedOutput);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetFixed(ModelCombined model, Instant startTime) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(gearRetFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, true);

        Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> presetReforge = new EnumMap<>(SlotEquip.class);
//        presetReforge.put(SlotEquip.Head, Tuple.create(null, null));
        presetReforge.put(SlotEquip.Neck, Tuple.create(StatType.Hit, StatType.Expertise));
        //presetReforge.put(SlotEquip.Shoulder, Tuple.create(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Back, Tuple.create(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Chest, Tuple.create(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Wrist, Tuple.create(StatType.Dodge, StatType.Mastery));
        presetReforge.put(SlotEquip.Ring2, Tuple.create(StatType.Crit, StatType.Hit));
//        presetReforge.put(SlotEquip.Trinket2, Tuple.create(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, Tuple.create(null, null));
//        presetReforge.put(SlotEquip.Offhand, Tuple.create(StatType.Parry, StatType.Hit));

        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);
//        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.standardItemsReforgedToMap(model.getReforgeRules(), items);
        Optional<ItemSet> bestSets = EngineStream.runSolver(model, map, startTime, null);

//        Collection<ItemSet> bestSets = EngineRandom.runSolver(model, map, null);

        outputResult(bestSets, model, true);
    }

    private void multiSpecSpecifiedRating() throws IOException {
        ModelCombined modelRet = standardRetModel();
        ModelCombined modelProt = standardProtModel();

        System.out.println("RET GEAR CURRENT");
        List<ItemData> retItems = ItemUtil.loadItems(itemCache, InputParser.readInput(gearRetFile), true);
        System.out.println("PROT GEAR CURRENT");
        List<ItemData> protItems = ItemUtil.loadItems(itemCache, InputParser.readInput(gearProtFile), true);

        Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> reforgeRet = new EnumMap<>(SlotEquip.class);
        reforgeRet.put(SlotEquip.Head, Tuple.create(null, null));
        reforgeRet.put(SlotEquip.Neck, Tuple.create(StatType.Crit, StatType.Expertise));
        reforgeRet.put(SlotEquip.Shoulder, Tuple.create(StatType.Expertise, StatType.Haste));
        reforgeRet.put(SlotEquip.Back, Tuple.create(StatType.Crit, StatType.Expertise));
        reforgeRet.put(SlotEquip.Chest, Tuple.create(StatType.Crit, StatType.Expertise));
        reforgeRet.put(SlotEquip.Wrist, Tuple.create(StatType.Hit, StatType.Haste));
        reforgeRet.put(SlotEquip.Hand, Tuple.create(StatType.Crit, StatType.Hit));
        reforgeRet.put(SlotEquip.Belt, Tuple.create(StatType.Mastery, StatType.Expertise));
        reforgeRet.put(SlotEquip.Leg, Tuple.create(StatType.Crit, StatType.Mastery));
        reforgeRet.put(SlotEquip.Foot, Tuple.create(StatType.Mastery, StatType.Expertise));
        reforgeRet.put(SlotEquip.Ring1, Tuple.create(StatType.Crit, StatType.Haste));
        reforgeRet.put(SlotEquip.Ring2, Tuple.create(StatType.Crit, StatType.Mastery));
        reforgeRet.put(SlotEquip.Trinket1, Tuple.create(null, null));
        reforgeRet.put(SlotEquip.Trinket2, Tuple.create(StatType.Expertise, StatType.Mastery));
        reforgeRet.put(SlotEquip.Weapon, Tuple.create(StatType.Hit, StatType.Haste));

        Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> reforgeProt = new EnumMap<>(SlotEquip.class);
        reforgeProt.put(SlotEquip.Head, Tuple.create(null, null));
        reforgeProt.put(SlotEquip.Neck, Tuple.create(StatType.Crit, StatType.Expertise));
        reforgeProt.put(SlotEquip.Shoulder, Tuple.create(StatType.Dodge, StatType.Hit));
        reforgeProt.put(SlotEquip.Back, Tuple.create(StatType.Crit, StatType.Expertise));
        reforgeProt.put(SlotEquip.Chest, Tuple.create(StatType.Crit, StatType.Expertise));
        reforgeProt.put(SlotEquip.Wrist, Tuple.create(StatType.Dodge, StatType.Mastery));
        reforgeProt.put(SlotEquip.Hand, Tuple.create(StatType.Parry, StatType.Hit));
        reforgeProt.put(SlotEquip.Belt, Tuple.create(StatType.Dodge, StatType.Hit));
        reforgeProt.put(SlotEquip.Leg, Tuple.create(StatType.Dodge, StatType.Mastery));
        reforgeProt.put(SlotEquip.Foot, Tuple.create(StatType.Parry, StatType.Expertise));
        reforgeProt.put(SlotEquip.Ring1, Tuple.create(StatType.Parry, StatType.Expertise));
        reforgeProt.put(SlotEquip.Ring2, Tuple.create(StatType.Crit, StatType.Mastery));
        reforgeProt.put(SlotEquip.Trinket1, Tuple.create(null, null));
        reforgeProt.put(SlotEquip.Trinket2, Tuple.create(StatType.Expertise, StatType.Mastery));
        reforgeProt.put(SlotEquip.Weapon, Tuple.create(null, null));
        reforgeProt.put(SlotEquip.Offhand, Tuple.create(StatType.Parry, StatType.Hit));

        EnumMap<SlotEquip, ItemData> retForgedItems = ItemUtil.chosenItemsReforgedToMap(retItems, reforgeRet);
        ItemSet retSet = ItemSet.manyItems(retForgedItems, null);

        EnumMap<SlotEquip, ItemData> protForgedItems = ItemUtil.chosenItemsReforgedToMap(protItems, reforgeProt);
        ItemSet protSet = ItemSet.manyItems(protForgedItems, null);

        retSet.outputSet(modelRet);
        System.out.println("---------------------" + (modelRet.calcRating(retSet) + modelProt.calcRating(protSet)));
        protSet.outputSet(modelProt);
    }

    private EnumMap<SlotEquip, ItemData[]> readAndLoad(boolean detailedOutput, Path file, ReforgeRules rules) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(file);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);
        return ItemUtil.standardItemsReforgedToMap(rules, items);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcess(Path file, ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad(detailedOutput, file, model.reforgeRules());
        Optional<ItemSet> bestSet = EngineRandom.runSolver(model, reforgedItems, startTime, null, BILLION);
//        ItemSet bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        outputResult(bestSet, model, detailedOutput);
        if (bestSet.isPresent()) {
            ItemSet tweakSet = Tweaker.tweak(bestSet.get(), model, reforgedItems);
            if (bestSet.get() != tweakSet) {
                if (detailedOutput)
                    System.out.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");
                outputResult(Optional.ofNullable(tweakSet), model, detailedOutput);
            }
        }
    }

    private Optional<ItemSet> reforgeProcessLight(Path file, ModelCombined model, long runSize, boolean outputExistingGear) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad(outputExistingGear, file, model.reforgeRules());
        Optional<ItemSet> bestSet = EngineRandom.runSolver(model, reforgedItems, null, null, runSize);
        return bestSet.map(itemSet -> Tweaker.tweak(itemSet, model, reforgedItems));
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(Path file, ModelCombined model, Instant startTime, boolean detailedOutput, int extraItemId, boolean replace) throws IOException {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        Optional<ItemSet> bestSet = reforgeProcessPlusCore(file, model, startTime, detailedOutput, extraItemId, extraItem.slot.toSlotEquip(), replace, BILLION);
        outputResult(bestSet, model, detailedOutput);
    }

    private Optional<ItemSet> reforgeProcessPlusCore(Path file, ModelCombined model, Instant startTime, boolean detailedOutput, int extraItemId, SlotEquip slot, boolean replace, Long runSize) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad(detailedOutput, file, model.reforgeRules());

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        ItemData[] extraForged = Reforger.reforgeItem(model.reforgeRules(), extraItem);
        if (replace) {
            reforgedItems.put(slot, extraForged);
        } else {
            ArrayUtil.map(reforgedItems.get(slot), ItemData::disenchant);
            reforgedItems.put(slot, ArrayUtil.concat(reforgedItems.get(slot), extraForged));
        }

        if (detailedOutput) {
            System.out.println("EXTRA " + extraItem);
        }

        if (runSize != null) {
            Optional<ItemSet> proposed = EngineRandom.runSolver(model, reforgedItems, startTime, null, runSize);
            return proposed.map(itemSet -> Tweaker.tweak(itemSet, model, reforgedItems));
        } else {
            return EngineStream.runSolver(model, reforgedItems, startTime, null);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeAlternatives(Path file, ModelCombined model, Instant startTime, int[] alternateItems) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad(false, file, model.reforgeRules());

        for (int extraItemId : alternateItems) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            Map<SlotEquip, ItemData[]> itemMap = new EnumMap<>(reforgedItems);
            itemMap.put(extraItem.slot.toSlotEquip(), new ItemData[]{extraItem});
            Optional<ItemSet> bestSets = EngineStream.runSolver(model, itemMap, null, null);
            outputResult(bestSets, model, false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlusPlus(Path file, ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2) throws IOException {
        ReforgeRules rules = model.reforgeRules();
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad(false, file, rules);

        ItemData extraItem1 = ItemUtil.loadItemBasic(itemCache, extraItemId1);
        reforgedItems.computeIfPresent(extraItem1.slot.toSlotEquip(), (k, v) -> ArrayUtil.concat(v, Reforger.reforgeItem(rules, extraItem1)));
        System.out.println("EXTRA " + extraItem1);

        ItemData extraItem2 = ItemUtil.loadItemBasic(itemCache, extraItemId2);
        reforgedItems.computeIfPresent(extraItem2.slot.toSlotEquip(), (k, v) -> ArrayUtil.concat(v, Reforger.reforgeItem(rules, extraItem2)));
        System.out.println("EXTRA " + extraItem2);

        Optional<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        outputResult(bestSets, model, true);
    }

    private void outputResult(Collection<ItemSet> bestSets, ModelCombined model, boolean detailedOutput) {
        if (detailedOutput) {
            System.out.println("@@@@@@@@@ Set count " + bestSets.size() + " @@@@@@@@@");
            bestSets.forEach(s -> System.out.println(s.getTotals()));
            bestSets.forEach(s -> {
                System.out.println("#######################################");
                s.outputSet(model);
            });
        } else {
            Optional<ItemSet> last = bestSets.stream().reduce((a, b) -> b);
            last.orElseThrow().outputSet(model);
        }
    }

    private void outputResult(Optional<ItemSet> bestSet, ModelCombined model, boolean detailedOutput) {
        if (bestSet.isPresent()) {
            bestSet.get().outputSet(model);
        } else {
            System.out.println("@@@@@@@@@ NO VALID SET RESULTS @@@@@@@@@");
        }
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
