package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.DataLocation;
import au.nerago.mopgear.io.SourcesOfItems;
import au.nerago.mopgear.model.*;
import au.nerago.mopgear.permute.Solver;
import au.nerago.mopgear.process.*;
import au.nerago.mopgear.results.*;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BestHolder;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static au.nerago.mopgear.domain.StatType.Crit;
import static au.nerago.mopgear.domain.StatType.Expertise;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue", "unused"})
public class Tasks {
    public static final long BILLION = 1000 * 1000 * 1000;

    public static void findUpgrade(EquipOptionsMap baseItems, CostedItem[] extraItems, ModelCombined model, boolean allowHacks, StatBlock adjustment, int upgradeLevel) {
        new FindUpgrades(model, allowHacks).run(baseItems, extraItems, adjustment, upgradeLevel);
    }

    public static void findUpgrade(EquipOptionsMap baseItems, List<EquippedItem> extraItems, ModelCombined model, boolean allowHacks, StatBlock adjustment) {
        new FindUpgrades(model, allowHacks).run(baseItems, extraItems, adjustment);
    }

    public static void findUpgradeMaxedItems(EquipOptionsMap baseItems, List<EquippedItem> extraItems, ModelCombined model, boolean allowHacks, StatBlock adjustment) {
        new FindUpgrades(model, allowHacks).runMaxedItems(baseItems, extraItems, adjustment);
    }

    public static void findBIS(ModelCombined model, CostedItem[] allItems, Instant startTime, int upgradeLevel) {
        EquipOptionsMap optionsMap = EquipOptionsMap.empty();
        Arrays.stream(allItems).map(equip -> ItemLoadUtil.loadItemBasic(equip.itemId(), upgradeLevel))
                .filter(item -> item.slot() != SlotItem.Weapon2H)
                .forEach(item -> {
                    item = ItemLoadUtil.defaultEnchants(item, model, true);
                    SlotEquip[] slotOptions = item.slot().toSlotEquipOptions();
                    ItemData[] reforged = Reforger.reforgeItem(model.reforgeRules(), item);
                    for (SlotEquip slot : slotOptions) {
                        optionsMap.put(slot, ArrayUtil.concatNullSafe(optionsMap.get(slot), reforged));
                    }
                });

        JobInput job = new JobInput();
        job.model = model;
        job.itemOptions = optionsMap;
        job.startTime = startTime;
        job.forceSkipIndex = true;
        job.forcedRunSized = BILLION*4;
        job.printRecorder.outputImmediate = true;
        job.specialFilter = set -> model.setBonus().countInSet(set.items()) >= 4;
        JobOutput output = Solver.runJob(job);

        outputResultSimple(output.resultSet, model, true);
    }

    public static void findBestBySlot(ModelCombined model, CostedItem[] allItems, Instant startTime, int upgradeLevel) {
        Map<Integer, Integer> costs = new HashMap<>();
        EquipOptionsMap optionsMap = EquipOptionsMap.empty();
        Arrays.stream(allItems)
                .peek(costed -> costs.put(costed.itemId(), costed.cost()))
                .map(equip -> ItemLoadUtil.loadItemBasic(equip.itemId(), upgradeLevel))
                .filter(item -> item.slot() != SlotItem.Weapon2H)
                .forEach(item -> {
                    item = ItemLoadUtil.defaultEnchants(item, model, true);
                    SlotEquip[] slotOptions = item.slot().toSlotEquipOptions();
                    ItemData[] reforged = Reforger.reforgeItemBest(model, item);
//                    ItemData[] reforged = new ItemData[] { item };
                    for (SlotEquip slot : slotOptions) {
                        optionsMap.put(slot, ArrayUtil.concatNullSafe(optionsMap.get(slot), reforged));
                    }
                });

        optionsMap.entryStream().forEach(
                tuple -> {
                    SlotEquip slot = tuple.a();
                    ItemData[] options = tuple.b();
//                    OutputText.printf("##### %s #####\n", slot);
                    Arrays.stream(options).sorted(Comparator.comparingLong(model::calcRating))
                            .forEach(item ->
                                    OutputText.printf("%s \t%08d \t%s \t%d \t%d\n", slot, model.calcRating(item), item.shared.name(), item.itemLevel(), costs.get(item.itemId())));
                    OutputText.println();
//                    TopHolderN<ItemData> best = new TopHolderN<>(5, model::calcRating);
//                    ArrayUtil.forEach(options, best::add);
//                    OutputText.println(best.result().stream()
//                            .map(item -> item.name + " " + model.calcRating(item))
//                            .collect(Collectors.joining(" | ")));
                }
        );
    }

    public static void rankAlternativeCombos(EquipOptionsMap baseOptions, ModelCombined model, Instant startTime, List<List<Integer>> comboListList) {
        BestHolder<List<ItemData>> best = new BestHolder<>();
        for (List<Integer> combo : comboListList) {
            Function<ItemData, ItemData> enchants = t -> ItemLoadUtil.defaultEnchants(t, model, true);
            EquipOptionsMap submitMap = baseOptions.deepClone();
            List<ItemData> optionItems = new ArrayList<>();
            for (int extraId : combo) {
                ItemData item = addExtra(submitMap, model, extraId, 0, enchants, null, true, true, true);
                optionItems.add(item);
            }

            ItemSet set = Solver.chooseEngineAndRun(model, submitMap, null, null).orElseThrow();
            set.outputSet(model);
            long rating = model.calcRating(set);
            OutputText.println("RATING " + rating);
            OutputText.println();

            best.add(optionItems, rating);
        }

        OutputText.println(best.get().toString());
    }

    public static void reforgeProcess(EquipOptionsMap itemOptions, ModelCombined model, Instant startTime) {
        JobInput job = new JobInput();
        job.printRecorder.outputImmediate = true;
        job.hackAllow = true;
        job.runSizeMultiply = 8;
        job.config(model, itemOptions, startTime, null);
        JobOutput output = Solver.runJob(job);

        outputResultSimple(output.resultSet, model, true);
        outputReforgeJson(output.resultSet);
        outputTweaked(output.resultSet, itemOptions, model);
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlus(EquipOptionsMap itemOptions, ModelCombined model, Instant startTime, SlotEquip slot, int extraItemId, int upgradeLevel, boolean replace, boolean defaultEnchants, StatBlock adjustment) {
        ItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel);

        Function<ItemData, ItemData> enchanting = defaultEnchants ? x -> ItemLoadUtil.defaultEnchants(x, model, true) : Function.identity();
        if (slot == null)
            slot = extraItem.slot().toSlotEquip();

        EquipOptionsMap runItems = itemOptions.deepClone();
        extraItem = addExtra(runItems, model, extraItemId, upgradeLevel, slot, enchanting, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem);

        JobInput job = new JobInput();
        job.printRecorder.outputImmediate = true;
        job.config(model, runItems, startTime, adjustment);
        job.runSizeMultiply = 8;
        JobOutput output = Solver.runJob(job);

        outputResultSimple(output.resultSet, model, true);
        if (output.resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job.printRecorder);
        }
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, int upgradeLevel, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot, boolean errorOnExists) {
        ItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel);
        return addExtra(reforgedItems, model, extraItem, customiseItem, reforge, replace, customiseOthersInSlot, errorOnExists);
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, int upgradeLevel, SlotEquip slot, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot, boolean errorOnExists) {
        ItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel);
        return addExtra(reforgedItems, model, extraItem, slot, customiseItem, reforge, replace, customiseOthersInSlot, errorOnExists);
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, ItemData extraItem, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot, boolean errorOnExists) {
        return addExtra(reforgedItems, model, extraItem, extraItem.slot().toSlotEquip(), customiseItem, reforge, replace, customiseOthersInSlot, errorOnExists);
    }

    @Nullable
    private static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, ItemData extraItem, SlotEquip slot, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot, boolean errorOnExists) {
        extraItem = customiseItem.apply(extraItem);
        ItemRef ref = extraItem.ref();
        ItemData[] existing = reforgedItems.get(slot);

        if (slot == SlotEquip.Weapon && existing != null && extraItem.slot() != existing[0].slot()) {
            OutputText.println("WRONG WEAPON " + extraItem);
            return null;
        }

        ItemData[] extraForged = reforge != null ?
                new ItemData[]{Reforger.presetReforge(extraItem, reforge)} :
                Reforger.reforgeItem(model.reforgeRules(), extraItem);

        if (replace) {
            OutputText.println("REPLACING " + (existing != null ? existing[0] : "NOTHING"));
            reforgedItems.put(slot, extraForged);
        } else {
            if (ArrayUtil.anyMatch(existing, item -> item.ref().equalsTyped(ref))) {
                if (errorOnExists)
                    throw new IllegalArgumentException("item already included " + extraItem);
                OutputText.println("ALREADY INCLUDED " + extraItem);
                return null;
            }
            reforgedItems.put(slot, ArrayUtil.concatNullSafe(existing, extraForged));
        }

        ItemData[] slotArray = reforgedItems.get(slot);
        if (customiseOthersInSlot) {
            ArrayUtil.mapInPlace(slotArray, customiseItem);
        }
        HashSet<ItemRef> seen = new HashSet<>();
        ArrayUtil.forEach(slotArray, it -> {
            if (seen.add(it.ref())) {
                OutputText.println("NEW " + slot + " " + it);
            }
        });

        return extraItem;
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlusPlus(EquipOptionsMap runItems, ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2, int upgradeLevel, boolean replace, StatBlock adjustment) {
        Function<ItemData, ItemData> enchant = x -> ItemLoadUtil.defaultEnchants(x, model, true);
//        Function<ItemData, ItemData> enchant2 = x -> x.changeFixed(new StatBlock(285,90,0,165,0,0,320,0,0,0));

        ItemData extraItem1 = addExtra(runItems, model, extraItemId1, upgradeLevel, enchant, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem1);
        OutputText.println();

        ItemData extraItem2 = addExtra(runItems, model, extraItemId2, upgradeLevel, enchant, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem2);
        OutputText.println();

        JobInput job = new JobInput();
        job.config(model, runItems, startTime, adjustment);
        job.runSizeMultiply = 16;
        job.printRecorder.outputImmediate = true;
        JobOutput output = Solver.runJob(job);

        outputResultSimple(output.resultSet, model, true);
        if (output.resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job.printRecorder);
        }
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, CostedItem[] extraItems, int upgradeLevel) {
        Function<ItemData, ItemData> enchant = x -> ItemLoadUtil.defaultEnchants(x, model, true);

        for (CostedItem entry : extraItems) {
            int extraItemId = entry.itemId();
            if (SourcesOfItems.ignoredItems.contains(extraItemId)) continue;
            ItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel);
            for (SlotEquip slot : extraItem.slot().toSlotEquipOptions()) {
                ItemData[] existing = items.get(slot);
                if (existing == null) {
                    OutputText.println("SKIP SLOT NOT NEEDED " + extraItem);
                } else if (ArrayUtil.anyMatch(existing, item -> item.ref().equalsTyped(extraItem.ref()))) {
                    OutputText.println("SKIP DUP " + extraItem);
                } else {
                    addExtra(items, model, extraItemId, upgradeLevel, slot, enchant, null, false, true, true);
                }
            }
        }

        JobInput job = new JobInput();
        job.model = model;
        job.itemOptions = items;
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
        job.runSizeMultiply = 12;
        JobOutput output = Solver.runJob(job);
        job.printRecorder.outputNow();
        Optional<ItemSet> best = output.resultSet;
        outputResultSimple(best, model, true);
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, List<EquippedItem> extraItems) {
        Function<ItemData, ItemData> enchant = x -> ItemLoadUtil.defaultEnchants(x, model, true);

        EquipOptionsMap itemsOriginal = items.deepClone();

        for (EquippedItem entry : extraItems) {
            if (SourcesOfItems.ignoredItems.contains(entry.itemId())) continue;
            ItemData extraItem = ItemLoadUtil.loadItem(entry, true);
            for (SlotEquip slot : extraItem.slot().toSlotEquipOptions()) {
                ItemData[] existing = items.get(slot);
                if (existing == null) {
                    OutputText.println("SKIP SLOT NOT NEEDED " + extraItem);
                } else if (ArrayUtil.anyMatch(existing, item -> item.ref().equalsTyped(extraItem.ref()))) {
                    OutputText.println("SKIP DUP " + extraItem);
                } else {
                    addExtra(items, model, extraItem, slot, enchant, null, false, true, true);
                }
            }
        }

        JobInput job = new JobInput();
        job.model = model;
        job.itemOptions = items;
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
        job.runSizeMultiply = 20;
        JobOutput output = Solver.runJob(job);
        job.printRecorder.outputNow();
        ItemSet best = output.resultSet.orElseThrow();
        outputResultChanges(itemsOriginal, best, model);
    }

    public static void outputResultSimple(Optional<ItemSet> bestSet, ModelCombined model, boolean detailedOutput) {
        if (bestSet.isPresent()) {
            if (detailedOutput) {
                bestSet.get().outputSetDetailed(model);
                bestSet.get().outputSetLight();
            } else {
                bestSet.get().outputSet(model);
            }
        } else {
            OutputText.println("@@@@@@@@@ NO VALID SET RESULTS @@@@@@@@@");
        }
    }

    private static void outputResultChanges(EquipOptionsMap baseline, ItemSet best, ModelCombined model) {
        best.outputSetDetailed(model);
        best.outputSetLight();

        OutputText.println("CHANGES vvvvvvvvvvvvvvv CHANGES");
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] options = baseline.get(slot);
            ItemData choice = best.items().get(slot);
            if (choice != null) {
                boolean existing = ArrayUtil.anyMatch(options, x -> x.ref().equalsTyped(choice.ref()));
                if (existing)
                    OutputText.println(" == " + choice.toStringExtended());
                else
                    OutputText.println(">>> " + choice.toStringExtended());
            }
        }
    }

    private static void outputReforgeJson(Optional<ItemSet> resultSet) {
        AsWowSimJson.writeToOut(resultSet.orElseThrow().items());
    }

    public static void outputTweaked(Optional<ItemSet> bestSet, EquipOptionsMap reforgedItems, ModelCombined model) {
        bestSet.ifPresent(itemSet -> outputTweaked(itemSet, reforgedItems, model));
    }

    public static void outputTweaked(ItemSet bestSet, EquipOptionsMap reforgedItems, ModelCombined model) {
        ItemSet tweakSet = Tweaker.tweak(bestSet, model, reforgedItems);
        if (bestSet != tweakSet) {
            OutputText.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");

            OutputText.println(tweakSet.totalForRating().toStringExtended() + " " + model.calcRating(tweakSet));
            OutputText.println(tweakSet.totalForCaps().toStringExtended());
            for (SlotEquip slot : SlotEquip.values()) {
                ItemData orig = bestSet.items().get(slot);
                ItemData change = tweakSet.items().get(slot);
                if (orig != null && change != null) {
                    if (!ItemData.isIdenticalItem(orig, change)) {
                        OutputText.println(change + " " + model.calcRating(change));
                    }
                } else if (orig != null || change != null) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    private static void outputFailureDetails(ModelCombined model, EquipOptionsMap runItems, PrintRecorder job) {
        FindStatRange2.checkSetReportOnly(model, runItems, job);
    }

    public static void paladinMultiSpecSolve() {
        FindMultiSpec multi = new FindMultiSpec();
//        multi.addFixedForge(86802, ReforgeRecipe.empty()); // lei shen trinket

//        multi.addFixedForge(86219, new ReforgeRecipe(StatType.Hit, StatType.Haste)); // 1h sword
//        multi.addFixedForge(89280, new ReforgeRecipe(StatType.Crit, StatType.Haste)); // voice greathelm
//        multi.addFixedForge(85991, new ReforgeRecipe(StatType.Hit, StatType.Expertise)); // Soulgrasp Choker
//        multi.addFixedForge(86794, new ReforgeRecipe(StatType.Hit, StatType.Expertise)); // Starcrusher Gauntlets
//        multi.addFixedForge(89069, new ReforgeRecipe(StatType.Crit, StatType.Haste)); // ring golden stair
//        multi.addFixedForge(89954, new ReforgeRecipe(StatType.Expertise, StatType.Haste));// warbelt
//        multi.addFixedForge(89346, new ReforgeRecipe(StatType.Dodge, StatType.Haste)); // autumn shoulder

//        multi.addFixedForge(86979, new ReforgeRecipe(Hit, Expertise)); // Foot Impaling Treads (Hit->Expertise)
        multi.addFixedForge(87100, ReforgeRecipe.empty()); // Hand White Tiger Gauntlets
        multi.addFixedForge(87026, ReforgeRecipe.empty()); // Back Cloak of Peacock Feathers
        multi.addFixedForge(86683, new ReforgeRecipe(Crit, Expertise)); // Chest White Tiger Battleplate (Crit->Expertise)
        multi.addFixedForge(86957, ReforgeRecipe.empty()); // Ring Ring of the Bladed Tempest

        int extraUpgrade = 2;
        boolean preUpgrade = false;

        multi.addSpec(
                "RET",
                DataLocation.gearRetFile,
                ModelCombined.extendedRetModel(true, false),
                2113,
                new int[]{
//                        88862, // tankiss
//                        84950, // pvp belt
//                        87024, // null greathelm
//                        87036, // heroic soulgrasp
//                        87026, // heroic peacock cloak
//                        86880, // dread shadow ring
//                        86955, // heroic overwhelm assault belt

                        // possibly useful
//                        89954, // warbelt pods
//                        84949, // mal glad girdle accuracy
//                        89280, // voice helm
//                        86822, // celestial overwhelm assault belt
//                        87015, // heroic clawfeet
                          86979, // heroic impaling treads
//                        86957, // heroic bladed tempest
                },
                extraUpgrade,
                preUpgrade
        );

        multi.addSpec(
                "PROT-DAMAGE",
                DataLocation.gearProtDpsFile,
                ModelCombined.damageProtModel(),
                799,
                new int[]{
//                        84870, // pvp legs
//                        86682, // white tiger gloves
//                        86680, // white tiger legs
//                        84949 // mal glad girdle accuracy
//                        87026, // heroic peacock cloak
//                        86075, // steelskin basic
//                        86955, // heroic overwhelm assault belt
//                        86979, // heroic impaling treads
//                        87015, // clawfeet
//                        87062 // elegion heroic
//                        86957, // heroic bladed tempest
                },
                extraUpgrade,
                preUpgrade
        )
//                .setDuplicatedItems(Map.of(89934, 1))
                .setWorstCommonPenalty(99.7);

        multi.addSpec(
                "PROT-DEFENCE",
                DataLocation.gearProtDefenceFile,
                ModelCombined.defenceProtModel(),
                145,
                new int[]{
//                        89280, // voice amp
////                        87024, // null greathelm
////                        85339, // white tiger pauldrons
////                        89345, // stonetoe spaulders
//                        82980, // gauntlets ancient steel
//                        85983, // bracers six oxen
//                        90594, // golden lotus durable necklace
//                        84807, // mav glad cloak alacrity
//                        87036, // heroic soulgrasp
//                        87026, // heroic peacock cloak
//                        86955, // heroic overwhelm assault belt
//                        86979, // heroic impaling treads
//                        87015, // clawfeet
//                        87062 // elegion heroic
//                        89075, // yi cloak
//                        86957, // heroic bladed tempest
//                        86325 // normal daybreak drake
                },
                extraUpgrade,
                preUpgrade
        )
                .setDuplicatedItems(Map.of(89934, 2))
                .setWorstCommonPenalty(99.0);

//        multi.suppressSlotCheck(86957);
//        multi.suppressSlotCheck(84829);
//        multi.suppressSlotCheck(86880);

//        multi.overrideEnchant(86905, StatBlock.of(StatType.Primary, 500));

//        multi.solve(3000);
        multi.solve(50000);
//        multi.solve(600000);
//        multi.solve(4000000);
    }

    public static void druidMultiSpecSolve() {
        FindMultiSpec multi = new FindMultiSpec();

        multi.addSpec(
                "BOOM",
                DataLocation.gearBoomFile,
                ModelCombined.standardBoomModel(),
                400,
                new int[]{
//                        86909 // regail dagger
//                        86694, // Eternal Blossom Mantle
//                        88885, // Clever Ashyo's Armbands
                        86748, // Cape of Three Lanterns
//                        89078, // sagewhisper cloak
                },
                0,
                false
        );

        multi.addSpec(
                "TREE",
                DataLocation.gearTreeFile,
                ModelCombined.standardTreeModel(),
                1,
                new int[]{
//                        86909 // regail dagger
                        88885, // Clever Ashyo's Armbands
                        86810, // Worldwaker Cabochon
//                        89078, // sagewhisper cloak
                },
                0,
                false
        );

        multi.overrideEnchant(86865, StatBlock.empty); // no sha gem
        multi.overrideEnchant(86893, StatBlock.empty); // no sha gem

        multi.solve(3000);
    }

    public static void compareBestReforgesWithCommon(Path file, ModelCombined model, Map<Integer, List<ReforgeRecipe>> commonOne, Map<Integer, List<ReforgeRecipe>> commonTwo) {
        EquipOptionsMap optionsOne = ItemLoadUtil.readAndLoad(true, file, model.reforgeRules(), commonOne);
        EquipOptionsMap optionsTwo = ItemLoadUtil.readAndLoad(true, file, model.reforgeRules(), commonTwo);

        int runSizeMultiply = 2;

        JobInput jobOne = new JobInput();
        jobOne.printRecorder.outputImmediate = true;
        jobOne.runSizeMultiply = runSizeMultiply;
        jobOne.model = model;
        jobOne.itemOptions = optionsOne;
        JobOutput outputOne = Solver.runJob(jobOne);

        OutputText.println("111111111111111111111111111111111111");
        outputOne.resultSet.orElseThrow().outputSetDetailed(model);
        double ratingOne = model.calcRating(outputOne.resultSet.orElseThrow());

        JobInput jobTwo = new JobInput();
        jobTwo.printRecorder.outputImmediate = true;
        jobTwo.runSizeMultiply = runSizeMultiply;
        jobTwo.model = model;
        jobTwo.itemOptions = optionsTwo;
        JobOutput outputTwo = Solver.runJob(jobTwo);

        OutputText.println("22222222222222222222222222222222222222");
        outputTwo.resultSet.orElseThrow().outputSetDetailed(model);
        double ratingTwo = model.calcRating(outputTwo.resultSet.orElseThrow());

        OutputText.printf("COMMON ITEM PENALTY PERCENT %1.3f\n", ratingOne / ratingTwo * 100);
    }

    public static void determineRatingMultipliers() {
        StatRatingsWeights tankMitigation = new StatRatingsWeights(DataLocation.weightProtMitigationFile, false, true, false);
        StatRatingsWeights tankDps = new StatRatingsWeights(DataLocation.weightProtDpsFile, false, true, false);
        StatRatingsWeights retRet = new StatRatingsWeights(DataLocation.weightRetFile);

        EquipOptionsMap itemsRet = ItemLoadUtil.readAndLoad(true, DataLocation.gearRetFile, ReforgeRules.ret(), null);
        EquipOptionsMap itemsTank = ItemLoadUtil.readAndLoad(true, DataLocation.gearProtDpsFile, ReforgeRules.prot(), null);

        double rateMitigation = determineRatingMultipliersOne(tankMitigation, itemsTank, StatRequirementsHitExpertise.protFlexibleParry());
        double rateTankDps = determineRatingMultipliersOne(tankDps, itemsTank, StatRequirementsHitExpertise.protFlexibleParry());
        double rateRet = determineRatingMultipliersOne(retRet, itemsRet, StatRequirementsHitExpertise.ret());

        double targetCombined = 1000000000;

        OutputText.printf("MITIGATION %,d\n", (long)rateMitigation);
        OutputText.printf("TANK_DPS   %,d\n", (long)rateTankDps);
        OutputText.printf("RET        %,d\n", (long)rateRet);
        OutputText.println();

        OutputText.printf("damageProtModel 15%% mitigation, 85%% dps\n");
        long dmgMultiplyA = Math.round(targetCombined * 0.15 / rateMitigation);
        long dmgMultiplyB = Math.round(targetCombined * 0.85 / rateTankDps);
        OutputText.printf("USE mitigation %d dps %d\n", dmgMultiplyA, dmgMultiplyB);
        double dmgTotal = dmgMultiplyA * rateMitigation + dmgMultiplyB * rateTankDps;
        OutputText.printf("EFFECTIVE %.2f %.2f\n\n",
                dmgMultiplyA * rateMitigation / dmgTotal,
                dmgMultiplyB * rateTankDps / dmgTotal);

        OutputText.printf("defenceProtModel 90%% mitigation, 10%% dps\n");
        long defMultiplyA = Math.round(targetCombined * 0.9 / rateMitigation);
        long defMultiplyB = Math.round(targetCombined * 0.1 / rateTankDps);
        OutputText.printf("USE mitigation %d dps %d\n", defMultiplyA, defMultiplyB);
        double defTotal = defMultiplyA * rateMitigation + defMultiplyB * rateTankDps;
        OutputText.printf("EFFECTIVE %.2f %.2f\n\n",
                defMultiplyA * rateMitigation / defTotal,
                defMultiplyB * rateTankDps / defTotal);

        long multiTargetCombined = 1000000000000L;
        OutputText.printf("multiSpec 5%% ret 80%% dmg_tank 15%% mitigation\n");
        long multiA = Math.round(multiTargetCombined * 0.05 / rateRet);
        long multiB = Math.round(multiTargetCombined * 0.80 / dmgTotal);
        long multiC = Math.round(multiTargetCombined * 0.15 / defTotal);
        OutputText.printf("USE ret %d dmg_tank %d mitigation %d \n", multiA, multiB, multiC);
        double multiTotal = multiA * rateRet + multiB * dmgTotal + multiC * defTotal;
        OutputText.printf("EFFECTIVE %.2f %.2f %.2f\n\n",
                multiA * rateRet / multiTotal,
                multiB * dmgTotal / multiTotal,
                multiC * defTotal / multiTotal);
    }

    private static long determineRatingMultipliersOne(StatRatingsWeights weights, EquipOptionsMap items, StatRequirements req) {
        ModelCombined model = new ModelCombined(weights, req, ReforgeRules.prot(), null, new SetBonus());
        JobInput job = new JobInput();
        job.model = model;
        job.itemOptions = items;
        JobOutput output = Solver.runJob(job);
        ItemSet set = output.resultSet.orElseThrow();
        return model.calcRating(set);
    }
}
