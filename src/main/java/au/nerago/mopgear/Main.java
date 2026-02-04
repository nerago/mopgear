package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.*;
import au.nerago.mopgear.model.*;
import au.nerago.mopgear.process.FindUpgrades;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.results.UpgradeResultItem;
import au.nerago.mopgear.util.ArrayUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

import static au.nerago.mopgear.Tasks.*;
import static au.nerago.mopgear.domain.StatType.*;
import static au.nerago.mopgear.io.SourcesOfItems.*;

@SuppressWarnings({"SameParameterValue", "unused"})
public class Main {
    static void main(String[] arg) throws IOException {
        try {
            new Main().run();
        } catch (Throwable ex) {
            OutputText.printException(ex);
        }
        OutputText.finish();
    }

    private void run() throws ExecutionException, InterruptedException, IOException {
        Instant startTime = Instant.now();

        lowerPriority();

        try (ForkJoinPool myPool = new ForkJoinPool(12, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false, 0, 64, 1, null, 60, TimeUnit.SECONDS)) {
            myPool.submit(() -> launchpad(startTime)).get();
        }

        printElapsed(startTime);

        ItemCache.instance.cacheSave();
    }

    private void lowerPriority() throws IOException {
        long processId = ProcessHandle.current().pid();
        new ProcessBuilder().command("C:\\Windows\\System32\\wbem\\wmic.exe",
                "process", "where", "ProcessId=" + processId, "CALL", "setpriority", "\"below normal\"").start();
    }

    private void launchpad(Instant startTime) {
        generateRatingDataFromSims();

//        WowSimDB.discoverSetBonuses();
//        Tasks.dumpTier2Gear();
//        ItemCache.instance.get(null);
//        new ReadLog().run();
//        SourcesOfItemsRaid.findNormalVariants();

//        everyoneBis();

//        determineRatingMultipliers();
//        TaskMulti.paladinMultiSpecSolve();
//        druidMultiSpecSolve();

//        variableRatingProt(startTime);
//        runPrebuiltSimFiles();

//        allUpgradesProt(startTime);

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
        ModelCombined model = StandardModels.pallyRetModel();

        Map<Integer, List<ReforgeRecipe>> commonItems = commonFixedItems();
//        Map<Integer, List<ReforgeRecipe>> commonItems = null;

        EquipOptionsMap items = ItemLoadUtil.readAndLoad(DataLocation.gearRetFile, model, commonItems, PrintRecorder.withAutoOutput());

//        reforgeProcess(items, model, startTime);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);
//        reforgeProcessPlus(items, model, startTime, SlotEquip.Ring2,86880, 2, false, true, null);
//        reforgeProcessPlus(items, model, startTime, null, 87145, 2, false, EnchantMode.BothDefaultAndAlternate, null, false);
//        reforgeProcessPlus(items, model, startTime, null,86683, false, true, null);
//        reforgeProcessPlus(items, model, startTime, null,87015, 2, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true,86145, false, true, new StatBlock(285+80+120,0,0,165,160,160+160,0,0,0));
//        reforgeProcessPlus(items, model, startTime, null, 86145, false, true, null);
//        reforgeProcessPlusPlus(items, model, startTime, 87110, 87100, false, null);
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.bagItemsArray(model, new int[]{77530,89075,81262,87607,89823}));
//        reforgeProcessPlusMany(items, model, startTime, SourcesOfItems.currentItemsAll(DataLocation.gearRetFile, DataLocation.gearProtDpsFile, DataLocation.gearProtDefenceFile));

//        Predicate<SolvableItemSet> specialFilter;
//        job.specialFilter = set -> model.setBonus().countInAnySet(set.items()) >= 4;
//        ToIntFunction<SolvableEquipMap> countFunc = SetBonus.countInSpecifiedSet("Battlegear of the Lightning Emperor");
//        ToIntFunction<SolvableEquipMap> countFunc = SetBonus.countInSpecifiedSet("White Tiger Battlegear");
//        specialFilter = set -> countFunc.applyAsInt(set.items()) >= 4;
//        specialFilter = null;
//        reforgeProcessPlusMany(items, model, startTime,
//                ArrayUtil.concat(
//                    SourcesOfItems.throneClassGearSetHeroic(SpecType.PaladinRet, Difficulty.Celestial),
//                    strengthPlateThroneNormal(Difficulty.Celestial)),
//                0, false, specialFilter);

//          reforgeProcessPlusMany(items, model, startTime, new CostedItem[]{
//                  new CostedItem(84950,0),
//                  new CostedItem(89954,0),
//                  new CostedItem(86822,0)
//          }, 0);

        ToIntFunction<SolvableEquipMap> countSet = SetBonus.countInSpecifiedSet("Battlegear of the Lightning Emperor");
        reforgeProcessPlusMany(items, model, startTime, new int[]{
                        87026, // heroic peacock cloak
                        94942, // hydra bloodcloak

//                        86880, // dread shadow ring
//                        95140, // shado assault band
//                        86957, // heroic bladed tempest ring
//                        87071, // yang-xi heroic

                        87015, // heroic clawfeet
                        86979, // heroic impaling treads
                        87024, // null greathelm
                        87145, // defiled earth
                        86955, // heroic overwhelm assault belt
                        94726, // cloudbreaker belt
                        95652, // Puncture-Proof Greathelm head
                        95778, // celestial golden golem head

                        95535, // normal lightning legs
                        94773, // centripetal shoulders normal

                        85340, // ret tier14 legs
//                        87101, // ret tier14 head [running tank gem]
                        85339, // ret tier14 shoulder
                        85343, // ret tier14 chest
                        87100, // ret tier14 hands

                        95910, // ret tier15 chest celestial
                        95911, // ret tier15 gloves celestial
//                        95912, // ret tier15 celestial (don't have yet) [would need gem, AVOID]
//                        95913, // ret tier15 celestial (don't have yet)
                        95914, // ret tier15 shoulder celestial

                        95142, // striker's battletags
                        95205, // terra-cotta neck
//                        87036, // soulgrasp heroic

                        87145, // defiled earth
                        89934, // soul bracer
                        94820, // caustic spike bracers
        }, 2, false,
                null);
//                set -> countSet.applyAsInt(set.items()) >= 4);

//            reforgeProcessRetFixed(model, startTime, true);
//            reforgeProcessRetFixedAlone(model, startTime, true);
//        reforgeProcessRetChallenge(model, startTime);

//        compareBestReforgesWithCommon(DataLocation.gearRetFile, model, commonFixedItems(), null);

//                        findUpgradeSetup(items, strengthPlateMsvArray(), model, true, StatBlock.of(Hit, 200, Expertise, 200));
//                findUpgrade(items, pallyPhase3Valor(), model, true, null, 0);
//        new FindUpgrades(model, true).runMaxedItems(items, strengthPlateValorCelestialRet(), null);
//        new FindUpgrades(model, true).runMaxedItems(items, new CostedItem[]{new CostedItem(86905, 0)}, null);

//        new FindUpgrades(itemCache, model, true).findUpgradeSetup(items, new Tuple.Tuple2[] { Tuple.create(84950,0)});
//        findUpgradeSetup(items, strengthPlateCurrentItemsProt(model), model);
//        findUpgrade(items, bagItemsArray(ignoredItems), model, true, null);
//        findUpgrade(items, ArrayUtil.concat(bagItemsArray(ignoredItems), SourcesOfItems.currentItemsAll(DataLocation.gearRetFile, DataLocation.gearProtDpsFile, DataLocation.gearProtDefenceFile)), model, true, null, 2);

//        findUpgrade(items, ArrayUtil.concat(new CostedItem[][]{strengthPlateMsvArray(), strengthPlateMsvHeroicArray(), strengthPlateHeartOfFear(), strengthPlateHeartOfFearHeroic(), strengthPlateTerrace(), strengthPlateTerraceHeroic()}), model, true, null, 2);

//                findUpgradeSetup(items, strengthPlateCrafted(), model);

//        Difficulty difficulty = Difficulty.Celestial;
//        CostedItem[] upgradeShit = ArrayUtil.concat(new CostedItem[][]{
//                pallyPhase3Valor(),
//                throneClassGearSetHeroic(SpecType.PaladinRet, difficulty),
//                strengthPlateThroneNormal(difficulty),
//                strengthDpsTrinketsThroneNormal(difficulty),
//        });
//        upgradeShit = minusRadenLoot(upgradeShit);
//        findUpgrade(items, upgradeShit, model, true, null, 2, 4);
    }

    private void reforgeProt(Instant startTime) {
//        ModelCombined model = StandardModels.pallyProtDpsModel();
//        Path file = DataLocation.gearProtDpsFile;

        ModelCombined model = StandardModels.pallyProtMitigationModel();
        Path file = DataLocation.gearProtDefenceFile;

//        Map<Integer, List<ReforgeRecipe>> commonItems = commonFixedItems();
        Map<Integer, List<ReforgeRecipe>> commonItems = null;

        EquipOptionsMap items = ItemLoadUtil.readAndLoad(file, model, commonItems, PrintRecorder.withAutoOutput());

//        reforgeProcess(items, model, startTime);
//        reforgeProcessPlus(items, model, startTime, SlotEquip.Trinket2,79327, false, true, null);
//        reforgeProcessProtFixedPlus(model, startTime, 86753, false, true);
//        reforgeProcessProtFixed(model, startTime, true);
//        reforgeProcessProtFixed2(model, startTime, true);

//          reforgeProcessPlus(items, model, startTime, null, 96182, 0, false, EnchantMode.BothDefaultAndAlternate, null, false);
//        reforgeProcessPlus(items, model, startTime, null, 95144	, 2, false, EnchantMode.BothDefaultAndAlternate, null, false);
//        reforgeProcessPlus(items, model, startTime, null, 87145, 2, false, EnchantMode.BothDefaultAndAlternate, null, false);
//        reforgeProcessPlus(items, model, startTime, null,85340, 2, true, EnchantMode.BothDefaultAndAlternate, null, false);
//        reforgeProcessPlus(items, model, startTime, null, 86219, false, true, StatBlock.of(Expertise, 170, Primary, -170));
//        reforgeProcessPlusPlus(items, model, startTime, 85320, 85323, StatBlock.of(Expertise, 320, Primary, -320));
//          reforgeProcessPlusPlus(items, model, startTime, 87063, 95178, 2, false, null, true);

        // adding yang-xi for dps prot
//        reforgeProcessPlusMany(items, model, startTime, new CostedItem[]{
//                new CostedItem(87071,0), // yang-xi
//                new CostedItem(86681,0), // bad head
//                new CostedItem(85343,0), // normal chest
//                new CostedItem(87015,0) // clawfeet
//                },
//                2, true);

        // adding yang-xi for miti prot
//        reforgeProcessPlusMany(items, model, startTime, new CostedItem[]{
//                        new CostedItem(87071,0), // yang-xi
//                        new CostedItem(86661,0), // bad head
//                        new CostedItem(87015,0) // clawfeet
//                },
//                2, true);


//        reforgeProcessPlusMany(items, model, startTime, strengthPlateCurrentItemsRet(model));
//        reforgeProcessPlusMany(items, model, startTime, bagItemsArray(ignoredItems));
//        reforgeProcessPlusMany(items, model, startTime, new CostedItem[]{new CostedItem(87110, 0), new CostedItem(87100, 0), new CostedItem(86661, 0)});


//                reforgeProcessPlusMany(items, model, startTime, new int[]{
//                                86979, // heroic impaling treads
//                                86957, // heroic bladed tempest
//                                85343, // normal ret chest
//
//                                87015, // heroic clawfeet
//                                86979, // heroic impaling treads
//                                87071, // yang-xi heroic
//                                87145, // defiled earth
//                                85340, // normal ret legs
//                                87101, // heroic ret head
//                                86946, // ruby signet heroic
//                                94726, // cloudbreaker belt
//
//                                87026, // heroic peacock cloak
//                                86955, // heroic overwhelm assault belt
//                                95535, // normal lightning legs
//
//                                87050, // steelskin heroic
//                                95768, // greatshield gloaming celestial
//                                95652, // Puncture-Proof Greathelm head
//                                95687, // celestial beakbreaker cloak
//
//                                95142, // striker's battletags
//                                95205, // terra-cotta neck
//                                87036, // soulgrasp heroic
//
//                        95513, // scaled tyrant normal
//
//                        86979, // heroic impaling treads
//                        87024, // null greathelm
//
//                        94726, // cloudbreaker belt
//                        86955, // heroic overwhelm assault belt
//
//                        87026, // heroic peacock cloak
//                        86325, // daybreak
//
//                        95535, // normal lightning legs
//
//                                85340, // ret tier14 legs
//                                87101, // ret tier14 head
//                                85339, // ret tier14 shoulder
//                                85343, // ret tier14 chest
//                                87100, // ret tier14 hands
//
//                                95914, // ret tier15 shoulder celestial
//                                95910, // ret tier15 chest celestial
//                                95911, // ret tier15 gloves celestial
//
//                        95291, // prot tier15 hand normal
//                        95920, // prot tier15 chest celestial (don't have yet)
//                        95292, // prot tier15 head normal
//                        96667, // prot tier15 leg heroic
//                        95924, // prot tier15 shoulder celestial
//
//                        95142, // striker's battletags
//                        95205, // terra-cotta neck
//
//                        96182, // ultimate prot of the emperor thunder
//
//                                94773, // centripetal shoulders normal
//                                95140, // shado assault band
//
//                                87145, // defiled earth
//                                89934, // soul bracer
//                        94820, // caustic spike bracers
//
////                        87063, // vial of dragon's blood
//                        95178, // Lootraptor's Amulet
//                        96468, // talonrender chest
//
//                        }, 2, false, null);



//        findUpgradeSetup(items, strengthPlateCurrentItemsRet(model), model);
//        findUpgrade(items, strengthPlateHeartOfFear(), model, true, null, 2);
//        findUpgrade(items, ArrayUtil.concat(new CostedItem[][]{strengthPlateMsvArray(), strengthPlateMsvHeroicArray(), strengthPlateHeartOfFear(), strengthPlateHeartOfFearHeroic(), strengthPlateTerrace(), strengthPlateTerraceHeroic()}), model, true, null, 2);
//        findUpgradeSetup(items, ArrayUtil.concat(strengthPlateHeartOfFear(), strengthPlateHeartOfFearHeroic()), model, true, null);
//        findUpgrade(items, ArrayUtil.concat(strengthPlateTerrace(), strengthPlateTerraceHeroic()), model, true, null, 2);
//        findUpgrade(items, ArrayUtil.concat(strengthPlateTerrace(), strengthPlateMsvHeroicArray()), model, true, null, 2);
//        findUpgradeSetup(items, strengthPlateMsvArray(), model, false);
//        findUpgradeSetup(items, strengthPlateMsvHeroicArray(), model, false);
//        findUpgrade(items, strengthPlateHeartOfFearHeroic(), model, true, null, 2);
//        findUpgradeSetup(items, strengthPlateHeartOfFear(), model, false, StatBlock.of(Hit, 200, Expertise, 400));
//        findUpgrade(items, strengthPlateTerrace(), model, true, null, 2);
//        findUpgrade(items, bagItemsArray(ignoredItems), model, false, null);
//        findUpgradeMaxedItems(items, bagItemsArray(ignoredItems), model, true, null);
//        new FindUpgrades(model, true).runMaxedItems(items, strengthPlateValorCelestialTank(), null);
//        new FindUpgrades(model, true).run(items, strengthPlateCurrentItemsProtAllUpgradable(), null, 2);
//        new FindUpgrades(itemCache, model, true).run(items, strengthPlateCrafted());
//        findUpgrade(items, ArrayUtil.concat(bagItemsArray(ignoredItems), SourcesOfItems.currentItemsAll(DataLocation.gearRetFile, DataLocation.gearProtDpsFile, DataLocation.gearProtDefenceFile)), model, true, null, 2);

//        new FindUpgrades(itemCache, model, true).findUpgradeSetup(items, new Tuple.Tuple2[] { Tuple.create(84950,0)});
//                reforgeProcessPlus(items, model, startTime, true,86751, true, true, null);

//        compareBestReforgesWithCommon(file, model, commonFixedItems(), null);

//        rankSingleItems(model, Arrays.asList(87172, 87063, 79329, 79327, 86046, 87072, 87160, 96398, 96470, 96501, 96543,
//                94507, 94508


//                96471, hard to model
//                96523  difficult, very defensive
//                96555  defensive
//                ));

//        CostedItem[] allTheGoodShit = ArrayUtil.concat(
//                new CostedItem[][]{
////                    strengthPlateValorCelestialTank(),
////                    strengthPlateHeartOfFearHeroic(),
////                        strengthPlateMsvHeroicArray(),
////                    strengthPlateTerraceHeroic(),
//                        strengthPlateThroneHeroic(),
//                        tankTrinketsThroneHeroic(),
//                        strengthDpsTrinketsThroneHeroic(),
//                        throneClassGearSetHeroic(SpecType.PaladinProtMitigation, Difficulty.Normal),
//                        throneClassGearSetHeroic(SpecType.PaladinRet, true),
////                    strengthPallyTankSetT1Heroic(),
////                    strengthPallyRetSetT1Heroic(),
//                        new CostedItem[]{new CostedItem(90862, 0)}, // quest ring
//                        new CostedItem[]{new CostedItem(79327, 0)}, // darkmoon dps
////                    new CostedItem[]{new CostedItem(84910, 0)} // pvp shield
//                }
//        );
////        allTheGoodShit = minusRadenLoot(allTheGoodShit);
//        findBIS(model, allTheGoodShit, startTime, 2, false);

//        findBestBySlot(model, allTheGoodShit, startTime);

//        findUpgrade(items, pallyPhase3Valor(), model, true, null, 0);
//        findUpgrade(items, SourcesOfItems.strengthPlateCraftedT3(), model, true, null, 0, 8);
//        findUpgrade(items, SourcesOfItems.strengthPlateThroneNormalBoss(Difficulty.Heroic, 701), model, true, null, 2, 8);

        Difficulty difficulty = Difficulty.Heroic;
        CostedItem[] upgradeShit = ArrayUtil.concat(new CostedItem[][]{
                pallyPhase3Valor(),
//                throneClassGearSetHeroic(SpecType.PaladinProtMitigation, difficulty),
//                throneClassGearSetHeroic(SpecType.PaladinRet, difficulty),
//                strengthPlateThroneNormal(difficulty),
//                tankTrinketsThroneNormal(difficulty),
//                strengthDpsTrinketsThroneNormal(difficulty),
        });
        upgradeShit = minusRadenLoot(upgradeShit);
        findUpgrade(items, upgradeShit, model, true, null, 2, 25);
    }

    private void allUpgradesProt(Instant startTime) {
        ModelCombined modelMitigation = StandardModels.pallyProtMitigationModel();
        ModelCombined modelDps = StandardModels.pallyProtDpsModel();

        EquipOptionsMap itemsMitigation = ItemLoadUtil.readAndLoad(DataLocation.gearProtDefenceFile, modelMitigation, null, PrintRecorder.swallow());
        EquipOptionsMap itemsDps = ItemLoadUtil.readAndLoad(DataLocation.gearProtDpsFile, modelDps, null, PrintRecorder.swallow());

//        CostedItem[] upgradeNormal = throneProtLootMinusRaden(Difficulty.Normal);
//        CostedItem[] upgradeHeroic = throneProtLootMinusRaden(Difficulty.Heroic);

        CostedItem[] upgradeNormal = new CostedItem[0];
        CostedItem[] upgradeHeroic = new CostedItem[] {new CostedItem(96476, 0)};

        int multiply = 75; // 25

        OutputText.println("[[[[[[[[[[[[[[[[[[[[ PALLY PROT DPS normal UPGRADES ]]]]]]]]]]]]]]]]]]]]");
        List<UpgradeResultItem> outNormalDps = findUpgrade(itemsDps, upgradeNormal, modelDps, false, null, 2, multiply);

        OutputText.switchToNewFile();
        OutputText.println("[[[[[[[[[[[[[[[[[[[[ PALLY PROT DPS heroic UPGRADES ]]]]]]]]]]]]]]]]]]]]");
        List<UpgradeResultItem> outHeroicDps = findUpgrade(itemsDps, upgradeHeroic, modelDps, false, null, 2, multiply);

        OutputText.switchToNewFile();
        OutputText.println("[[[[[[[[[[[[[[[[[[[[ PALLY PROT MITIGATION normal UPGRADES ]]]]]]]]]]]]]]]]]]]]");
        List<UpgradeResultItem> outNormalMitigation = findUpgrade(itemsMitigation, upgradeNormal, modelMitigation, false, null, 2, multiply);

        OutputText.switchToNewFile();
        OutputText.println("[[[[[[[[[[[[[[[[[[[[ PALLY PROT MITIGATION heroic UPGRADES ]]]]]]]]]]]]]]]]]]]]");
        List<UpgradeResultItem> outHeroicMitigation = findUpgrade(itemsMitigation, upgradeHeroic, modelMitigation, false, null, 2, multiply);

        OutputText.switchToNewFile();
        LinkedHashMap<String, List<UpgradeResultItem>> outputs = new LinkedHashMap<>();
        outputs.put("NORMAL DPS", outNormalDps);
        outputs.put("HEROIC DPS", outHeroicDps);
        outputs.put("NORMAL MITIGATION", outNormalMitigation);
        outputs.put("HEROIC MITIGATION", outHeroicMitigation);
        FindUpgrades.reportMultipleRunsByBoss(outputs);
    }

    private void variableRatingProt(Instant startTime) {
//        Path file = DataLocation.gearProtDefenceFile;
        Path file = DataLocation.gearProtDpsFile;
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(file, StandardModels.pallyProtDpsModel(), null, PrintRecorder.withAutoOutput());

        int[] extraItems = new int[]{
                85320, // prot tier14 legs normal w/dodge+mostery
                85323, // prot tier14 chest normal, w/parry
                86659, // prot tier14 shoulder celestial, w/mastery
                86662, // prot tier14 hand celestial w/dodge
                85339, // ret tier14 shoulder
                85340, // ret tier14 legs
                85343, // ret tier14 chest
                87100, // ret tier14 hands
                87101, // ret tier14 head
                86325, // daybreak
                86955, // heroic overwhelm assault belt
                86979, // heroic impaling treads
                87024, // null greathelm
                87026, // heroic peacock cloak
                94942, // hydra bloodcloak
                87060, // Star-Stealer Waistguard
                87145, // defiled earth
                89934, // soul bracer
                94726, // cloudbreaker belt
                94773, // centripetal shoulders normal
//                94820, // caustic spike bracers
//                95140, // shado assault band
                95142, // striker's battletags
                95205, // terra-cotta neck
                95535, // normal lightning legs
                95652, // Puncture-Proof Greathelm head
                96182, // ultimate prot of the emperor thunder

                95910, // ret tier15 chest celestial
                95911, // ret tier15 gloves celestial
                95912, // ret tier15 celestial
                95913, // ret tier15 celestial
                95914, // ret tier15 shoulder celestial

                95291, // prot tier15 hand normal
                95920, // prot tier15 chest celestial
                95922, // prot tier15 head celestial (don't have yet)
                96667, // prot tier15 leg heroic
                95924, // prot tier15 shoulder celestial
        };
        List<EquippedItem> bagsItems = bagItemsArray(ignoredItems);
        List<EquippedItem> extraItems2 = Arrays.stream(extraItems).mapToObj(id ->
                bagsItems.stream().filter(x -> x.itemId()==id).findAny().orElse(new EquippedItem(id, new int[0], null, 2, 0, null))
        ).toList();
//        List<EquippedItem> extraItems2 = Arrays.stream(extraItems).mapToObj(id -> new EquippedItem(id, new int[0], null, 2, 0, null)).toList();

        List<EquippedItem> extraItemsAll = ArrayUtil.concat(extraItems2, currentItemsAll(DataLocation.gearRetFile, DataLocation.gearProtDpsFile, DataLocation.gearProtDefenceFile));
        extraItemsAll = extraItemsAll.stream()
                .filter(ei -> ItemCache.instance.get(ei.itemId(), 0).slot() != SlotItem.Trinket)
                .filter(ei -> ItemCache.instance.get(ei.itemId(), 0).slot() != SlotItem.Ring)
                .toList();
        optimalForVariedRating(items, extraItemsAll);
    }

    private void reforgeBoom(Instant startTime) {
        ModelCombined model = StandardModels.modelFor(SpecType.DruidBoom);
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(DataLocation.gearBoomFile, model, null, PrintRecorder.withAutoOutput());

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
        ModelCombined model = StandardModels.modelFor(SpecType.DruidTree);
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(DataLocation.gearTreeFile, model, null, PrintRecorder.withAutoOutput());

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
        ModelCombined model = StandardModels.modelFor(SpecType.DruidBear);
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(DataLocation.gearBearFile, model, null, PrintRecorder.withAutoOutput());

        reforgeProcess(items, model, startTime);
//        findUpgradeSetup(items, ArrayUtil.concat(SourcesOfItems.agilityLeatherCelestialArray(), SourcesOfItems.agilityLeatherValorArray()), model, true, null);
    }

    private void reforgeWarlock(Instant startTime) {
        ModelCombined model = StandardModels.modelFor(SpecType.Warlock);
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(DataLocation.gearWarlockFile, model, null, PrintRecorder.withAutoOutput());

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
        // 27/01/2026
        map.put(95205, List.of(new ReforgeRecipe(Hit, Expertise))); // Neck Necklace of the Terra-Cotta Vanquisher
        map.put(86979, List.of(new ReforgeRecipe(null, null))); // Foot Impaling Treads
        map.put(85343, List.of(new ReforgeRecipe(Crit, Hit))); // Chest White Tiger Battleplate
        map.put(96182, List.of(new ReforgeRecipe(Parry, Haste))); // Offhand Ultimate Protection of the Emperor
        map.put(95535, List.of(new ReforgeRecipe(null, null))); // Leg Legplates of the Lightning Throne
        map.put(95140, List.of(new ReforgeRecipe(Haste, Expertise))); // Ring Band of the Shado-Pan Assault
        map.put(94773, List.of(new ReforgeRecipe(null, null))); // Shoulder Shoulderguards of Centripetal Destruction
        map.put(87100, List.of(new ReforgeRecipe(Crit, Haste))); // Hand White Tiger Gauntlets
        map.put(94519, List.of(new ReforgeRecipe(Crit, Haste))); // Trinket Primordius' Talisman of Rage
        map.put(87024, List.of(new ReforgeRecipe(Haste, Expertise))); // Head Nullification Greathelm
        map.put(95513, List.of(new ReforgeRecipe(Hit, Expertise))); // Ring Band of the Scaled Tyrant
        map.put(94726, List.of(new ReforgeRecipe(Mastery, Hit))); // Belt Cloudbreaker Greatbelt
        map.put(94820, List.of(new ReforgeRecipe(Crit, Haste))); // Wrist Caustic Spike Bracers
        map.put(94942, List.of(new ReforgeRecipe(Expertise, Haste))); // Back Hydra-Scale Bloodcloak
        map.put(96376, List.of(new ReforgeRecipe(null, null))); // Weapon1H Worldbreaker's Stormscythe
        return map;
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetChallenge(ModelCombined model, Instant startTime) {
        // CHALLENGE MODE SET

//        List<EquippedItem> itemIds = InputGearParser.readInput(DataLocation.gearRetFile);
//        List<FullItemData> inputSetItems = ItemLoadUtil.loadItems(itemIds, model.enchants(), true);
//
//        OutputText.println("FINDING EXPECTED REFORGE IN RAID RET");
//        EquipOptionsMap raidMap = ItemMapUtil.limitedItemsReforgedToMap(model.reforgeRules(), inputSetItems, commonFixedItems());
//        FullItemSet raidSet = chooseEngineAndRun(model, raidMap, null, null).orElseThrow();
//        OutputText.println("FOUND REFORGE RAID RET");
//        outputResultSimple(Optional.of(raidSet), model, false);
//
//        Map<Integer, List<ReforgeRecipe>> presetReforge = commonFixedItems();
//        raidSet.items().forEachValue(item -> presetReforge.put(item.itemId(), Collections.singletonList(item.reforge)));
//        presetReforge.put(89954, List.of(new ReforgeRecipe(Crit, Haste)));
//
//        EquipOptionsMap map = ItemMapUtil.limitedItemsReforgedToMap(model.reforgeRules(), inputSetItems, presetReforge);
//
//        // compared to raid dps
//        int[] extraItems = new int[]{
//                81129, 89665, 90910, 81268, 81138, 77539, 85991,
//                81098, 89503, 82975, 81694, 86799, 82856, 86682,
//                86794, 89954, 81130, 87060, 82812, 86145, 86680,
//                81284, 86852, 86742, 88862, 81113, 90862, 81251,
//                89526, 82822, 82814
//        };
//
////        Function<FullItemData, FullItemData> customiseItem = extraItem -> {
//////            if (extraItem.id == 82812) { // Pyretic Legguards
//////                return extraItem.changeFixed(new StatBlock(285, 0, 0, 165, 160, 160 + 60, 0, 0, 0, 0));
//////            } else if (extraItem.id == 81284) { // Anchoring Sabatons
//////                return extraItem.changeFixed(new StatBlock(60 + 60, 0, 140, 0, 0, 120, 0, 0, 0, 0));
//////            } else if (extraItem.id == 81113) { // Spike-Soled Stompers
//////                return extraItem.changeFixed(new StatBlock(60, 0, 0, 0, 160, 175 + 160, 0, 0, 0, 0));
////            if (extraItem.itemId() == 87060) { // Star-Stealer Waistguard
////                return extraItem.changeEnchant(new StatBlock(0, 0, 0, 0, 160, 320 * 2 + 160, 0, 120, 0, 0));
//////            } else if (extraItem.id == 86794) { // starcrusher gauntlets
//////                return extraItem.changeFixed(new StatBlock(170, 0, 0, 0, 160, 60 + 320 + 160, 0, 0, 0, 0));
//////            } else if (extraItem.id == 86145) { // jang-xi devastating legs
//////                return extraItem.changeFixed(new StatBlock(120, 430, 0, 0, 160, 160 * 2, 160, 0, 0, 0));
////            } else if (extraItem.itemId() == 77539) { // engineer helm
////                return extraItem.changeEnchant(new StatBlock(216, 0, 0, 0, 600, 600, 0, 0, 0, 0));
////            } else if (extraItem.itemId() == 89503) { // Greenstone Drape
////                return extraItem.changeStatsBase(new StatBlock(501, 751, 0, 334, 334, 0, 0, 0, 0, 0))
////                        .changeEnchant(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
////            } else if (extraItem.slot() == SlotItem.Back) {
////                return extraItem.changeEnchant(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
////            } else {
////                OutputText.println("DEFAULT ENCHANT " + extraItem);
////                return ItemLoadUtil.defaultEnchants(extraItem, model, false, null);
////            }
////        };
//
//        for (int extraId : extraItems) {
//            ReforgeRecipe reforge = null;
//            if (presetReforge.containsKey(extraId))
//                reforge = presetReforge.get(extraId).getFirst();
//            FullItemData extraItem = addExtra(map, model, extraId, 0, EnchantMode.BothDefaultAndAlternate, reforge, false, false);
//            if (extraItem != null)
//                OutputText.println("EXTRA " + extraItem);
//        }
//
//        EquipOptionsMap scaledMap = ItemLevel.scaleForChallengeMode(map);
//
//        JobInput job = new JobInput();
//        job.printRecorder.outputImmediate = true;
//        job.model = model;
//        job.setItemOptions(scaledMap);
//        job.startTime = startTime;
//        job.runSizeMultiply = 16;
//        job.forceRandom = true;
//        job.forcedRunSized = BILLION;
//        JobOutput output = Solver.runJob(job);
//        FullItemSet bestScaledSet = output.getFinalResultSet().orElseThrow();
//
//        OutputText.println("SCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALED");
//        outputResultSimple(Optional.of(bestScaledSet), model, true);
//
//        for (SlotEquip slot : SlotEquip.values()) {
//            FullItemData scaledChoice = bestScaledSet.items().get(slot);
//            if (scaledChoice != null) {
//                FullItemData[] options = map.get(slot);
//                boolean inRaidDPSSet = inputSetItems.stream().anyMatch(x -> x.itemId() == scaledChoice.itemId());
//
//                if (inRaidDPSSet) {
//                    // need exact item + forge but prescale
//                    // note were using id match only, scaled stuff could confused normal "exact" match
//                    // avoid engineering heads mixup
//                    FullItemData match = ArrayUtil.findOne(options, x -> x.itemId() == scaledChoice.itemId() && Objects.equals(x.reforge, scaledChoice.reforge));
//                    options = new FullItemData[]{match};
//                } else {
//                    options = ArrayUtil.allMatch(options, x -> x.itemId() == scaledChoice.itemId());
//                }
//
//                map.put(slot, options);
//            }
//        }
//
//        ModelCombined finalModel = new ModelCombined(model.statRatings(), StatRequirementsHitExpertise.retWideCapRange(), model.reforgeRules(), model.enchants(), model.setBonus(), SpecType.PaladinRet, model.defaultGemAlternateChoice());
//        Optional<FullItemSet> bestSetFinal = chooseEngineAndRun(finalModel, map, startTime, null);
//
//        OutputText.println("FINALFINALFINALFINALFINALFINALFINALFINALFINALFINALFINAL");
//        outputResultSimple(bestSetFinal, model, true);
    }

    private void multiSpecSpecifiedRating() {
        ModelCombined modelRet = StandardModels.modelFor(SpecType.PaladinRet);
        ModelCombined modelProt = StandardModels.modelFor(SpecType.PaladinProtMitigation);

        OutputText.println("RET GEAR CURRENT");
        List<FullItemData> retItems = ItemLoadUtil.loadItems(InputGearParser.readInput(DataLocation.gearRetFile), modelRet.enchants(), PrintRecorder.withAutoOutput());
        OutputText.println("PROT GEAR CURRENT");
        List<FullItemData> protItems = ItemLoadUtil.loadItems(InputGearParser.readInput(DataLocation.gearProtDpsFile), modelProt.enchants(), PrintRecorder.withAutoOutput());

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
