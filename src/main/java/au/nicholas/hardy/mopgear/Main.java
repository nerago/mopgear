package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.io.DataLocation;
import au.nicholas.hardy.mopgear.io.InputGearParser;
import au.nicholas.hardy.mopgear.io.ItemCache;
import au.nicholas.hardy.mopgear.io.SourcesOfItems;
import au.nicholas.hardy.mopgear.model.ItemLevel;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.model.StatRequirements;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static au.nicholas.hardy.mopgear.EngineUtil.chooseEngineAndRun;
import static au.nicholas.hardy.mopgear.Jobs.*;
import static au.nicholas.hardy.mopgear.domain.StatType.*;
import static au.nicholas.hardy.mopgear.io.SourcesOfItems.intellectLeatherCelestialArray;
import static au.nicholas.hardy.mopgear.io.SourcesOfItems.intellectLeatherValorArray;

@SuppressWarnings({"CallToPrintStackTrace", "ThrowablePrintedToSystemOut", "SameParameterValue", "unused", "OptionalUsedAsFieldOrParameterType", "ConstantValue"})
public class Main {

    public static final long BILLION = 1000 * 1000 * 1000;

    public ItemCache itemCache;

    public static void main(String[] arg) throws IOException, ExecutionException, InterruptedException {
        new Main().run();
    }

    private void run() throws IOException, ExecutionException, InterruptedException {
        itemCache = new ItemCache(DataLocation.cacheFile);
        Jobs.itemCache = itemCache;

        Instant startTime = Instant.now();

        try (ForkJoinPool myPool = new ForkJoinPool(12, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false, 20, 256, 10, null, 60, TimeUnit.SECONDS)) {
            myPool.submit(() -> exceptionalCheck(startTime)).get();
        }

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void exceptionalCheck(Instant startTime) {
        try {
//            WowHead.fetchItem(86145);

//            multiSpecSequential(startTime);

//            reforgeRet(startTime);
            reforgeProt(startTime);
//            reforgeBoom(startTime);
//        rankSomething();
//        multiSpecReforge(startTime);
//            multiSpecSpecifiedRating();
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

        EnumMap<SlotEquip, ReforgeRecipe> commonItems = commonFixedItems();
//        EnumMap<SlotEquip, ReforgeRecipe> commonItems = null;

        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearRetFile, model.reforgeRules(), commonItems);

//        reforgeProcess(items, model, startTime, true);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);

//        reforgeProcessPlus(items, model, startTime, true, 89981, true, true, null);
//        reforgeProcessPlus(items, model, startTime, true,86145, false, true, new StatBlock(285+80+120,0,0,165,160,160+160,0,0,0));
//        reforgeProcessPlus(items, model, startTime, true, 89280, false, true, null);
//        reforgeProcessPlusPlus(model, startTime, 81251, 81694);
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.bagItemsArray(model, new int[]{77530,89075,81262,87607,89823}));
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.bagItemsArray(model, new ArrayList<>()));
            reforgeProcessRetFixed(model, startTime, true);
//            reforgeProcessRetFixedAlone(model, startTime, true);
//        reforgeProcessRetChallenge(model, startTime);

//                        findUpgradeSetup(items, strengthPlateMsvArray(), model);
//                findUpgradeSetup(items, strengthPlateValorArray(), model);
//        new FindUpgrades(itemCache).findUpgradeSetup(model, items, strengthPlateValorCelestialP1(itemCache));
//        findUpgradeSetup(items, strengthPlateCurrentItemsProt(model), model);
//        findUpgradeSetup(items, bagItemsArray(model, ignoredItems), model);
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
//        reforgeProcessProtFixed2(model, startTime, true);
//        reforgeProcessPlus(items, model, startTime, true,84950, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true,85991, false, true, null);
//        reforgeProcessPlusPlus(items, model, startTime, 89280, 86752);
//        reforgeProcessPlusMany(items, model, startTime, strengthPlateCurrentItemsRet(model));

//        findUpgradeSetup(items, strengthPlateCurrentItemsRet(model), model);
//        new FindUpgrades(itemCache).findUpgradeSetup(model, items, bagItemsArray(model, ignoredItems));
//        findUpgradeSetup(items, strengthPlateMsvHeroicArray(), model);
//        findUpgradeSetup(items, strengthPlateHeartOfFearHeroic(), model);
//        findUpgradeSetup(items, strengthPlateHeartOfFear(), model);
//        findUpgradeSetup(items, strengthPlateValorArray(), model);
//        findUpgradeSetup(items, strengthPlateCrafted(), model);
//        new FindUpgrades(itemCache).findUpgradeSetup(model, items, strengthPlateValorCelestialP1(itemCache));

        new FindUpgrades(itemCache).findUpgradeSetup(model, items, new Tuple.Tuple2[] { Tuple.create(86751,0)});
//                reforgeProcessPlus(items, model, startTime, true,86751, true, true, null);

        // so we could get a conclusive result from the ret, then set the common slots to fixed
    }

    private void reforgeBoom(Instant startTime) throws IOException {
        ModelCombined model = ModelCombined.standardBoomModel();
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearBoomFile, model.reforgeRules(), null);

//        reforgeProcess(items, model, startTime, true);
//        reforgeProcessPlus(items, model, startTime, true, 86783, false, true, null);

        Tuple.Tuple2<Integer, Integer>[] filteredCelestialArray = SourcesOfItems.filterItemLevel(itemCache, intellectLeatherCelestialArray(), 476);
       new FindUpgrades(itemCache).findUpgradeSetup(model, items, ArrayUtil.concat(filteredCelestialArray, intellectLeatherValorArray()));

//        findUpgradeSetup(items, intellectLeatherCelestialArray(), model, 476);
//        findUpgradeSetup(items, intellectLeatherValorArray(), model, null);
    }

    private static EnumMap<SlotEquip, ReforgeRecipe> commonFixedItems() {
        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(Crit, Hit));
        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(Hit, Expertise));
        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(Crit, Expertise));
        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(Haste, Expertise));
        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(Dodge, Expertise));
//        presetReforge.put(SlotEquip.Leg, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(Mastery, Expertise));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(Haste, Hit));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));
        return presetReforge;
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessProtFixed(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = commonFixedItems();

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null, null);
        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    private void reforgeProcessProtFixed2(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
//        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(Hit, Expertise));
        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(Crit, Expertise));
//        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(Haste, Hit));
//        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(Dodge, Expertise));
//        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(Mastery, Expertise));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(Haste, Hit));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null, null);
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

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null, null);
        outputResult(bestSet, model, true);
        outputTweaked(bestSet, map, model);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetFixed(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = commonFixedItems();

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, null, null, null, null);

        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    private void reforgeProcessRetFixedAlone(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        EnumMap<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(Crit, Haste));
        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(Haste, Expertise));
        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(Haste, Expertise));
        //        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(Haste, Expertise));
        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(Dodge, Expertise));
        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(Mastery, Expertise));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(Haste, Hit));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));

        EquipOptionsMap map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, null, null, null, null);

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

        ItemSet bestScaledSet = chooseEngineAndRun(model, scaledMap, startTime, null, null, null).orElseThrow();

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
        Optional<ItemSet> bestSetFinal = chooseEngineAndRun(finalModel, map, startTime, null, null, null);

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
        ItemSet retSet = ItemSet.manyItems(retForgedItems, null, null);

        EquipMap protForgedItems = ItemUtil.chosenItemsReforgedToMap(protItems, reforgeProt);
        ItemSet protSet = ItemSet.manyItems(protForgedItems, null, null);

        retSet.outputSet(modelRet);
        System.out.println("---------------------" + (modelRet.calcRating(retSet) + modelProt.calcRating(protSet)));
        protSet.outputSet(modelProt);
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
