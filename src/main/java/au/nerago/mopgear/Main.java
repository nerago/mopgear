package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.DataLocation;
import au.nerago.mopgear.io.InputGearParser;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.io.ReadLog;
import au.nerago.mopgear.model.ItemLevel;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirementsHitExpertise;
import au.nerago.mopgear.permute.Solver;
import au.nerago.mopgear.process.FindUpgrades;
import au.nerago.mopgear.results.JobInput;
import au.nerago.mopgear.results.JobOutput;
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

import static au.nerago.mopgear.Tasks.*;
import static au.nerago.mopgear.domain.StatType.*;
import static au.nerago.mopgear.io.SourcesOfItems.*;
import static au.nerago.mopgear.permute.Solver.chooseEngineAndRun;

@SuppressWarnings({"SameParameterValue", "unused", "ConstantValue"})
public class Main {
    public static final long BILLION = 1000 * 1000 * 1000;

    public static void main(String[] arg) throws IOException {
        try {
            new Main().run();
        } catch (Throwable ex) {
            OutputText.printException(ex);
        }
        OutputText.finish();
    }

    private void run() throws ExecutionException, InterruptedException {
        Instant startTime = Instant.now();

        try (ForkJoinPool myPool = new ForkJoinPool(12, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false, 20, 256, 10, null, 60, TimeUnit.SECONDS)) {
            myPool.submit(() -> launchpad(startTime)).get();
        }

        printElapsed(startTime);

        ItemCache.instance.cacheSave();
    }

    private void launchpad(Instant startTime) {
        new ReadLog().run();
//
//        determineRatingMultipliers();
//        paladinMultiSpecSolve();
//        druidMultiSpecSolve();

//        reforgeRet(startTime);
//            reforgeProt(startTime);
//            reforgeBoom(startTime);
//        reforgeTree(startTime);
//                    reforgeBear(startTime);
//            reforgeWarlock(startTime);
//        rankSomething();
//        multiSpecReforge(startTime);
//            multiSpecSpecifiedRating();
    }

    private void reforgeRet(Instant startTime) {
//        ModelCombined model = ModelCombined.standardRetModel();
//        ModelCombined model = ModelCombined.extendedRetModel(true, true);
        ModelCombined model = ModelCombined.extendedRetModel(true, true);
//        ModelCombined model = ModelCombined.priorityRetModel();

//        Map<Integer, List<ReforgeRecipe>> commonItems = commonFixedItems();
        Map<Integer, List<ReforgeRecipe>> commonItems = null;

        EquipOptionsMap items = ItemLoadUtil.readAndLoad(true, DataLocation.gearRetFile, model.reforgeRules(), commonItems);

//        reforgeProcess(items, model, startTime);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);
//        reforgeProcessPlus(items, model, startTime, SlotEquip.Ring2,86880, 2, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true, 89981, true, true, null);
//        reforgeProcessPlus(items, model, startTime, null,86683, false, true, null);
//        reforgeProcessPlus(items, model, startTime, null,87015, 2, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true,86145, false, true, new StatBlock(285+80+120,0,0,165,160,160+160,0,0,0));
//        reforgeProcessPlus(items, model, startTime, null, 86145, false, true, null);
//        reforgeProcessPlusPlus(items, model, startTime, 87110, 87100, false, null);
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.bagItemsArray(model, new int[]{77530,89075,81262,87607,89823}));
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.bagItemsArray(model, new ArrayList<>()));
//          reforgeProcessPlusMany(items, model, startTime, new CostedItem[]{
//                  new CostedItem(84950,0),
//                  new CostedItem(89954,0),
////                  new CostedItem(86822,0)
//          }, 0);

//            reforgeProcessRetFixed(model, startTime, true);
//            reforgeProcessRetFixedAlone(model, startTime, true);
//        reforgeProcessRetChallenge(model, startTime);

//        compareBestReforgesWithCommon(DataLocation.gearRetFile, model, commonFixedItems(), null);

//                        findUpgradeSetup(items, strengthPlateMsvArray(), model, true, StatBlock.of(Hit, 200, Expertise, 200));
//                findUpgradeSetup(items, strengthPlateValorArray(), model);
//        new FindUpgrades(model, true).runMaxedItems(items, strengthPlateValorCelestialRet(), null);
//        new FindUpgrades(model, true).runMaxedItems(items, new CostedItem[]{new CostedItem(86905, 0)}, null);

//        new FindUpgrades(itemCache, model, true).findUpgradeSetup(items, new Tuple.Tuple2[] { Tuple.create(84950,0)});
//        findUpgradeSetup(items, strengthPlateCurrentItemsProt(model), model);
//        findUpgrade(items, bagItemsArray(ignoredItems), model, true, null);
        findUpgrade(items, ArrayUtil.concat(new CostedItem[][]{strengthPlateMsvArray(), strengthPlateMsvHeroicArray(), strengthPlateHeartOfFear(), strengthPlateHeartOfFearHeroic(), strengthPlateTerrace(), strengthPlateTerraceHeroic()}), model, true, null, 2);

//                findUpgradeSetup(items, strengthPlateCrafted(), model);
    }

    private void reforgeProt(Instant startTime) {
        ModelCombined model = ModelCombined.damageProtModel();
        Path file = DataLocation.gearProtDpsFile;

//        ModelCombined model = ModelCombined.defenceProtModel();
//        Path file = DataLocation.gearProtDefenceFile;

//        Map<Integer, List<ReforgeRecipe>> commonItems = commonFixedItems();
        Map<Integer, List<ReforgeRecipe>> commonItems = null;

        EquipOptionsMap items = ItemLoadUtil.readAndLoad(true, file, model.reforgeRules(), commonItems);

//        reforgeProcess(items, model, startTime);
//        reforgeProcessPlus(items, model, startTime, SlotEquip.Trinket2,79327, false, true, null);
//        reforgeProcessProtFixedPlus(model, startTime, 86753, false, true);
//        reforgeProcessProtFixed(model, startTime, true);
//        reforgeProcessProtFixed2(model, startTime, true);
//        reforgeProcessPlus(items, model, startTime, null,87015, 2, false, true, null);
//        reforgeProcessPlus(items, model, startTime, null, 86219, false, true, StatBlock.of(Expertise, 170, Primary, -170));
//        reforgeProcessPlusPlus(items, model, startTime, 85320, 85323, StatBlock.of(Expertise, 320, Primary, -320));
//          reforgeProcessPlusPlus(items, model, startTime, 86680, 86682, 0, false, null);
//        reforgeProcessPlusPlus(items, model, startTime, 87110, 87100, false, null);
//        reforgeProcessPlusMany(items, model, startTime, strengthPlateCurrentItemsRet(model));
//        reforgeProcessPlusMany(items, model, startTime, bagItemsArray(ignoredItems));
//        reforgeProcessPlusMany(items, model, startTime, new CostedItem[]{new CostedItem(87110, 0), new CostedItem(87100, 0), new CostedItem(86661, 0)});

//        findUpgradeSetup(items, strengthPlateCurrentItemsRet(model), model);
//        findUpgradeSetup(items, ArrayUtil.concat(strengthPlateHeartOfFear()), model, true, null);
        findUpgrade(items, ArrayUtil.concat(new CostedItem[][]{strengthPlateMsvArray(), strengthPlateMsvHeroicArray(), strengthPlateHeartOfFear(), strengthPlateHeartOfFearHeroic(), strengthPlateTerrace(), strengthPlateTerraceHeroic()}), model, true, null, 2);
//        findUpgradeSetup(items, ArrayUtil.concat(strengthPlateHeartOfFear(), strengthPlateHeartOfFearHeroic()), model, true, null);
//        findUpgrade(items, ArrayUtil.concat(strengthPlateTerrace(), strengthPlateTerraceHeroic()), model, true, null, 2);
//        findUpgrade(items, ArrayUtil.concat(strengthPlateTerrace(), strengthPlateMsvHeroicArray()), model, true, null, 2);
//        findUpgradeSetup(items, strengthPlateMsvArray(), model, false);
//        findUpgradeSetup(items, strengthPlateMsvHeroicArray(), model, false);
//        findUpgrade(items, strengthPlateHeartOfFearHeroic(), model, true, null, 2);
//        findUpgradeSetup(items, strengthPlateHeartOfFear(), model, false, StatBlock.of(Hit, 200, Expertise, 400));
//        findUpgrade(items, strengthPlateTerrace(), model, true, null, 2);
//        findUpgrade(items, bagItemsArray(ignoredItems), model, true, null);
//        findUpgradeMaxedItems(items, bagItemsArray(ignoredItems), model, true, null);
//        new FindUpgrades(model, true).runMaxedItems(items, strengthPlateValorCelestialTank(), null);
//        new FindUpgrades(model, true).run(items, strengthPlateCurrentItemsProtAllUpgradable(), null, 2);
//        new FindUpgrades(itemCache, model, true).run(items, strengthPlateCrafted());

//        new FindUpgrades(itemCache, model, true).findUpgradeSetup(items, new Tuple.Tuple2[] { Tuple.create(84950,0)});
//                reforgeProcessPlus(items, model, startTime, true,86751, true, true, null);

//        compareBestReforgesWithCommon(file, model, commonFixedItems(), null);

//        CostedItem[] allTheGoodShit = ArrayUtil.concat(
//                strengthPlateValorCelestialTank(itemCache),
//                strengthPlateMsvHeroicArray(),
//                strengthPlateHeartOfFearHeroic(),
//                strengthPlateTerraceHeroic(),
//                new CostedItem[]{new CostedItem(90862, 0)}, // quest ring
//                new CostedItem[]{new CostedItem(79327, 0)}, // darkmoon dps
//                new CostedItem[]{new CostedItem(84910, 0)} // pvp shield
//        );
//        findBIS(model, allTheGoodShit, startTime);
//        findBestBySlot(model, allTheGoodShit, startTime);
    }

    private void reforgeBoom(Instant startTime) {
        ModelCombined model = ModelCombined.standardBoomModel();
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(true, DataLocation.gearBoomFile, model.reforgeRules(), null);

//        reforgeProcess(items, model, startTime);
//        reforgeProcessPlus(items, model, startTime, null, 90429, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true, SlotEquip.Ring2,89968, false, true, null, null);
//        reforgeProcessPlusPlus(items, model, startTime, 90410, 84833, false, null);
//        new FindUpgrades(itemCache, model, true).run(items, new Tuple.Tuple2[]{Tuple.create(89089,0)});

       new FindUpgrades(model, true).run(items, intellectLeatherValorCelestial(), null, 0);

//        new FindUpgrades(model, true).run(items, bagItemsArray(ignoredItems), null);

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

    private void reforgeTree(Instant startTime) {
        ModelCombined model = ModelCombined.standardTreeModel();
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(true, DataLocation.gearTreeFile, model.reforgeRules(), null);

//        reforgeProcess(items, model, startTime);
//        reforgeProcessPlus(items, model, startTime, null, 90429, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true, SlotEquip.Ring2,89968, false, true, null, null);
//        reforgeProcessPlusPlus(items, model, startTime, 90410, 84833, false, null);
//        new FindUpgrades(itemCache, model, true).run(items, new Tuple.Tuple2[]{Tuple.create(89089,0)});

       new FindUpgrades(model, true).run(items, intellectLeatherValorCelestial(), null, 0);

//        new FindUpgrades( model, true).run(items, bagItemsArray(ignoredItems), null);

//        findUpgradeSetup(items, intellectLeatherCelestialArray(), model, 476);
//        findUpgradeSetup(items, intellectLeatherValorArray(), model, null);
    }

    private void reforgeBear(Instant startTime) {
        ModelCombined model = ModelCombined.standardBearModel();
//        ItemUtil.forceReload(itemCache, DataLocation.gearBearFile);
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(true, DataLocation.gearBearFile, model.reforgeRules(), null);

        reforgeProcess(items, model, startTime);
//        findUpgradeSetup(items, ArrayUtil.concat(SourcesOfItems.agilityLeatherCelestialArray(), SourcesOfItems.agilityLeatherValorArray()), model, true, null);
    }

    private void reforgeWarlock(Instant startTime) {
        ModelCombined model = ModelCombined.standardWarlockModel();
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(true, DataLocation.gearWarlockFile, model.reforgeRules(), null);

//        reforgeProcess(items, model, startTime);
        new FindUpgrades(model, true).run(items, intellectClothValorCelestialP1Array(), null, 0);

//               Jobs.rankAlternativeCombos(items, model, startTime, List.of(
//               List.of(90462),
////               List.of(81079),
////               List.of(86806, 86829),
//               List.of(82826, 90105)
//       ));
    }

    private static Map<Integer, List<ReforgeRecipe>> commonFixedItems() {
        Map<Integer, List<ReforgeRecipe>> map = new HashMap<>();
        // 5/10/2025
        map.put(86979, List.of(new ReforgeRecipe(Hit, Expertise))); // Foot Impaling Treads (Hit->Expertise)
        map.put(87036, List.of(new ReforgeRecipe(null, null))); // Neck Soulgrasp Choker
        map.put(85339, List.of(new ReforgeRecipe(Hit, Expertise))); // Shoulder White Tiger Pauldrons (Hit->Expertise)
        map.put(89934, List.of(new ReforgeRecipe(null, null))); // Wrist Bonded Soul Bracers
        map.put(90862, List.of(new ReforgeRecipe(Expertise, Hit))); // Ring Seal of the Bloodseeker (Expertise->Hit)
        map.put(87100, List.of(new ReforgeRecipe(Crit, Haste))); // Hand White Tiger Gauntlets (Crit->Haste)
        map.put(87024, List.of(new ReforgeRecipe(null, null))); // Head Nullification Greathelm
        map.put(87026, List.of(new ReforgeRecipe(Crit, Haste))); // Back Cloak of Peacock Feathers (Crit->Haste)
        map.put(79327, List.of(new ReforgeRecipe(null, null))); // Trinket Relic of Xuen
        map.put(86802, List.of(new ReforgeRecipe(Haste, Expertise))); // Trinket Lei Shen's Final Orders (Haste->Expertise)
        map.put(86906, List.of(new ReforgeRecipe(Mastery, Haste))); // Weapon1H Kilrak, Jaws of Terror (Mastery->Haste)
        map.put(86683, List.of(new ReforgeRecipe(Haste, Hit))); // Chest White Tiger Battleplate (Haste->Hit)
        map.put(86680, List.of(new ReforgeRecipe(Mastery, Haste))); // Leg White Tiger Legplates (Mastery->Haste)
        map.put(86955, List.of(new ReforgeRecipe(Mastery, Expertise))); // Belt Waistplate of Overwhelming Assault (Mastery->Expertise)
        return map;
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetChallenge(ModelCombined model, Instant startTime) {
        // CHALLENGE MODE SET

        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
        List<FullItemData> inputSetItems = ItemLoadUtil.loadItems(itemIds, true);

        OutputText.println("FINDING EXPECTED REFORGE IN RAID RET");
        EquipOptionsMap raidMap = ItemMapUtil.limitedItemsReforgedToMap(model.reforgeRules(), inputSetItems, commonFixedItems());
        FullItemSet raidSet = chooseEngineAndRun(model, raidMap, null, null).orElseThrow();
        OutputText.println("FOUND REFORGE RAID RET");
        outputResultSimple(Optional.of(raidSet), model, false);

        Map<Integer, List<ReforgeRecipe>> presetReforge = commonFixedItems();
        raidSet.items().forEachValue(item -> presetReforge.put(item.itemId(), Collections.singletonList(item.reforge)));
        presetReforge.put(89954, List.of(new ReforgeRecipe(Crit, Haste)));

        EquipOptionsMap map = ItemMapUtil.limitedItemsReforgedToMap(model.reforgeRules(), inputSetItems, presetReforge);

        // compared to raid dps
        int[] extraItems = new int[]{
                81129, 89665, 90910, 81268, 81138, 77539, 85991,
                81098, 89503, 82975, 81694, 86799, 82856, 86682,
                86794, 89954, 81130, 87060, 82812, 86145, 86680,
                81284, 86852, 86742, 88862, 81113, 90862, 81251,
                89526, 82822, 82814
        };

        Function<FullItemData, FullItemData> customiseItem = extraItem -> {
//            if (extraItem.id == 82812) { // Pyretic Legguards
//                return extraItem.changeFixed(new StatBlock(285, 0, 0, 165, 160, 160 + 60, 0, 0, 0, 0));
//            } else if (extraItem.id == 81284) { // Anchoring Sabatons
//                return extraItem.changeFixed(new StatBlock(60 + 60, 0, 140, 0, 0, 120, 0, 0, 0, 0));
//            } else if (extraItem.id == 81113) { // Spike-Soled Stompers
//                return extraItem.changeFixed(new StatBlock(60, 0, 0, 0, 160, 175 + 160, 0, 0, 0, 0));
            if (extraItem.itemId() == 87060) { // Star-Stealer Waistguard
                return extraItem.changeEnchant(new StatBlock(0, 0, 0, 0, 160, 320 * 2 + 160, 0, 120, 0, 0));
//            } else if (extraItem.id == 86794) { // starcrusher gauntlets
//                return extraItem.changeFixed(new StatBlock(170, 0, 0, 0, 160, 60 + 320 + 160, 0, 0, 0, 0));
//            } else if (extraItem.id == 86145) { // jang-xi devastating legs
//                return extraItem.changeFixed(new StatBlock(120, 430, 0, 0, 160, 160 * 2, 160, 0, 0, 0));
            } else if (extraItem.itemId() == 77539) { // engineer helm
                return extraItem.changeEnchant(new StatBlock(216, 0, 0, 0, 600, 600, 0, 0, 0, 0));
            } else if (extraItem.itemId() == 89503) { // Greenstone Drape
                return extraItem.changeStatsBase(new StatBlock(501, 751, 0, 334, 334, 0, 0, 0, 0, 0))
                        .changeEnchant(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
            } else if (extraItem.slot() == SlotItem.Back) {
                return extraItem.changeEnchant(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
            } else {
                OutputText.println("DEFAULT ENCHANT " + extraItem);
                return ItemLoadUtil.defaultEnchants(extraItem, model, false);
            }
        };

        for (int extraId : extraItems) {
            ReforgeRecipe reforge = null;
            if (presetReforge.containsKey(extraId))
                reforge = presetReforge.get(extraId).getFirst();
            FullItemData extraItem = addExtra(map, model, extraId, 0, customiseItem, reforge, false, false, false);
            if (extraItem != null)
                OutputText.println("EXTRA " + extraItem);
        }

        EquipOptionsMap scaledMap = ItemLevel.scaleForChallengeMode(map);

        JobInput job = new JobInput();
        job.printRecorder.outputImmediate = true;
        job.config(model, scaledMap, startTime, null);
        job.runSizeMultiply = 16;
        job.forceRandom = true;
        job.forcedRunSized = BILLION;
        JobOutput output = Solver.runJob(job);
        FullItemSet bestScaledSet = output.getFinalResultSet().orElseThrow();

        OutputText.println("SCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALED");
        outputResultSimple(Optional.of(bestScaledSet), model, true);

        for (SlotEquip slot : SlotEquip.values()) {
            FullItemData scaledChoice = bestScaledSet.items().get(slot);
            if (scaledChoice != null) {
                FullItemData[] options = map.get(slot);
                boolean inRaidDPSSet = inputSetItems.stream().anyMatch(x -> x.itemId() == scaledChoice.itemId());

                if (inRaidDPSSet) {
                    // need exact item + forge but prescale
                    // note were using id match only, scaled stuff could confused normal "exact" match
                    // avoid engineering heads mixup
                    FullItemData match = ArrayUtil.findOne(options, x -> x.itemId() == scaledChoice.itemId() && Objects.equals(x.reforge, scaledChoice.reforge));
                    options = new FullItemData[]{match};
                } else {
                    options = ArrayUtil.allMatch(options, x -> x.itemId() == scaledChoice.itemId());
                }

                map.put(slot, options);
            }
        }

        ModelCombined finalModel = new ModelCombined(model.statRatings(), StatRequirementsHitExpertise.retWideCapRange(), model.reforgeRules(), model.enchants(), model.setBonus());
        Optional<FullItemSet> bestSetFinal = chooseEngineAndRun(finalModel, map, startTime, null);

        OutputText.println("FINALFINALFINALFINALFINALFINALFINALFINALFINALFINALFINAL");
        outputResultSimple(bestSetFinal, model, true);
    }

    private void multiSpecSpecifiedRating() {
        ModelCombined modelRet = ModelCombined.extendedRetModel(true, true);
        ModelCombined modelProt = ModelCombined.defenceProtModel();

        OutputText.println("RET GEAR CURRENT");
        List<FullItemData> retItems = ItemLoadUtil.loadItems(InputGearParser.readInput(DataLocation.gearRetFile), true);
        OutputText.println("PROT GEAR CURRENT");
        List<FullItemData> protItems = ItemLoadUtil.loadItems(InputGearParser.readInput(DataLocation.gearProtDpsFile), true);

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

        EquipMap retForgedItems = ItemMapUtil.chosenItemsReforgedToMap(retItems, reforgeRet);
        FullItemSet retSet = FullItemSet.manyItems(retForgedItems, null);

        EquipMap protForgedItems = ItemMapUtil.chosenItemsReforgedToMap(protItems, reforgeProt);
        FullItemSet protSet = FullItemSet.manyItems(protForgedItems, null);

        retSet.outputSet(modelRet);
        OutputText.println("---------------------" + (modelRet.calcRating(retSet) + modelProt.calcRating(protSet)));
        protSet.outputSet(modelProt);
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        OutputText.println("elapsed = " + duration.toString());
    }
}
