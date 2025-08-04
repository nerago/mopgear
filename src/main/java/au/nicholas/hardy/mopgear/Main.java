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

@SuppressWarnings({"CallToPrintStackTrace", "ThrowablePrintedToSystemOut", "SameParameterValue", "unused"})
public class Main {

    private static final Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    static final Path cacheFile = directory.resolve("cache.json");
    private static final Path gearRetFile = directory.resolve("gear-ret.json");
    private static final Path gearProtFile = directory.resolve("gear-prot.json");
    private static final Path weightFileRetMine = directory.resolve("weight-mysim.json");
    private static final Path weightFileStandard = directory.resolve("weight-standard.json");
    private static final Path weightFileProtMine = directory.resolve("weight-prot-sim.json");

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
            multiSpecSequential(startTime);

//        reforgeRet(startTime);
//            reforgeProt(startTime);
//        rankSomething();
//        multiSpecReforge(startTime);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    private void rankSomething() throws IOException {
//        model = new ModelWeights(weightFileMine, true);
        StatRatings statRatings = new StatRatingsWeights(weightFileStandard, true, 1, 1);
        StatRequirements statRequirements = new StatRequirements(false, false);
        ModelCombined model = new ModelCombined(statRatings, statRequirements, ReforgeRules.ret());

//        rankAlternatives(new int [] {89530,81239,81567,81180,81568}); // necks
//        rankAlternatives(new int [] {81129,81234,82850,81571}); // cloak
//        rankAlternatives(new int [] {84036,81190,81687,81130,81086}); // belt
        rankAlternatives(model, new int[]{84027, 81284, 81073, 81113, 82852}); // feet
    }

    private void reforgeRet(Instant startTime) throws IOException {
        StatRatings statRatings = new StatRatingsWeights(weightFileRetMine, false, 1, 1);
        StatRequirements statRequirements = new StatRequirements(false, false);
        ModelCombined model = new ModelCombined(statRatings, statRequirements, ReforgeRules.ret());

//        reforgeProcess(model, startTime, true);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);
//        reforgeProcessPlus(model, startTime, 89345, true); // shoulder
        reforgeProcessPlus(model, startTime, 82824, false);
//        reforgeProcessPlusPlus(model, startTime, 81251, 81694);
//        reforgeProcessRetFixed(model, startTime, true);
    }

    private void reforgeProt(Instant startTime) throws IOException {
        StatRatings statRatings = new StatRatingsWeights(weightFileProtMine, false, 1, 1);
//        StatRatings statRatings = StatRatingsWeights.protHardcode();
//        StatRatings statRatings = new StatRatingsPriority(new StatType[]{StatType.Expertise, StatType.Haste, StatType.Mastery, StatType.Crit});
//        StatRatings statRatings = new StatRatingsPriority(new StatType[]{StatType.Mastery, StatType.Haste, StatType.Parry, StatType.Dodge});
        StatRequirements statRequirements = new StatRequirements(false, true);
        ModelCombined model = new ModelCombined(statRatings, statRequirements, ReforgeRules.prot());

//        reforgeProcessProtFixed(model, startTime, true);
        reforgeProcessProtPlus2(model, startTime, 81696, 89823);

        // so we could get a conclusive result from the ret, then set the common slots to fixed
    }

    private void rankAlternatives(ModelCombined model, int[] itemIds) {
        List<ItemData> reforgedItems = Arrays.stream(itemIds)
                .mapToObj(x -> new EquippedItem(x, new int[0]))
                .map(x -> ItemUtil.loadItem(itemCache, x, true))
                .flatMap(x -> Arrays.stream(Reforger.reforgeItem(model.getReforgeRules(), x)))
                .sorted(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())))
                .toList();
        for (ItemData item : reforgedItems) {
            System.out.println(item + " " + model.calcRating(item.totalStatCopy()));
        }
    }

    private void multiSpecSequential(Instant startTime) throws IOException {
        StatRatings statRatingsRet = new StatRatingsWeights(weightFileRetMine, false, 1, 1);
        StatRequirements statRet = new StatRequirements(false, false);
        ModelCombined modelRet = new ModelCombined(statRatingsRet, statRet, ReforgeRules.ret());

        StatRatings protStatRatings = new StatRatingsWeights(weightFileProtMine, false, StatRatingsWeights.PROT_MULT, 1);
        StatRequirements statProt = new StatRequirements(false, true);
//        StatRatings protStatRatings = StatRatingsWeights.protHardcode();
        ModelCombined modelProt = new ModelCombined(protStatRatings, statProt, ReforgeRules.prot());

        System.out.println("RET GEAR CURRENT");
        EnumMap<SlotEquip, ItemData[]> retMap = readAndLoad2(true, gearRetFile, modelRet.getReforgeRules());
        System.out.println("PROT GEAR CURRENT");
        EnumMap<SlotEquip, ItemData[]> protMap = readAndLoad2(true, gearProtFile, modelProt.getReforgeRules());
        validateDualSets(retMap, protMap);

        long randomCount = 1000L * 1000L * 1000L;
        Stream<ItemSet> retStream = EngineRandom.runSolverPartial(modelRet, retMap, startTime, null, randomCount);

//        Stream<ItemSet> protStream = retStream.flatMap(r -> subSolveProt(r, protMap, modelProt));
        Stream<ItemSet> protStream = retStream.map(r -> subSolveBoth(r, retMap, modelRet, protMap, modelProt));

//        protStream = protStream.peek(x -> System.out.println(x.getTotals() + " " + x.otherSet.getTotals()));

        Collection<ItemSet> best = protStream.collect(
                new TopCollectorReporting<>(s -> dualRating(s, modelRet, modelProt),
                        s -> reportBetter(s, modelRet, modelProt)));
        outputResult(best, modelProt, true);
    }

    private void validateDualSets(Map<SlotEquip, ItemData[]> retMap, Map<SlotEquip, ItemData[]> protMap) {
        if (protMap.get(SlotEquip.Offhand) == null || protMap.get(SlotEquip.Offhand).length == 0)
            throw new IllegalArgumentException("no shield");
        if (protMap.get(SlotEquip.Ring1)[0].id == retMap.get(SlotEquip.Ring2)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Ring2)[0].id == retMap.get(SlotEquip.Ring1)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Trinket1)[0].id == retMap.get(SlotEquip.Trinket2)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Trinket2)[0].id == retMap.get(SlotEquip.Trinket1)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
    }

    private void reportBetter(ItemSet itemSet, ModelCombined modelRet, ModelCombined modelProt) {
        long rating = modelProt.calcRating(itemSet) + modelRet.calcRating(itemSet.otherSet);
        synchronized (System.out) {
            System.out.println(LocalDateTime.now());
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            itemSet.otherSet.outputSet(modelRet);
            System.out.println("--------------------------------------- " + rating);
            itemSet.outputSet(modelProt);
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

        EnumMap<SlotEquip, ItemData[]> submitRetMap = retMap.clone();
        ItemUtil.buildJobWithCommonItemsFixed(chosenMap, submitRetMap);
        ItemSet optimisedRet = EngineStream.runSolver(modelRet, submitRetMap, null, null);

        EnumMap<SlotEquip, ItemData[]> submitProtMap = protMap.clone();
        ItemUtil.buildJobWithCommonItemsFixed(chosenMap, submitProtMap);
        return EngineStream.runSolver(modelRet, submitRetMap, null, optimisedRet);
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

        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.limitedItemsReforgedToMap(model.getReforgeRules(), items, presetReforge);
//        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.standardItemsReforgedToMap(model.getReforgeRules(), items);
        ItemSet bestSet = EngineStream.runSolver(model, map, startTime, null);

//        Collection<ItemSet> bestSets = EngineRandom.runSolver(model, map, null);

        outputResult(bestSet, model, detailedOutput);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetFixed(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(gearRetFile);
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

        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.limitedItemsReforgedToMap(model.getReforgeRules(), items, presetReforge);
//        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.standardItemsReforgedToMap(model.getReforgeRules(), items);
        ItemSet bestSets = EngineStream.runSolver(model, map, startTime, null);

//        Collection<ItemSet> bestSets = EngineRandom.runSolver(model, map, null);

        outputResult(bestSets, model, detailedOutput);
    }

    private void multiSpecSpecifiedRating() throws IOException {
        StatRatings statRatingsRet = new StatRatingsWeights(weightFileRetMine, false, 1, 1);
        StatRequirements statRet = new StatRequirements(false, false);
        ModelCombined modelRet = new ModelCombined(statRatingsRet, statRet, ReforgeRules.ret());

        StatRequirements statProt = new StatRequirements(false, true);
//        StatRatings protStatRatings = StatRatingsWeights.protHardcode();
        StatRatings protStatRatings = new StatRatingsWeights(weightFileProtMine, false, StatRatingsWeights.PROT_MULT, 1);
        ModelCombined modelProt = new ModelCombined(protStatRatings, statProt, ReforgeRules.prot());

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

        EnumMap<SlotEquip, ItemData> retForgedItems = ItemUtil.chosenItemsReforgedToMap(modelRet, retItems, reforgeRet);
        ItemSet retSet = ItemSet.manyItems(retForgedItems, null);

        EnumMap<SlotEquip, ItemData> protForgedItems = ItemUtil.chosenItemsReforgedToMap(modelProt, protItems, reforgeProt);
        ItemSet protSet = ItemSet.manyItems(protForgedItems, null);

        retSet.outputSet(modelRet);
        System.out.println("---------------------" + (modelRet.calcRating(retSet) + modelProt.calcRating(protSet)));
        protSet.outputSet(modelProt);
    }

//    private void multiSpecReforge(Instant startTime) throws IOException {
//        StatRatings statRatings = new StatRatingsPriority(new StatType[]{StatType.Expertise, StatType.Haste, StatType.Mastery, StatType.Crit});
//        ReforgeRules reforgeRules = new ReforgeRules();
//
//        StatRequirements statRet = new StatRequirements(false, false);
//        ModelCombined modelRet = new ModelCombined(statRatings, statRet, reforgeRules);
//
//        StatRequirements statProt = new StatRequirements(false, true);
//        ModelCombined modelProt = new ModelCombined(statRatings, statProt, reforgeRules);
//
//        System.out.println("RET GEAR CURRENT");
//        Map<SlotEquip, ItemData[]> retMap = readAndLoad2(true, inputFile, reforgeRules);
//        System.out.println("PROT GEAR CURRENT");
//        Map<SlotEquip, ItemData[]> protMap = readAndLoad2(true, inputProtFile, reforgeRules);
//
////        Collection<ItemSet> bestSets = EngineStreamDual.runSolver(modelRet, retMap, modelProt, protMap, null);
////        Collection<ItemSet> bestSets = EngineStream.runSolver(model, modelProt, protMap, startTime);
//
////        outputResult(bestSets, true);
//    }

    private EnumMap<SlotEquip, ItemData[]> readAndLoad2(boolean detailedOutput, Path file, ReforgeRules rules) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(file);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);
        return ItemUtil.standardItemsReforgedToMap(rules, items);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcess(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        Map<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(detailedOutput, gearRetFile, model.getReforgeRules());
        ItemSet bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        outputResult(bestSets, model, detailedOutput);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(ModelCombined model, Instant startTime, int extraItemId, boolean replace) throws IOException {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        reforgeProcessPlus(model, startTime, extraItemId, extraItem.slot.toSlotEquip(), replace);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(ModelCombined model, Instant startTime, int extraItemId, SlotEquip slot, boolean replace) throws IOException {
        Map<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(false, gearRetFile, model.getReforgeRules());

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        ItemData[] extraForged = Reforger.reforgeItem(model.getReforgeRules(), extraItem);
        if (replace) {
            reforgedItems.put(slot, extraForged);
        } else {
            ArrayUtil.map(reforgedItems.get(slot), ItemData::disenchant);
            reforgedItems.put(slot, ArrayUtil.concat(reforgedItems.get(slot), extraForged));
        }

//        reforgedItems.get(slot).addAll(Reforger.reforgeItem(model.getReforgeRules(), extraItem));
        //reforgedItems.computeIfPresent(slot, (x,lst) -> lst.stream().map(t -> new ItemData(t.slot, t.name, t.stat, StatBlock.empty, t.id)).toList());
        System.out.println("EXTRA " + extraItem);

        ItemSet bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        outputResult(bestSets, model, true);
    }

    private void reforgeProcessProtPlus(ModelCombined model, Instant startTime, int extraItemId, SlotEquip slot, boolean replace) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(false, gearProtFile, model.getReforgeRules());

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        System.out.println("EXTRA " + extraItem);
        ItemData[] extraForged = Reforger.reforgeItem(model.getReforgeRules(), extraItem);
        if (replace) {
            reforgedItems.put(slot, extraForged);
        } else {
            ArrayUtil.map(reforgedItems.get(slot), ItemData::disenchant);
            reforgedItems.put(slot, ArrayUtil.concat(reforgedItems.get(slot), extraForged));
        }

//        ItemSet bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        ItemSet bestSets = EngineRandom.runSolver(model, reforgedItems, null, null, 1000L * 1000L * 1000L);
        outputResult(bestSets, model, true);
    }

    private void reforgeProcessProtPlus2(ModelCombined model, Instant startTime, int extraItemId, int extraItemIdTwo) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(false, gearProtFile, model.getReforgeRules());

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        ItemData extraItemTwo = ItemUtil.loadItemBasic(itemCache, extraItemIdTwo);
        System.out.println("EXTRA " + extraItem);
        System.out.println("EXTRA " + extraItemTwo);
        ItemData[] extraForged = Reforger.reforgeItem(model.getReforgeRules(), extraItem);
        ArrayUtil.map(reforgedItems.get(extraItem.slot.toSlotEquip()), ItemData::disenchant);
        reforgedItems.put(extraItem.slot.toSlotEquip(), ArrayUtil.concat(reforgedItems.get(extraItem.slot.toSlotEquip()), extraForged));

        ItemData[] extraForgedTwo = Reforger.reforgeItem(model.getReforgeRules(), extraItemTwo);
        ArrayUtil.map(reforgedItems.get(extraItemTwo.slot.toSlotEquip()), ItemData::disenchant);
        reforgedItems.put(extraItemTwo.slot.toSlotEquip(), ArrayUtil.concat(reforgedItems.get(extraItemTwo.slot.toSlotEquip()), extraForgedTwo));

//        ItemSet bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        ItemSet bestSets = EngineRandom.runSolver(model, reforgedItems, null, null, 1000L * 1000L * 1000L);
        outputResult(bestSets, model, true);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeAlternatives(ModelCombined model, Instant startTime, int[] alternateItems) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(false, gearRetFile, model.getReforgeRules());

        for (int extraItemId : alternateItems) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            Map<SlotEquip, ItemData[]> itemMap = new EnumMap<>(reforgedItems);
            itemMap.put(extraItem.slot.toSlotEquip(), new ItemData[] { extraItem });
            ItemSet bestSets = EngineStream.runSolver(model, itemMap, null, null);
            outputResult(bestSets, model, false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlusPlus(ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2) throws IOException {
        ReforgeRules rules = model.getReforgeRules();
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(false, gearRetFile, rules);

        ItemData extraItem1 = ItemUtil.loadItemBasic(itemCache, extraItemId1);
        reforgedItems.computeIfPresent(extraItem1.slot.toSlotEquip(), (k,v) -> ArrayUtil.concat(v, Reforger.reforgeItem(rules, extraItem1)));
        System.out.println("EXTRA " + extraItem1);

        ItemData extraItem2 = ItemUtil.loadItemBasic(itemCache, extraItemId2);
        reforgedItems.computeIfPresent(extraItem2.slot.toSlotEquip(), (k,v) -> ArrayUtil.concat(v, Reforger.reforgeItem(rules, extraItem2)));
        System.out.println("EXTRA " + extraItem2);

        ItemSet bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        outputResult(bestSets, model, true);
    }

    private void outputResult(Collection<ItemSet> bestSets, StatRatings statRatings, boolean detailedOutput) {
        if (detailedOutput) {
            System.out.println("@@@@@@@@@ Set count " + bestSets.size() + " @@@@@@@@@");
            bestSets.forEach(s -> System.out.println(s.getTotals()));
            bestSets.forEach(s -> {
                System.out.println("#######################################");
                s.outputSet(statRatings);
            });
        } else {
            Optional<ItemSet> last = bestSets.stream().reduce((a, b) -> b);
            last.orElseThrow().outputSet(statRatings);
        }
    }

    private void outputResult(ItemSet bestSet, ModelCombined model, boolean detailedOutput) {
        if (bestSet != null) {
            bestSet.outputSet(model);
        } else {
            System.out.println("@@@@@@@@@ NO VALID SET RESULTS @@@@@@@@@");
        }
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
