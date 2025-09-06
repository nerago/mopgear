package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.DataLocation;
import au.nerago.mopgear.io.InputGearParser;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.model.ItemLevel;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.results.JobInfo;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.ArrayUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static au.nerago.mopgear.Jobs.*;
import static au.nerago.mopgear.Solver.chooseEngineAndRun;
import static au.nerago.mopgear.domain.StatType.*;

@SuppressWarnings({"SameParameterValue", "unused", "ConstantValue"})
public class Main {

    public static final long BILLION = 1000 * 1000 * 1000;

    public ItemCache itemCache;

    public static void main(String[] arg) throws IOException {
        try {
            new Main().run();
        } catch (Throwable ex) {
            OutputText.printException(ex);
        }
        OutputText.finish();
    }

    private void run() throws ExecutionException, InterruptedException {
        itemCache = new ItemCache(DataLocation.cacheFile);
        Jobs.itemCache = itemCache;

        Instant startTime = Instant.now();

        try (ForkJoinPool myPool = new ForkJoinPool(12, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false, 20, 256, 10, null, 60, TimeUnit.SECONDS)) {
            myPool.submit(() -> launchpad(startTime)).get();
        }

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void launchpad(Instant startTime) {
//            WowHead.fetchItem(86145);

//        multiSpecSolve(startTime);

        reforgeRet(startTime);
//            reforgeProt(startTime);
//            reforgeBoom(startTime);
//                    reforgeBear(startTime);
//            reforgeWarlock(startTime);
//        rankSomething();
//        multiSpecReforge(startTime);
//            multiSpecSpecifiedRating();
    }

    private void rankSomething() {
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

    private void reforgeRet(Instant startTime) {
//        ModelCombined model = ModelCombined.standardRetModel();
//        ModelCombined model = ModelCombined.extendedRetModel(true, true);
        ModelCombined model = ModelCombined.extendedRetModel(true, true);
//        ModelCombined model = ModelCombined.priorityRetModel();

//        Map<Integer, ReforgeRecipe> commonItems = commonFixedItems();
        Map<Integer, ReforgeRecipe> commonItems = null;

        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearRetFile, model.reforgeRules(), commonItems);

//        reforgeProcess(items, model, startTime);
//        new SolverHitCaps(model).solveHitCaps(items);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);

//        reforgeProcessPlus(items, model, startTime, true, 89981, true, true, null);
//        reforgeProcessPlus(items, model, startTime, null,86683, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true,86145, false, true, new StatBlock(285+80+120,0,0,165,160,160+160,0,0,0));
//        reforgeProcessPlus(items, model, startTime, null, 86145, false, true, null);
//        reforgeProcessPlusPlus(model, startTime, 81251, 81694);
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.bagItemsArray(model, new int[]{77530,89075,81262,87607,89823}));
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.bagItemsArray(model, new ArrayList<>()));
//          reforgeProcessPlusMany(items, model, startTime, new CostedItem[]{new CostedItem(86683,0), new CostedItem(86682,0), new CostedItem(86662,0)});
//            reforgeProcessRetFixed(model, startTime, true);
//            reforgeProcessRetFixedAlone(model, startTime, true);
        reforgeProcessRetChallenge(model, startTime);

//                        findUpgradeSetup(items, strengthPlateMsvArray(), model, true, StatBlock.of(Hit, 200, Expertise, 200));
//                findUpgradeSetup(items, strengthPlateValorArray(), model);
//        new FindUpgrades(itemCache, model, true).run(items, strengthPlateValorCelestialRet(itemCache), null);
//        new FindUpgrades(itemCache, model, true).run(items, strengthPlateValorCelestialRet(itemCache), null);

//        new FindUpgrades(itemCache, model, true).findUpgradeSetup(items, new Tuple.Tuple2[] { Tuple.create(84950,0)});
//        findUpgradeSetup(items, strengthPlateCurrentItemsProt(model), model);
//        findUpgradeSetup(items, bagItemsArray(ignoredItems), model, true, null);
//                findUpgradeSetup(items, strengthPlateCrafted(), model);

//        combinationDumb(items, model, startTime);
    }

    private void reforgeProt(Instant startTime) {
//        ModelCombined model = ModelCombined.damageProtModel();
//        Path file = DataLocation.gearProtFile;

        ModelCombined model = ModelCombined.defenceProtModel();
        Path file = DataLocation.gearProtDefenceFile;

//        Map<Integer, ReforgeRecipe> commonItems = commonFixedItems();
        Map<Integer, ReforgeRecipe> commonItems = null;

        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, file, model.reforgeRules(), commonItems);

//        reforgeProcess(items, model, startTime);
//        reforgeProcess2(items, model, startTime);
//        reforgeProcessProtFixedPlus(model, startTime, 86753, false, true);
//        reforgeProcessProtFixed(model, startTime, true);
//        reforgeProcessProtFixed2(model, startTime, true);
//        reforgeProcessPlus(items, model, startTime, null,86683, false, true, null);
//        reforgeProcessPlus(items, model, startTime, null, 86219, false, true, StatBlock.of(Expertise, 170, Primary, -170));
//        reforgeProcessPlusPlus(items, model, startTime, 85320, 85323, StatBlock.of(Expertise, 320, Primary, -320));
//          reforgeProcessPlusPlus(items, model, startTime, 86753, 89075, false, null);
//        reforgeProcessPlusMany(items, model, startTime, strengthPlateCurrentItemsRet(model));
        reforgeProcessPlusMany(items, model, startTime, new CostedItem[]{new CostedItem(86683, 0), new CostedItem(86682, 0), new CostedItem(86662, 0)});

//        findUpgradeSetup(items, strengthPlateCurrentItemsRet(model), model);
//        new FindUpgrades(itemCache).findUpgradeSetup(model, items, bagItemsArray(model, ignoredItems));
//        findUpgradeSetup(items, ArrayUtil.concat(strengthPlateMsvArray(), strengthPlateMsvHeroicArray()), model, true, null);
//        findUpgradeSetup(items, ArrayUtil.concat(strengthPlateMsvArray(), strengthPlateMsvHeroicArray(), strengthPlateHeartOfFear(), strengthPlateHeartOfFearHeroic()), model, true, null);
//        findUpgradeSetup(items, strengthPlateMsvArray(), model, false);
//        findUpgradeSetup(items, strengthPlateMsvHeroicArray(), model, false);
//        findUpgradeSetup(items, strengthPlateHeartOfFearHeroic(), model, true);
//        findUpgradeSetup(items, strengthPlateHeartOfFear(), model, false, StatBlock.of(Hit, 200, Expertise, 400));
//        findUpgradeSetup(items, strengthPlateValorArray(), model);
//        findUpgradeSetup(items, bagItemsArray(ignoredItems), model, true, null);
//        new FindUpgrades(itemCache, model, true).run(items, strengthPlateValorCelestialTank(itemCache), null);
//        new FindUpgrades(itemCache, model, true).run(items, strengthPlateCrafted());

//        new FindUpgrades(itemCache, model, true).findUpgradeSetup(items, new Tuple.Tuple2[] { Tuple.create(84950,0)});
//                reforgeProcessPlus(items, model, startTime, true,86751, true, true, null);

        // so we could get a conclusive result from the ret, then set the common slots to fixed
    }

    private void reforgeBoom(Instant startTime) {
        ModelCombined model = ModelCombined.standardBoomModel();
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearBoomFile, model.reforgeRules(), null);

        reforgeProcess(items, model, startTime);
//        reforgeProcessPlus(items, model, startTime, null, 90429, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true, SlotEquip.Ring2,89968, false, true, null, null);
//        reforgeProcessPlusPlus(items, model, startTime, 90410, 84833, false, null);
//        new FindUpgrades(itemCache, model, true).run(items, new Tuple.Tuple2[]{Tuple.create(89089,0)});

//       new FindUpgrades(itemCache, model, true).run(items, intellectLeatherValorCelestial(itemCache), null);

//       Jobs.rankAlternativeCombos(items, model, startTime, List.of(
//               List.of(81140),
//               List.of(81079),
//               List.of(81288),
//               List.of(81691),
////               List.of(86806, 89426),
////               List.of(86806, 86829),
//               List.of(81253, 89426),
////               List.of(81253, 86829),
//               List.of(89392, 89426)
////               List.of(89392, 86829)
//       ));

//        findUpgradeSetup(items, intellectLeatherCelestialArray(), model, 476);
//        findUpgradeSetup(items, intellectLeatherValorArray(), model, null);
    }

    private void reforgeBear(Instant startTime) {
        ModelCombined model = ModelCombined.standardBearModel();
//        ItemUtil.forceReload(itemCache, DataLocation.gearBearFile);
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearBearFile, model.reforgeRules(), null);

        reforgeProcess(items, model, startTime);
//        findUpgradeSetup(items, ArrayUtil.concat(SourcesOfItems.agilityLeatherCelestialArray(), SourcesOfItems.agilityLeatherValorArray()), model, true, null);
    }

    private void reforgeWarlock(Instant startTime) {
        ModelCombined model = ModelCombined.standardWarlockModel();
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearWarlockFile, model.reforgeRules(), null);

        reforgeProcess(items, model, startTime);
//        new FindUpgrades(itemCache, model, true).groupBySlot().run(items, intellectClothValorCelestialP1Array());

//               Jobs.rankAlternativeCombos(items, model, startTime, List.of(
//               List.of(90462),
////               List.of(81079),
////               List.of(86806, 86829),
//               List.of(82826, 90105)
//       ));
    }

    private static Map<Integer, ReforgeRecipe> commonFixedItems() {
        Map<Integer, ReforgeRecipe> map = new HashMap<>();
        // 5/9/2025
        map.put(89280, new ReforgeRecipe(Crit, Haste));
        map.put(85991, new ReforgeRecipe(null, null));
        map.put(89346, new ReforgeRecipe(Dodge, Haste));
        map.put(84807, new ReforgeRecipe(Crit, Expertise));
        map.put(85323, new ReforgeRecipe(Parry, Haste));
        map.put(89934, new ReforgeRecipe(null, null));
        map.put(86794, new ReforgeRecipe(Hit, Expertise));
        map.put(86852, new ReforgeRecipe(Hit, Expertise));
        map.put(89069, new ReforgeRecipe(Crit, Haste));
        map.put(90862, new ReforgeRecipe(null, null));
        map.put(86802, new ReforgeRecipe(null, null));
        map.put(86219, new ReforgeRecipe(Hit, Haste));
        return map;
    }

    private static EnumMap<SlotEquip, ReforgeRecipe> commonFixedItemsOld() {
        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(Crit, StatType.Hit));
        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(StatType.Hit, Expertise));
        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(Crit, Expertise));
        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(Dodge, Expertise));
//        presetReforge.put(SlotEquip.Leg, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(Mastery, Expertise));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(Haste, StatType.Hit));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));
        return presetReforge;
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessProtFixed(ModelCombined model, Instant startTime, boolean detailedOutput) {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        Map<Integer, ReforgeRecipe> presetReforge = commonFixedItems();

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null);
        outputResultSimple(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    private void reforgeProcessProtFixed2(ModelCombined model, Instant startTime, boolean detailedOutput) {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        Map<Integer, ReforgeRecipe> presetReforge = new HashMap<>();
//        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(Hit, Expertise));
        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(Crit, Expertise));
//        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(Haste, Hit));
//        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(Dodge, Expertise));
//        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(Mastery, Expertise));
//        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(Haste, StatType.Hit));
//        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
//        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null);
        outputResultSimple(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    private void reforgeProcessProtFixedPlus(ModelCombined model, Instant startTime, int extraItemId, boolean replace, boolean defaultEnchants) {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, true);

        Map<Integer, ReforgeRecipe> presetReforge = commonFixedItems();
        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Function<ItemData, ItemData> enchanting = defaultEnchants ? x -> ItemUtil.defaultEnchants(x, model, true) : Function.identity();

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        extraItem = addExtra(map, model, extraItemId, extraItem.slot.toSlotEquip(), enchanting, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null);
        outputResultSimple(bestSet, model, true);
        outputTweaked(bestSet, map, model);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetFixed(ModelCombined model, Instant startTime, boolean detailedOutput) {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        Map<Integer, ReforgeRecipe> presetReforge = commonFixedItems();

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, null, null, null);

        outputResultSimple(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    private void reforgeProcessRetFixedAlone(ModelCombined model, Instant startTime, boolean detailedOutput) {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        Map<Integer, ReforgeRecipe> presetReforge = new HashMap<>();
//        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(Crit, Haste));
//        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(Haste, Expertise));
//        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(Haste, Expertise));
//        //        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(null, null));
////        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(StatType.Dodge, Expertise));
//        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(StatType.Mastery, Expertise));
//        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(Haste, StatType.Hit));
//        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
//        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, null, null, null);

        outputResultSimple(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetChallenge(ModelCombined model, Instant startTime) {
        // CHALLENGE MODE SET

        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
        List<ItemData> inputSetItems = ItemUtil.loadItems(itemCache, itemIds, true);

        OutputText.println("FINDING EXPECTED REFORGE IN RAID RET");
        EquipOptionsMap raidMap = ItemUtil.standardItemsReforgedToMap(model.reforgeRules(), inputSetItems);
        ItemSet raidSet = chooseEngineAndRun(model, raidMap, null, null, null).orElseThrow();
        OutputText.println("FOUND REFORGE RAID RET");
        outputResultSimple(Optional.of(raidSet), model, false);

        Map<Integer, ReforgeRecipe> presetReforge = commonFixedItems();
        raidSet.getItems().forEachValue(item -> presetReforge.put(item.id, item.reforge));

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), inputSetItems, presetReforge);

        // compared to raid dps
        int[] extraItems = new int[]{
                81129, 89665, 90910, 81268, 81138, 77539, 85991,
                81098, 89503, 82975, 81694, 86799, 82856, 86682,
                86794, 89954, 81130, 87060, 82812, 86145, 86680,
                81284, 86852, 86742, 88862, 81113, 90862, 81251,
                89526, 82822, 82814
        };

        Function<ItemData, ItemData> customiseItem = extraItem -> {
//            if (extraItem.id == 82812) { // Pyretic Legguards
//                return extraItem.changeFixed(new StatBlock(285, 0, 0, 165, 160, 160 + 60, 0, 0, 0, 0));
//            } else if (extraItem.id == 81284) { // Anchoring Sabatons
//                return extraItem.changeFixed(new StatBlock(60 + 60, 0, 140, 0, 0, 120, 0, 0, 0, 0));
//            } else if (extraItem.id == 81113) { // Spike-Soled Stompers
//                return extraItem.changeFixed(new StatBlock(60, 0, 0, 0, 160, 175 + 160, 0, 0, 0, 0));
            if (extraItem.id == 87060) { // Star-Stealer Waistguard
                return extraItem.changeFixed(new StatBlock(0, 0, 0, 0, 160, 320 * 2 + 160, 0, 120, 0, 0));
//            } else if (extraItem.id == 86794) { // starcrusher gauntlets
//                return extraItem.changeFixed(new StatBlock(170, 0, 0, 0, 160, 60 + 320 + 160, 0, 0, 0, 0));
//            } else if (extraItem.id == 86145) { // jang-xi devastating legs
//                return extraItem.changeFixed(new StatBlock(120, 430, 0, 0, 160, 160 * 2, 160, 0, 0, 0));
            } else if (extraItem.id == 89503) { // Greenstone Drape
                return extraItem.changeStats(new StatBlock(501, 751, 0, 334, 334, 0, 0, 0, 0, 0))
                        .changeFixed(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
            } else if (extraItem.slot == SlotItem.Back) {
                return extraItem.changeFixed(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
            } else {
                OutputText.println("DEFAULT ENCHANT " + extraItem);
                return ItemUtil.defaultEnchants(extraItem, model, false);
            }
        };

        for (int extraId : extraItems) {
            ReforgeRecipe reforge = null;
            if (presetReforge.containsKey(extraId))
                reforge = presetReforge.get(extraId);
            ItemData extraItem = addExtra(map, model, extraId, customiseItem, reforge, false, false, false);
            if (extraItem != null)
                OutputText.println("EXTRA " + extraItem);
        }

        EquipOptionsMap scaledMap = ItemLevel.scaleForChallengeMode(map);

        JobInfo job = new JobInfo();
        job.printRecorder.outputImmediate = true;
        job.config(model, scaledMap, startTime, BILLION, null);
        job.forceRandom = true;
        Solver.runJob(job);
        ItemSet bestScaledSet = job.resultSet.orElseThrow();

        OutputText.println("SCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALED");
        outputResultSimple(Optional.of(bestScaledSet), model, true);

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

        OutputText.println("FINALFINALFINALFINALFINALFINALFINALFINALFINALFINALFINAL");
        outputResultSimple(bestSetFinal, model, true);
    }

    private void multiSpecSpecifiedRating() {
        ModelCombined modelRet = ModelCombined.standardRetModel();
        ModelCombined modelProt = ModelCombined.defenceProtModel();

        OutputText.println("RET GEAR CURRENT");
        List<ItemData> retItems = ItemUtil.loadItems(itemCache, InputGearParser.readInput(DataLocation.gearRetFile), true);
        OutputText.println("PROT GEAR CURRENT");
        List<ItemData> protItems = ItemUtil.loadItems(itemCache, InputGearParser.readInput(DataLocation.gearProtFile), true);

        Map<SlotEquip, ReforgeRecipe> reforgeRet = new EnumMap<>(SlotEquip.class);
        reforgeRet.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        reforgeRet.put(SlotEquip.Neck, new ReforgeRecipe(Crit, Expertise));
        reforgeRet.put(SlotEquip.Shoulder, new ReforgeRecipe(Expertise, Haste));
        reforgeRet.put(SlotEquip.Back, new ReforgeRecipe(Crit, Expertise));
        reforgeRet.put(SlotEquip.Chest, new ReforgeRecipe(Crit, Expertise));
        reforgeRet.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Hit, Haste));
        reforgeRet.put(SlotEquip.Hand, new ReforgeRecipe(Crit, StatType.Hit));
        reforgeRet.put(SlotEquip.Belt, new ReforgeRecipe(StatType.Mastery, Expertise));
        reforgeRet.put(SlotEquip.Leg, new ReforgeRecipe(Crit, StatType.Mastery));
        reforgeRet.put(SlotEquip.Foot, new ReforgeRecipe(StatType.Mastery, Expertise));
        reforgeRet.put(SlotEquip.Ring1, new ReforgeRecipe(Crit, Haste));
        reforgeRet.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, StatType.Mastery));
        reforgeRet.put(SlotEquip.Trinket1, new ReforgeRecipe(null, null));
        reforgeRet.put(SlotEquip.Trinket2, new ReforgeRecipe(Expertise, StatType.Mastery));
        reforgeRet.put(SlotEquip.Weapon, new ReforgeRecipe(StatType.Hit, Haste));

        Map<SlotEquip, ReforgeRecipe> reforgeProt = new EnumMap<>(SlotEquip.class);
        reforgeProt.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Neck, new ReforgeRecipe(Crit, Expertise));
        reforgeProt.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Dodge, StatType.Hit));
        reforgeProt.put(SlotEquip.Back, new ReforgeRecipe(Crit, Expertise));
        reforgeProt.put(SlotEquip.Chest, new ReforgeRecipe(Crit, Expertise));
        reforgeProt.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Dodge, StatType.Mastery));
        reforgeProt.put(SlotEquip.Hand, new ReforgeRecipe(StatType.Parry, StatType.Hit));
        reforgeProt.put(SlotEquip.Belt, new ReforgeRecipe(StatType.Dodge, StatType.Hit));
        reforgeProt.put(SlotEquip.Leg, new ReforgeRecipe(StatType.Dodge, StatType.Mastery));
        reforgeProt.put(SlotEquip.Foot, new ReforgeRecipe(StatType.Parry, Expertise));
        reforgeProt.put(SlotEquip.Ring1, new ReforgeRecipe(StatType.Parry, Expertise));
        reforgeProt.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, StatType.Mastery));
        reforgeProt.put(SlotEquip.Trinket1, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Trinket2, new ReforgeRecipe(Expertise, StatType.Mastery));
        reforgeProt.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));

        EquipMap retForgedItems = ItemUtil.chosenItemsReforgedToMap(retItems, reforgeRet);
        ItemSet retSet = ItemSet.manyItems(retForgedItems, null);

        EquipMap protForgedItems = ItemUtil.chosenItemsReforgedToMap(protItems, reforgeProt);
        ItemSet protSet = ItemSet.manyItems(protForgedItems, null);

        retSet.outputSet(modelRet);
        OutputText.println("---------------------" + (modelRet.calcRating(retSet) + modelProt.calcRating(protSet)));
        protSet.outputSet(modelProt);
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        OutputText.println("elapsed = " + duration.toString());
    }
}
