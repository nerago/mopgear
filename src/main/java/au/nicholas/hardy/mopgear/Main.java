package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.TopCollectorReporting;
import au.nicholas.hardy.mopgear.util.Tuple;
import au.nicholas.hardy.mopgear.util.ArrayUtil;

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

@SuppressWarnings({"CallToPrintStackTrace", "ThrowablePrintedToSystemOut"})
public class Main {

    private static final Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    static final Path cacheFile = directory.resolve("cache.json");
    private static final Path inputFile = directory.resolve("input.json");
    private static final Path inputProtFile = directory.resolve("input-prot.json");
    private static final Path weightFileMine = directory.resolve("weight-mysim.json");
    private static final Path weightFileStandard = directory.resolve("weight-standard.json");

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
//            multiSpecSequential(startTime);
        reforgeRet(startTime);

//            multiSpecReforge(startTime);
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
        StatRatings statRatings = new StatRatingsWeights(weightFileStandard, true);
        StatRequirements statRequirements = new StatRequirements(false, false);
        ModelCombined model = new ModelCombined(statRatings, statRequirements, new ReforgeRules());

//        rankAlternatives(new int [] {89530,81239,81567,81180,81568}); // necks
//        rankAlternatives(new int [] {81129,81234,82850,81571}); // cloak
//        rankAlternatives(new int [] {84036,81190,81687,81130,81086}); // belt
        rankAlternatives(model, new int[]{84027, 81284, 81073, 81113, 82852}); // feet
    }

    private void reforgeRet(Instant startTime) throws IOException {
        StatRatings statRatings = new StatRatingsWeights(weightFileMine, false);
        StatRequirements statRequirements = new StatRequirements(false, false);
        ModelCombined model = new ModelCombined(statRatings, statRequirements, new ReforgeRules());

//        reforgeProcess(model, startTime, true);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);
//        reforgeProcessPlus(model, startTime, 89345, true); // shoulder
        reforgeProcessPlus(model, startTime, 90910, false);
//        reforgeProcessPlusPlus(model, startTime, 81251, 81694);
    }

    private void reforgeProt(Instant startTime) throws IOException {
//        StatRatings statRatings = new StatRatingsWeights(weightFileMine, false);
        StatRatings statRatings = new StatRatingsPriority(new StatType[]{StatType.Expertise, StatType.Haste, StatType.Mastery, StatType.Crit});
        StatRequirements statRequirements = new StatRequirements(false, true);
        ModelCombined model = new ModelCombined(statRatings, statRequirements, new ReforgeRules());

        reforgeProcessProt(model, startTime, true);

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
        ReforgeRules reforgeRules = new ReforgeRules();

        StatRatings statRatingsRet = new StatRatingsWeights(weightFileMine, false);
        StatRequirements statRet = new StatRequirements(false, false);
        ModelCombined modelRet = new ModelCombined(statRatingsRet, statRet, reforgeRules);

        StatRequirements statProt = new StatRequirements(false, true);
        StatRatings protStatRatings = new StatRatingsPriority(new StatType[]{StatType.Expertise, StatType.Haste, StatType.Mastery, StatType.Crit});
        ModelCombined modelProt = new ModelCombined(protStatRatings, statProt, reforgeRules);

        System.out.println("RET GEAR CURRENT");
        EnumMap<SlotEquip, ItemData[]> retMap = readAndLoad2(true, inputFile, reforgeRules);
        System.out.println("PROT GEAR CURRENT");
        EnumMap<SlotEquip, ItemData[]> protMap = readAndLoad2(true, inputProtFile, reforgeRules);
        validateDualSets(retMap, protMap);

        Stream<ItemSet> retStream = EngineStream.runSolverPartial(modelRet, retMap, startTime, null);

//        retStream = retStream.peek(System.out::println);

        Stream<ItemSet> protStream = retStream.flatMap(r -> subSolveProt(r, protMap, modelProt));

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
        System.out.println(LocalDateTime.now());
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        itemSet.otherSet.outputSet(modelRet);
        System.out.println("--------------------------------------- " + rating);
        itemSet.outputSet(modelProt);
        System.out.println("#######################################");
    }

    private long dualRating(ItemSet set, ModelCombined modelRet, ModelCombined modelProt) {
        return modelRet.calcRating(set.otherSet) + modelProt.calcRating(set);
    }

    private Stream<? extends ItemSet> subSolveProt(ItemSet retSet, EnumMap<SlotEquip, ItemData[]> protMap, ModelCombined modelProt) {
        EnumMap<SlotEquip, ItemData> retMap = retSet.items;
        EnumMap<SlotEquip, ItemData[]> submitMap = protMap.clone();

        for (SlotEquip slot : SlotEquip.values()) {
            ItemData retItem = retMap.get(slot);
            ItemData[] protItemList = submitMap.get(slot);
            if (retItem == null || protItemList == null || protItemList.length == 0)
                continue;

            ItemData protItem = protItemList[0];
            if (protItem.id == retItem.id) {
                submitMap.put(slot, new ItemData[] { retItem });
            }
        }

        return EngineStream.runSolverPartial(modelProt, submitMap, null, retSet);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessProt(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> presetReforge = new EnumMap<>(SlotEquip.class);
        presetReforge.put(SlotEquip.Head, Tuple.create(null, null));
        presetReforge.put(SlotEquip.Neck, Tuple.create(StatType.Hit, StatType.Haste));
        presetReforge.put(SlotEquip.Shoulder, Tuple.create(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Back, Tuple.create(StatType.Mastery, StatType.Haste));
        presetReforge.put(SlotEquip.Chest, Tuple.create(StatType.Mastery, StatType.Haste));
        presetReforge.put(SlotEquip.Ring1, Tuple.create(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Trinket1, Tuple.create(StatType.Expertise, StatType.Haste));

        Map<SlotEquip, ItemData[]> map = ItemUtil.limitedItemsReforgedToMap(model.getReforgeRules(), items, presetReforge);
        Collection<ItemSet> bestSets = EngineStream.runSolver(model, map, startTime, null);
        outputResult(bestSets, model, detailedOutput);
    }

    private void multiSpecReforge(Instant startTime) throws IOException {
        StatRatings statRatings = new StatRatingsPriority(new StatType[]{StatType.Expertise, StatType.Haste, StatType.Mastery, StatType.Crit});
        ReforgeRules reforgeRules = new ReforgeRules();

        StatRequirements statRet = new StatRequirements(false, false);
        ModelCombined modelRet = new ModelCombined(statRatings, statRet, reforgeRules);

        StatRequirements statProt = new StatRequirements(false, true);
        ModelCombined modelProt = new ModelCombined(statRatings, statProt, reforgeRules);

        System.out.println("RET GEAR CURRENT");
        Map<SlotEquip, ItemData[]> retMap = readAndLoad2(true, inputFile, reforgeRules);
        System.out.println("PROT GEAR CURRENT");
        Map<SlotEquip, ItemData[]> protMap = readAndLoad2(true, inputProtFile, reforgeRules);

//        Collection<ItemSet> bestSets = EngineStreamDual.runSolver(modelRet, retMap, modelProt, protMap, null);
//        Collection<ItemSet> bestSets = EngineStream.runSolver(model, modelProt, protMap, startTime);

//        outputResult(bestSets, true);
    }

    private EnumMap<SlotEquip, ItemData[]> readAndLoad2(boolean detailedOutput, Path file, ReforgeRules rules) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(file);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);
        return ItemUtil.standardItemsReforgedToMap(rules, items);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcess(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        Map<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(detailedOutput, inputFile, model.getReforgeRules());
        Collection<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        outputResult(bestSets, model, detailedOutput);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(ModelCombined model, Instant startTime, int extraItemId, boolean replace) throws IOException {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        reforgeProcessPlus(model, startTime, extraItemId, extraItem.slot.toSlotEquip(), replace);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(ModelCombined model, Instant startTime, int extraItemId, SlotEquip slot, boolean replace) throws IOException {
        Map<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(false, inputFile, model.getReforgeRules());

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

        Collection<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
        outputResult(bestSets, model, true);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeAlternatives(ModelCombined model, Instant startTime, int[] alternateItems) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(false, inputFile, model.getReforgeRules());

        for (int extraItemId : alternateItems) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            Map<SlotEquip, ItemData[]> itemMap = new EnumMap<>(reforgedItems);
            itemMap.put(extraItem.slot.toSlotEquip(), new ItemData[] { extraItem });
            Collection<ItemSet> bestSets = EngineStream.runSolver(model, itemMap, null, null);
            outputResult(bestSets, model, false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlusPlus(ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2) throws IOException {
        ReforgeRules rules = model.getReforgeRules();
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad2(false, inputFile, rules);

        ItemData extraItem1 = ItemUtil.loadItemBasic(itemCache, extraItemId1);
        reforgedItems.computeIfPresent(extraItem1.slot.toSlotEquip(), (k,v) -> ArrayUtil.concat(v, Reforger.reforgeItem(rules, extraItem1)));
        System.out.println("EXTRA " + extraItem1);

        ItemData extraItem2 = ItemUtil.loadItemBasic(itemCache, extraItemId2);
        reforgedItems.computeIfPresent(extraItem2.slot.toSlotEquip(), (k,v) -> ArrayUtil.concat(v, Reforger.reforgeItem(rules, extraItem2)));
        System.out.println("EXTRA " + extraItem2);

        Collection<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime, null);
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

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
