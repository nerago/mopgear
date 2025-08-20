package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.BestCollection;
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
import java.util.function.Function;
import java.util.stream.Stream;

import static au.nicholas.hardy.mopgear.SourcesOfItems.*;
import static au.nicholas.hardy.mopgear.SourcesOfItems.strengthPlateValorCelestialP1;
import static au.nicholas.hardy.mopgear.StatType.*;

@SuppressWarnings({"CallToPrintStackTrace", "ThrowablePrintedToSystemOut", "SameParameterValue", "unused", "OptionalUsedAsFieldOrParameterType"})
public class Main {

    public static final long BILLION = 1000 * 1000 * 1000;

    ItemCache itemCache;

    public static void main(String[] arg) throws IOException, ExecutionException, InterruptedException {
        new Main().run();
    }

    private void run() throws IOException, ExecutionException, InterruptedException {
        itemCache = new ItemCache(DataLocation.cacheFile);

        Instant startTime = Instant.now();

        try (ForkJoinPool myPool = new ForkJoinPool(12, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false, 20, 256, 10, null, 60, TimeUnit.SECONDS)) {
            myPool.submit(() -> exceptionalCheck(startTime)).get();
        }

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void exceptionalCheck(Instant startTime) {
        try {
//            WowHead.fetchItem(89061);

//            multiSpecSpecifiedRating();
//            multiSpecSequential(startTime);

//            reforgeRet(startTime);
            reforgeProt(startTime);
//            reforgeBoom(startTime);
//        rankSomething();
//        multiSpecReforge(startTime);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    // protmit	21523312
    // protdps	38197350
    // ret	    15526158
    // SEE SPREADSHEET
    // mults: miti*33 prot_dps*8 ret*32

    private void rankSomething() throws IOException {
        ModelCombined model = ModelCombined.standardRetModel();
//        ModelCombined model = standardProtModel();

        // assumes socket bonus+non matching gems
        Map<Integer, StatBlock> enchants = Map.of(
//                86145, new StatBlock(120+285, 0, 0, 165, 0, 640,0,0,0),
                86145, new StatBlock(285, 0, 0, 165, 0, 640, 0, 0, 0, 0),
                84870, new StatBlock(0, 430, 0, 0, 0, 640, 0, 165, 0, 0));

        rankAlternativesAsSingleItems(model, new int[]{82856, 86794}, enchants, false);
//        rankAlternatives(new int [] {81129,81234,82850,81571}); // cloak
//        rankAlternatives(new int [] {84036,81190,81687,81130,81086}); // belt
//        rankAlternativesAsSingleItems(model, new int[]{84027, 81284, 81073, 81113, 82852}); // feet
//        rankAlternativesAsSingleItems(model, new int[]{86145, 82812, 84870}, enchants, false); // legs
    }

    private void reforgeRet(Instant startTime) throws IOException {
//        ModelCombined model = ModelCombined.standardRetModel();
        ModelCombined model = ModelCombined.extendedRetModel(true, true);
//        ModelCombined model = ModelCombined.priorityRetModel();

//        EnumMap<SlotEquip, ReforgeRecipe> commonItems = commonFixedItems();
        EnumMap<SlotEquip, ReforgeRecipe> commonItems = null;

        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearRetFile, model.reforgeRules(), commonItems);

//        reforgeProcess(items, model, startTime, true);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);


//        reforgeProcessPlus(items, model, startTime, true, 89981, true, true, null);
//        reforgeProcessPlus(items, model, startTime, true,86145, false, true, new StatBlock(285+80+120,0,0,165,160,160+160,0,0,0));
//        reforgeProcessPlus(items, model, startTime, true, 84950, false, true, null);
//        reforgeProcessPlusPlus(model, startTime, 81251, 81694);
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.bagItemsArray(model, new int[]{77530,89075,81262,87607,89823}));
//        reforgeProcessRetFixed(model, startTime, true);
//        reforgeProcessRetChallenge(model, startTime);

//                        findUpgradeSetup(items, strengthPlateMsvArray(), model);
//                findUpgradeSetup(items, strengthPlateValorArray(), model);
        findUpgradeSetup(items, strengthPlateValorCelestialP1(itemCache), model);
//        findUpgradeSetup(items, strengthPlateCurrentItemsProt(model), model);
//        findUpgradeSetup(items, bagItemsArray(model), model);
//                findUpgradeSetup(items, strengthPlateCrafted(), model);

//        combinationDumb(items, model, startTime);
    }

    private void reforgeProt(Instant startTime) throws IOException {
        ModelCombined model = ModelCombined.standardProtModel();
//        EnumMap<SlotEquip, ReforgeRecipe> commonItems = commonFixedItems();
        EnumMap<SlotEquip, ReforgeRecipe> commonItems = null;

        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearProtFile, model.reforgeRules(), commonItems);

//        reforgeProcess(items, model, startTime, true);
//        reforgeProcessProtFixedPlus(model, startTime, 86789, false, true);
//        reforgeProcessProtFixed(model, startTime, true);
//        reforgeProcessPlus(items, model, startTime, true,84950, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true,85991, false, true, null);
//        reforgeProcessPlusPlus(items, model, startTime, 89817, 86075);
//        reforgeProcessPlusMany(items, model, startTime, strengthPlateCurrentItemsRet(model));
//        findUpgradeSetup(items, strengthPlateCurrentItemsRet(model), model);
        findUpgradeSetup(items, bagItemsArray(model, ignoredItems), model);
//        findUpgradeSetup(items, strengthPlateMsvHeroicArray(), model);
//        findUpgradeSetup(items, strengthPlateHeartOfFearHeroic(), model);
//        findUpgradeSetup(items, strengthPlateHeartOfFear(), model);
//        findUpgradeSetup(items, strengthPlateValorArray(), model);
//        findUpgradeSetup(items, strengthPlateCrafted(), model);
//        findUpgradeSetup(items, strengthPlateValorCelestialP1()), model);

        // so we could get a conclusive result from the ret, then set the common slots to fixed
    }

    private void reforgeBoom(Instant startTime) throws IOException {
        ModelCombined model = ModelCombined.standardBoomModel();
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearBoomFile, model.reforgeRules(), null);

//        reforgeProcess(items, model, startTime, true);
//        reforgeProcessPlus(items, model, startTime, true, 86783, false, true, null);

        Tuple.Tuple2<Integer, Integer>[] filteredCelestialArray = SourcesOfItems.filterItemLevel(itemCache, intellectLeatherCelestialArray(), 476);
        findUpgradeSetup(items, ArrayUtil.concat(filteredCelestialArray, intellectLeatherValorArray()), model);

//        findUpgradeSetup(items, intellectLeatherCelestialArray(), model, 476);
//        findUpgradeSetup(items, intellectLeatherValorArray(), model, null);
    }

    private void combinationDumb(EquipOptionsMap items, ModelCombined model, Instant startTime) {
        for (int extraId : new int[]{89503, 81129, 89649, 87060, 89665, 82812, 90910, 81284, 82814, 84807, 84870, 84790, 82822}) {
            ItemData extraItem = addExtra(items, model, extraId, Function.identity(), null, false, true);
            System.out.println("EXTRA " + extraItem);
        }
        //        ItemUtil.disenchant(items);
        ItemUtil.defaultEnchants(items, model, true);
        ItemUtil.bestForgesOnly(items, model);
        ItemLevel.scaleForChallengeMode(items);

        ModelCombined dumbModel = model.withNoRequirements();

        Optional<ItemSet> bestSet = chooseEngineAndRun(dumbModel, items, startTime, null, null);
        outputResult(bestSet, model, true);
    }

    private void findUpgradeSetup(EquipOptionsMap reforgedItems, Tuple.Tuple2<Integer, Integer>[] extraItemArray, ModelCombined model) throws IOException {
//        SlotEquip slot = SlotEquip.Ring2;

//        long runSize = 10000000; // quick runs
        long runSize = 50000000; // 2 min total runs
//        long runSize = 100000000; // 4 min total runs
//        long runSize = 300000000; // 12 min total runs
//        long runSize = 1000000000; // 40 min runs

//        ItemUtil.defaultEnchants(reforgedItems, model, true);

        ItemSet baseSet = reforgeProcessLight(reforgedItems, model, runSize, true).get();
        double baseRating = model.calcRating(baseSet);
        System.out.printf("BASE RATING    = %.0f\n", baseRating);

        BestCollection<ItemData> bestCollection = new BestCollection<>();
        for (Tuple.Tuple2<Integer, Integer> extraItemInfo : extraItemArray) {
            int extraItemId = extraItemInfo.a();
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            SlotEquip slot = extraItem.slot.toSlotEquip();

            if (canSkipUpgradeCheck(extraItem, slot, reforgedItems))
                continue;

            if (extraItemInfo.b() != null) {
                System.out.println(extraItem.toStringExtended() + " $" + extraItemInfo.b());
            } else {
                System.out.println(extraItem.toStringExtended());
            }

            checkOneUpgrade(reforgedItems.deepClone(), model, extraItemId, slot, runSize, baseRating, bestCollection, extraItem);

            if (slot == SlotEquip.Trinket1) {
                checkOneUpgrade(reforgedItems.deepClone(), model, extraItemId, SlotEquip.Trinket2, runSize, baseRating, bestCollection, extraItem);
            }
            if (slot == SlotEquip.Ring1) {
                checkOneUpgrade(reforgedItems.deepClone(), model, extraItemId, SlotEquip.Ring2, runSize, baseRating, bestCollection, extraItem);
            }
        }

        System.out.println("RANKING RANKING");
        bestCollection.forEach((item, factor) ->
                System.out.printf("%10s \t%35s \t$%d \t%1.3f\n", item.slot, item.name,
                        ArrayUtil.findOne(extraItemArray, x -> x.a() == item.id).b(),
                        factor));
    }

    private boolean canSkipUpgradeCheck(ItemData extraItem, SlotEquip slot, EquipOptionsMap reforgedItems) {
        if (SourcesOfItems.ignoredItems.contains(extraItem.id))
            return true;

        if (reforgedItems.get(slot) == null) {
            System.out.println("SLOT NOT USED IN CURRENT SET " + extraItem.toStringExtended());
            return true;
        }
        if (reforgedItems.get(slot)[0].id == extraItem.id) {
            System.out.println("SAME ITEM " + extraItem.toStringExtended());
            return true;
        }

        return false;
    }

    private void checkOneUpgrade(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, SlotEquip slot, long runSize, double baseRating, BestCollection<ItemData> bestCollection, ItemData extraItem) throws IOException {
        Function<ItemData, ItemData> enchanting = x -> ItemUtil.defaultEnchants(x, model, true);
        Optional<ItemSet> extraSet = reforgeProcessPlusCore(reforgedItems, model, null, false, extraItemId, slot, enchanting, true, runSize);
        if (extraSet.isPresent()) {
            System.out.println("PROPOSED " + extraSet.get().totals);
            double extraRating = model.calcRating(extraSet.get());
            double factor = extraRating / baseRating;
            System.out.printf("UPGRADE RATING = %.0f FACTOR = %1.3f\n", extraRating, factor);
            bestCollection.add(extraItem, factor);
        } else {
            System.out.print("UPGRADE SET NOT FOUND\n");
        }
        System.out.println();
    }

    private void rankAlternativesAsSingleItems(ModelCombined model, int[] itemIds, Map<Integer, StatBlock> enchants, boolean scaleChallenge) {
        Stream<ItemData> stream = Arrays.stream(itemIds)
                .mapToObj(x -> new EquippedItem(x, new int[0], null))
                .map(x -> ItemUtil.loadItem(itemCache, x, true))
                .flatMap(x -> Arrays.stream(Reforger.reforgeItem(model.reforgeRules(), x)));
        if (enchants != null) {
            stream = stream.map(x ->
                    enchants.containsKey(x.id) ?
                            x.changeFixed(enchants.get(x.id)) :
                            ItemUtil.defaultEnchants(x, model, true)
            );
        }
        if (scaleChallenge) {
            stream = stream.map(ItemLevel::scaleForChallengeMode);
        }
        List<ItemData> reforgedItems = stream
                .sorted(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())))
                .toList();
        for (ItemData item : reforgedItems) {
            System.out.println(item + " " + model.calcRating(item.totalStatCopy()));
        }
    }

    private void multiSpecSequential(Instant startTime) throws IOException {
        ModelCombined modelNull = ModelCombined.nullMixedModel();
        ModelCombined modelRet = ModelCombined.extendedRetModel(true, false);
        ModelCombined modelProt = ModelCombined.standardProtModel();

        System.out.println("RET GEAR CURRENT");
        EquipOptionsMap retMap = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearRetFile, modelRet.reforgeRules(), null);
        System.out.println("PROT GEAR CURRENT");
        EquipOptionsMap protMap = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearProtFile, modelProt.reforgeRules(), null);
        ItemUtil.validateDualSets(retMap, protMap);
        EquipOptionsMap commonMap = ItemUtil.commonInDualSet(retMap, protMap);

//        commonMap.replaceWithFirstOption(SlotEquip.Neck);
//        commonMap.replaceWithFirstOption(SlotEquip.Hand);
//        commonMap.replaceWithFirstOption(SlotEquip.Trinket1);

        Stream<ItemSet> commonStream = EngineStream.runSolverPartial(modelNull, commonMap, startTime, null, 0);

        // TODO solve for challenge dps too

        Long runSize = BILLION / 100;
//        Long runSize = 10000L;
        Stream<ItemSet> protStream = commonStream.map(r -> subSolveBoth(r, retMap, modelRet, protMap, modelProt, runSize))
                .filter(Objects::nonNull);

        Collection<ItemSet> best = protStream.collect(
                new TopCollectorReporting<>(s -> dualRating(s, modelRet, modelProt),
                        s -> reportBetter(s, modelRet, modelProt, retMap, protMap)));
        outputResult(best, modelProt, true);
    }

    private void reportBetter(ItemSet itemSet, ModelCombined modelRet, ModelCombined modelProt, EquipOptionsMap itemsRet, EquipOptionsMap itemsProt) {
        long rating = modelProt.calcRating(itemSet) + modelRet.calcRating(itemSet.otherSet);
        synchronized (System.out) {
            System.out.println(LocalDateTime.now());
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            itemSet.otherSet.outputSet(modelRet);
            outputTweaked(itemSet.otherSet, itemsRet, modelRet);
            System.out.println("--------------------------------------- " + rating);
            itemSet.outputSet(modelProt);
            outputTweaked(itemSet, itemsProt, modelProt);
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

    private ItemSet subSolveBoth(ItemSet chosenSet, EquipOptionsMap retMap, ModelCombined modelRet, EquipOptionsMap protMap, ModelCombined modelProt, Long runSize) {
        EquipMap chosenMap = chosenSet.items;

//        System.out.println(chosenMap.values().stream().map(ItemData::toString).reduce("", String::concat));

        Optional<ItemSet> retSet = subSolvePart(retMap, modelRet, chosenMap, null, runSize);
        if (retSet.isPresent()) {
            Optional<ItemSet> protSet = subSolvePart(protMap, modelProt, chosenMap, retSet.get(), runSize);
            return protSet.orElse(null);
        }
        return null;
    }

    private static Optional<ItemSet> subSolvePart(EquipOptionsMap fullItemMap, ModelCombined model, EquipMap chosenMap, ItemSet otherSet, Long runSize) {
        EquipOptionsMap submitMap = fullItemMap.shallowClone();
        ItemUtil.buildJobWithSpecifiedItemsFixed(chosenMap, submitMap); // TODO build into map object
        return chooseEngineAndRun(model, submitMap, null, runSize, otherSet);
    }

    private static EnumMap<SlotEquip, ReforgeRecipe> commonFixedItems() {
        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
//        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(Hit, Expertise));
        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(Crit, Expertise));
        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(Haste, Hit));
        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(Dodge, Expertise));
//        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(Haste, Expertise));
        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(Mastery, Expertise));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(Haste, Hit));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));

//        COMMON Soulgrasp Choker
//        COMMON Malevolent Gladiator's Cloak of Alacrity
//        COMMON Bonded Soul Bracers
//        COMMON Starcrusher Gauntlets
//        COMMON Jasper Clawfeet
//        COMMON Seal of the Bloodseeker
//        COMMON Ring of the Golden Stair
//        COMMON Lei Shen's Final Orders

        return presetReforge;
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessProtFixed(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = commonFixedItems();

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null);
        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    private void reforgeProcessProtFixedPlus(ModelCombined model, Instant startTime, int extraItemId, boolean replace, boolean defaultEnchants) throws IOException {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, true);

        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = commonFixedItems();
        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Function<ItemData, ItemData> enchanting = defaultEnchants ? x -> ItemUtil.defaultEnchants(x, model, true) : Function.identity();

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        extraItem = addExtra(map, model, extraItemId, extraItem.slot.toSlotEquip(), enchanting, null, replace, true);
        System.out.println("EXTRA " + extraItem);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null);
        outputResult(bestSet, model, true);
        outputTweaked(bestSet, map, model);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetFixed(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = commonFixedItems();

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, null, null, null);

        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetChallenge(ModelCombined model, Instant startTime) throws IOException {
        // CHALLENGE MODE SET

        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
        List<ItemData> inputSetItems = ItemUtil.loadItems(itemCache, itemIds, true);

        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(Hit, Expertise));
        presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(Expertise, Hit));
        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(Crit, Expertise));
        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(Mastery, Haste));
        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(Haste, Hit));
        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(Crit, Hit));
        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(Dodge, Expertise));
        presetReforge.put(SlotEquip.Leg, new ReforgeRecipe(Crit, Hit));
        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(Mastery, Expertise));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(Haste, Hit));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));
        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(Crit, Hit));

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), inputSetItems, presetReforge);

//        int[] removeItems = new int[] { }

        // compared to raid dps
        int[] extraItems = new int[]{
                89503, 81129,
//                87060,
                89665, 82812, 82814, 90910, 81284, 82822, 81694, 86794, 86145,
                81113, 81251, 81268, 81138
        };

        Function<ItemData, ItemData> customiseItem = extraItem -> {
            if (extraItem.id == 82812) { // Pyretic Legguards
                return extraItem.changeFixed(new StatBlock(285, 0, 0, 165, 160, 160 + 60, 0, 0, 0, 0));
            } else if (extraItem.id == 81284) { // Anchoring Sabatons
                return extraItem.changeFixed(new StatBlock(60 + 60, 0, 140, 0, 0, 120, 0, 0, 0, 0));
            } else if (extraItem.id == 81113) { // Spike-Soled Stompers
                return extraItem.changeFixed(new StatBlock(60, 0, 0, 0, 160, 175 + 160, 0, 0, 0, 0));
            } else if (extraItem.id == 87060) { // Star-Stealer Waistguard
                return extraItem.changeFixed(new StatBlock(0, 0, 0, 0, 160, 160, 320 + 160 + 160, 120, 0, 0));
            } else if (extraItem.id == 86794) { // starcrusher gauntlets
                return extraItem.changeFixed(new StatBlock(170, 0, 0, 0, 160, 60 + 320 + 160, 0, 0, 0, 0));
            } else if (extraItem.id == 86145) { // jang-xi devastating legs
                return extraItem.changeFixed(new StatBlock(120, 430, 0, 0, 160, 160 * 2, 160, 0, 0, 0));
            } else if (extraItem.id == 89503) { // Greenstone Drape
                return extraItem.changeStats(new StatBlock(501, 751, 0, 334, 334, 0, 0, 0, 0, 0))
                        .changeFixed(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
            } else if (extraItem.slot == SlotItem.Back) {
                return extraItem.changeFixed(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
            } else {
                System.out.println("DEFAULT ENCHANT " + extraItem);
                return ItemUtil.defaultEnchants(extraItem, model, false);
            }
        };

        for (int extraId : extraItems) {
            ReforgeRecipe reforge = null;
            if (extraId == 86794)
                reforge = new ReforgeRecipe(Hit, Expertise);
            ItemData extraItem = addExtra(map, model, extraId, customiseItem, reforge, false, true);
            System.out.println("EXTRA " + extraItem);
        }

        EquipOptionsMap scaledMap = ItemLevel.scaleForChallengeMode(map);

        ItemSet bestScaledSet = chooseEngineAndRun(model, scaledMap, startTime, null, null).orElseThrow();

        System.out.println("SCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALED");
        outputResult(Optional.of(bestScaledSet), model, true);

        for (SlotEquip slot : SlotEquip.values()) {
            ItemData scaledChoice = bestScaledSet.items.get(slot);
            if (scaledChoice != null) {
                ItemData[] options = map.get(slot);
                boolean inRaidDPSSet = inputSetItems.stream().anyMatch(x -> x.id == scaledChoice.id);

                if (inRaidDPSSet) {
                    // need exact item + forge but prescale
                    // note were using id match only, scaled stuff could confused normal "exact" match
                    // avoid engineering heads mixup
                    ItemData match = ArrayUtil.findOne(options, x -> x.id == scaledChoice.id && Objects.equals(x.reforge, scaledChoice.reforge));
                    options = new ItemData[]{match};
                } else {
                    options = ArrayUtil.allMatch(options, x -> x.id == scaledChoice.id);
                }

                map.put(slot, options);
            }
        }

        ModelCombined finalModel = new ModelCombined(model.statRatings(), StatRequirements.retWideCapRange(), model.reforgeRules(), model.enchants());
        Optional<ItemSet> bestSetFinal = chooseEngineAndRun(finalModel, map, startTime, null, null);

        System.out.println("FINALFINALFINALFINALFINALFINALFINALFINALFINALFINALFINAL");
        outputResult(bestSetFinal, model, true);
    }

    private void multiSpecSpecifiedRating() throws IOException {
        ModelCombined modelRet = ModelCombined.standardRetModel();
        ModelCombined modelProt = ModelCombined.standardProtModel();

        System.out.println("RET GEAR CURRENT");
        List<ItemData> retItems = ItemUtil.loadItems(itemCache, InputGearParser.readInput(DataLocation.gearRetFile), true);
        System.out.println("PROT GEAR CURRENT");
        List<ItemData> protItems = ItemUtil.loadItems(itemCache, InputGearParser.readInput(DataLocation.gearProtFile), true);

        Map<SlotEquip, ReforgeRecipe> reforgeRet = new EnumMap<>(SlotEquip.class);
        reforgeRet.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        reforgeRet.put(SlotEquip.Neck, new ReforgeRecipe(StatType.Crit, Expertise));
        reforgeRet.put(SlotEquip.Shoulder, new ReforgeRecipe(Expertise, Haste));
        reforgeRet.put(SlotEquip.Back, new ReforgeRecipe(StatType.Crit, Expertise));
        reforgeRet.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, Expertise));
        reforgeRet.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Hit, Haste));
        reforgeRet.put(SlotEquip.Hand, new ReforgeRecipe(StatType.Crit, StatType.Hit));
        reforgeRet.put(SlotEquip.Belt, new ReforgeRecipe(Mastery, Expertise));
        reforgeRet.put(SlotEquip.Leg, new ReforgeRecipe(StatType.Crit, Mastery));
        reforgeRet.put(SlotEquip.Foot, new ReforgeRecipe(Mastery, Expertise));
        reforgeRet.put(SlotEquip.Ring1, new ReforgeRecipe(StatType.Crit, Haste));
        reforgeRet.put(SlotEquip.Ring2, new ReforgeRecipe(StatType.Crit, Mastery));
        reforgeRet.put(SlotEquip.Trinket1, new ReforgeRecipe(null, null));
        reforgeRet.put(SlotEquip.Trinket2, new ReforgeRecipe(Expertise, Mastery));
        reforgeRet.put(SlotEquip.Weapon, new ReforgeRecipe(StatType.Hit, Haste));

        Map<SlotEquip, ReforgeRecipe> reforgeProt = new EnumMap<>(SlotEquip.class);
        reforgeProt.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Neck, new ReforgeRecipe(StatType.Crit, Expertise));
        reforgeProt.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Dodge, StatType.Hit));
        reforgeProt.put(SlotEquip.Back, new ReforgeRecipe(StatType.Crit, Expertise));
        reforgeProt.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, Expertise));
        reforgeProt.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Dodge, Mastery));
        reforgeProt.put(SlotEquip.Hand, new ReforgeRecipe(StatType.Parry, StatType.Hit));
        reforgeProt.put(SlotEquip.Belt, new ReforgeRecipe(StatType.Dodge, StatType.Hit));
        reforgeProt.put(SlotEquip.Leg, new ReforgeRecipe(StatType.Dodge, Mastery));
        reforgeProt.put(SlotEquip.Foot, new ReforgeRecipe(StatType.Parry, Expertise));
        reforgeProt.put(SlotEquip.Ring1, new ReforgeRecipe(StatType.Parry, Expertise));
        reforgeProt.put(SlotEquip.Ring2, new ReforgeRecipe(StatType.Crit, Mastery));
        reforgeProt.put(SlotEquip.Trinket1, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Trinket2, new ReforgeRecipe(Expertise, Mastery));
        reforgeProt.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));

        EquipMap retForgedItems = ItemUtil.chosenItemsReforgedToMap(retItems, reforgeRet);
        ItemSet retSet = ItemSet.manyItems(retForgedItems, null);

        EquipMap protForgedItems = ItemUtil.chosenItemsReforgedToMap(protItems, reforgeProt);
        ItemSet protSet = ItemSet.manyItems(protForgedItems, null);

        retSet.outputSet(modelRet);
        System.out.println("---------------------" + (modelRet.calcRating(retSet) + modelProt.calcRating(protSet)));
        protSet.outputSet(modelProt);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcess(EquipOptionsMap reforgedItems, ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        Optional<ItemSet> bestSet = chooseEngineAndRun(model, reforgedItems, startTime, BILLION, null);
        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, reforgedItems, model);
    }

    private Optional<ItemSet> reforgeProcessLight(EquipOptionsMap reforgedItems, ModelCombined model, long runSize, boolean outputExistingGear) throws IOException {
        Optional<ItemSet> bestSet = chooseEngineAndRun(model, reforgedItems, null, runSize, null);
        return bestSet.map(itemSet -> Tweaker.tweak(itemSet, model, reforgedItems));
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(EquipOptionsMap reforgedItems, ModelCombined model, Instant startTime, boolean detailedOutput, int extraItemId, boolean replace, boolean defaultEnchants, StatBlock extraItemEnchants) throws IOException {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        Function<ItemData, ItemData> enchanting =
                extraItemEnchants != null ? x -> x.changeFixed(extraItemEnchants) :
                        defaultEnchants ? x -> ItemUtil.defaultEnchants(x, model, true) :
                                Function.identity();
        long runSize = BILLION;
        Optional<ItemSet> bestSet = reforgeProcessPlusCore(reforgedItems, model, startTime, detailedOutput, extraItemId, extraItem.slot.toSlotEquip(), enchanting, replace, runSize);
        outputResult(bestSet, model, detailedOutput);
    }

    private Optional<ItemSet> reforgeProcessPlusCore(EquipOptionsMap reforgedItems, ModelCombined model, Instant startTime, boolean detailedOutput, int extraItemId, SlotEquip slot, Function<ItemData, ItemData> enchanting, boolean replace, Long runSize) throws IOException {
        EquipOptionsMap runItems = reforgedItems.deepClone();
        ItemData extraItem = addExtra(runItems, model, extraItemId, slot, enchanting, null, replace, true);
        ArrayUtil.mapInPlace(runItems.get(slot), enchanting);

        if (detailedOutput) {
            System.out.println("EXTRA " + extraItem);
        }

        return chooseEngineAndRun(model, runItems, startTime, runSize, null);
    }

    private ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        return addExtra(reforgedItems, model, extraItemId, extraItem.slot.toSlotEquip(), customiseItem, reforge, replace, customiseOthersInSlot);
    }

    private ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, SlotEquip slot, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        extraItem = customiseItem.apply(extraItem);
        ItemData[] extraForged = reforge != null ?
                new ItemData[]{Reforger.presetReforge(extraItem, reforge)} :
                Reforger.reforgeItem(model.reforgeRules(), extraItem);
        if (replace) {
            System.out.println("REPLACING " + (reforgedItems.get(slot) != null ? reforgedItems.get(slot)[0] : "NOTHING"));
            reforgedItems.put(slot, extraForged);
        } else {
            ItemData[] existing = reforgedItems.get(slot);
            if (ArrayUtil.anyMatch(existing, item -> item.id == extraItemId))
                throw new IllegalArgumentException("item already included " + extraItemId + " " + extraItem);
            reforgedItems.put(slot, ArrayUtil.concat(existing, extraForged));
        }
        ItemData[] slotArray = reforgedItems.get(slot);
        if (customiseOthersInSlot) {
            ArrayUtil.mapInPlace(slotArray, customiseItem);
        }
        HashSet<Integer> seen = new HashSet<>();
        ArrayUtil.forEach(slotArray, it -> {
            if (seen.add(it.id)) {
                System.out.println("NEW " + slot + " " + it);
            }
        });
        System.out.println();
        return extraItem;
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeAlternatives(Path file, ModelCombined model, Instant startTime, int[] alternateItems) throws IOException {
        EquipOptionsMap reforgedItems = ItemUtil.readAndLoad(itemCache, false, file, model.reforgeRules(), null);

        for (int extraItemId : alternateItems) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            EquipOptionsMap itemMap = reforgedItems.copyWithReplaceSingle(extraItem.slot.toSlotEquip(), extraItem);
            Optional<ItemSet> bestSets = EngineStream.runSolver(model, itemMap, null, null, 0);
            outputResult(bestSets, model, false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlusPlus(EquipOptionsMap reforgedItems, ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2) throws IOException {
        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, model, true);

        ItemData extraItem1 = addExtra(reforgedItems, model, extraItemId1, enchant, null, false, true);
        System.out.println("EXTRA " + extraItem1);

        ItemData extraItem2 = addExtra(reforgedItems, model, extraItemId2, enchant, null, false, true);
        System.out.println("EXTRA " + extraItem2);

        Optional<ItemSet> best = chooseEngineAndRun(model, reforgedItems, startTime, BILLION * 3, null);
        outputResult(best, model, true);
    }

    private void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, Tuple.Tuple2<Integer, Integer>[] extraItems) {
        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, model, true);

        for (Tuple.Tuple2<Integer, Integer> entry : extraItems) {
            int extraItemId = entry.a();
            if (SourcesOfItems.ignoredItems.contains(extraItemId)) continue;
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            SlotEquip slot = extraItem.slot.toSlotEquip();
            ItemData[] existing = items.get(slot);
            if (existing == null) {
                System.out.println("SKIP SLOT NOT NEEDED " + extraItem);
            } else if (ArrayUtil.anyMatch(existing, item -> item.id == extraItemId)) {
                System.out.println("SKIP DUP " + extraItem);
            } else {
                if (slot == SlotEquip.Trinket1) {
                    addExtra(items, model, extraItemId, SlotEquip.Trinket1, enchant, null, false, true);
                    addExtra(items, model, extraItemId, SlotEquip.Trinket2, enchant, null, false, true);
                } else if (slot == SlotEquip.Ring1) {
                    addExtra(items, model, extraItemId, SlotEquip.Ring1, enchant, null, false, true);
                    addExtra(items, model, extraItemId, SlotEquip.Ring2, enchant, null, false, true);
                } else {
                    addExtra(items, model, extraItemId, slot, enchant, null, false, true);
                }
            }
        }

        Long runSize = BILLION * 2;
//                Long runSize = BILLION / 10;
//        Long runSize = null;
        Optional<ItemSet> best = chooseEngineAndRun(model, items, startTime, runSize, null);
        outputResult(best, model, true);
    }

    private static Optional<ItemSet> chooseEngineAndRun(ModelCombined model, EquipOptionsMap reforgedItems, Instant startTime, Long runSize, ItemSet otherSet) {
        long estimate = ItemUtil.estimateSets(reforgedItems);

        if (runSize != null && estimate > runSize) {
            if (startTime != null)
                System.out.printf("COMBINATIONS estimate=%,d RANDOM SAMPLE %,d\n", estimate, runSize);
            Optional<ItemSet> proposed = EngineRandom.runSolver(model, reforgedItems, startTime, otherSet, runSize);
            return proposed.map(itemSet -> Tweaker.tweak(itemSet, model, reforgedItems));
        } else {
            if (startTime != null)
                System.out.printf("COMBINATIONS estimate=%,d FULL RUN\n", estimate);
            return EngineStream.runSolver(model, reforgedItems, startTime, otherSet, estimate);
        }
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

    private void outputTweaked(Optional<ItemSet> bestSet, EquipOptionsMap reforgedItems, ModelCombined model) {
        if (bestSet.isPresent()) {
            outputTweaked(bestSet.get(), reforgedItems, model);
        }
    }

    private static void outputTweaked(ItemSet bestSet, EquipOptionsMap reforgedItems, ModelCombined model) {
        ItemSet tweakSet = Tweaker.tweak(bestSet, model, reforgedItems);
        if (bestSet != tweakSet) {
            System.out.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");

            System.out.println(tweakSet.getTotals().toStringExtended() + " " + model.calcRating(tweakSet.getTotals()));
            for (SlotEquip slot : SlotEquip.values()) {
                ItemData orig = bestSet.items.get(slot);
                ItemData change = tweakSet.items.get(slot);
                if (orig != null && change != null) {
                    if (!ItemData.isIdenticalItem(orig, change)) {
                        System.out.println(change + " " + model.calcRating(change.totalStatCopy()));
                    }
                } else if (orig != null || change != null) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
