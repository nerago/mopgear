package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.DataLocation;
import au.nerago.mopgear.io.SourcesOfItems;
import au.nerago.mopgear.model.ItemLevel;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.JobInfo;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.*;
import au.nerago.mopgear.io.ItemCache;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue", "unused"})
public class Jobs {
    public static final long BILLION = 1000 * 1000 * 1000;
    public static ItemCache itemCache;

    public static void findUpgradeSetup(EquipOptionsMap baseItems, CostedItem[] extraItems, ModelCombined model, boolean allowHacks, StatBlock adjustment, int upgradeLevel) {
        new FindUpgrades(itemCache, model, allowHacks).run(baseItems, extraItems, adjustment, upgradeLevel);
    }

    public static void findBIS(ModelCombined model, CostedItem[] allItems, Instant startTime, int upgradeLevel) {
        EquipOptionsMap optionsMap = EquipOptionsMap.empty();
        // TODO upgrade level
        Arrays.stream(allItems).map(equip -> ItemUtil.loadItemBasic(itemCache, equip.itemId(), upgradeLevel))
                .filter(item -> item.slot != SlotItem.WeaponTwoHand)
                .forEach(item -> {
                    item = ItemUtil.defaultEnchants(item, model, true);
                    SlotEquip[] slotOptions = item.slot.toSlotEquipOptions();
                    ItemData[] reforged = Reforger.reforgeItem(model.reforgeRules(), item);
                    for (SlotEquip slot : slotOptions) {
                        optionsMap.put(slot, ArrayUtil.concatNullSafe(optionsMap.get(slot), reforged));
                    }
                });

        JobInfo job = new JobInfo();
        job.model = model;
        job.itemOptions = optionsMap;
        job.startTime = startTime;
        job.forceSkipIndex = true;
        job.forcedRunSized = BILLION*4;
        job.printRecorder.outputImmediate = true;
        job.specialFilter = set -> model.setBonus().countInSet(set.items) >= 4;
        Solver.runJob(job);

        outputResultSimple(job.resultSet, model, true);
    }

    public static void findBestBySlot(ModelCombined model, CostedItem[] allItems, Instant startTime, int upgradeLevel) {
        Map<Integer, Integer> costs = new HashMap<>();
        EquipOptionsMap optionsMap = EquipOptionsMap.empty();
        // TODO upgrade level
        Arrays.stream(allItems)
                .peek(costed -> costs.put(costed.itemId(), costed.cost()))
                .map(equip -> ItemUtil.loadItemBasic(itemCache, equip.itemId(), upgradeLevel))
                .filter(item -> item.slot != SlotItem.WeaponTwoHand)
                .forEach(item -> {
                    item = ItemUtil.defaultEnchants(item, model, true);
                    SlotEquip[] slotOptions = item.slot.toSlotEquipOptions();
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
                                    OutputText.printf("%s \t%08d \t%s \t%d \t%d\n", slot, model.calcRating(item), item.name, item.ref.itemLevel(), costs.get(item.ref.itemId())));
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
            Function<ItemData, ItemData> enchants = t -> ItemUtil.defaultEnchants(t, model, true);
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
        JobInfo job = new JobInfo();
        job.printRecorder.outputImmediate = true;
        job.hackAllow = true;
        job.runSizeMultiply = 8;
        job.config(model, itemOptions, startTime, null);
        Solver.runJob(job);

        outputResultSimple(job.resultSet, model, true);
        outputTweaked(job.resultSet, itemOptions, model);
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlus(EquipOptionsMap itemOptions, ModelCombined model, Instant startTime, SlotEquip slot, int extraItemId, int upgradeLevel, boolean replace, boolean defaultEnchants, StatBlock adjustment) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId, upgradeLevel);

        // TODO upgrade level

        Function<ItemData, ItemData> enchanting = defaultEnchants ? x -> ItemUtil.defaultEnchants(x, model, true) : Function.identity();
        if (slot == null)
            slot = extraItem.slot.toSlotEquip();

        EquipOptionsMap runItems = itemOptions.deepClone();
        extraItem = addExtra(runItems, model, extraItemId, upgradeLevel, slot, enchanting, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem);

        JobInfo job = new JobInfo();
//        job.config(model, runItems, startTime, null, adjustment);
        job.config(model, runItems, startTime, adjustment);
        job.runSizeMultiply = 16;
        Solver.runJob(job);

        job.printRecorder.outputNow();
        outputResultSimple(job.resultSet, model, true);
        if (job.resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job);
        }
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, int upgradeLevel, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot, boolean errorOnExists) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId, upgradeLevel);
        return addExtra(reforgedItems, model, extraItemId, upgradeLevel, extraItem.slot.toSlotEquip(), customiseItem, reforge, replace, customiseOthersInSlot, errorOnExists);
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, int upgradeLevel, SlotEquip slot, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot, boolean errorOnExists) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId, upgradeLevel);
        extraItem = customiseItem.apply(extraItem);
        ItemRef ref = extraItem.ref;

        ItemData[] extraForged = reforge != null ?
                new ItemData[]{Reforger.presetReforge(extraItem, reforge)} :
                Reforger.reforgeItem(model.reforgeRules(), extraItem);
        if (replace) {
            OutputText.println("REPLACING " + (reforgedItems.get(slot) != null ? reforgedItems.get(slot)[0] : "NOTHING"));
            reforgedItems.put(slot, extraForged);
        } else {
            ItemData[] existing = reforgedItems.get(slot);
            if (ArrayUtil.anyMatch(existing, item -> item.ref.equalsTyped(ref))) {
                if (errorOnExists)
                    throw new IllegalArgumentException("item already included " + extraItemId + " " + extraItem);
                OutputText.println("ALREADY INCLUDED " + extraItem);
                return null;
            }
            reforgedItems.put(slot, ArrayUtil.concat(existing, extraForged));
        }
        ItemData[] slotArray = reforgedItems.get(slot);
        if (customiseOthersInSlot) {
            ArrayUtil.mapInPlace(slotArray, customiseItem);
        }
        HashSet<ItemRef> seen = new HashSet<>();
        ArrayUtil.forEach(slotArray, it -> {
            if (seen.add(it.ref)) {
                OutputText.println("NEW " + slot + " " + it);
            }
        });
        return extraItem;
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlusPlus(EquipOptionsMap runItems, ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2, int upgradeLevel, boolean replace, StatBlock adjustment) {
        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, model, true);
//        Function<ItemData, ItemData> enchant2 = x -> x.changeFixed(new StatBlock(285,90,0,165,0,0,320,0,0,0));

        ItemData extraItem1 = addExtra(runItems, model, extraItemId1, upgradeLevel, enchant, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem1);
        OutputText.println();

        ItemData extraItem2 = addExtra(runItems, model, extraItemId2, upgradeLevel, enchant, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem2);
        OutputText.println();

        JobInfo job = new JobInfo();
        job.config(model, runItems, startTime, adjustment);
        job.runSizeMultiply = 16;
        job.printRecorder.outputImmediate = true;
        Solver.runJob(job);

        outputResultSimple(job.resultSet, model, true);
        if (job.resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job);
        }
        job.printRecorder.outputNow();
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, CostedItem[] extraItems, int upgradeLevel) {
        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, model, true);

        for (CostedItem entry : extraItems) {
            int extraItemId = entry.itemId();
            if (SourcesOfItems.ignoredItems.contains(extraItemId)) continue;
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId, upgradeLevel);
            for (SlotEquip slot : extraItem.slot.toSlotEquipOptions()) {
                ItemData[] existing = items.get(slot);
                if (existing == null) {
                    OutputText.println("SKIP SLOT NOT NEEDED " + extraItem);
                } else if (ArrayUtil.anyMatch(existing, item -> item.ref.equalsTyped(extraItem.ref))) {
                    OutputText.println("SKIP DUP " + extraItem);
                } else {
                    addExtra(items, model, extraItemId, upgradeLevel, slot, enchant, null, false, true, true);
                }
            }
        }

        JobInfo job = new JobInfo();
        job.model = model;
        job.itemOptions = items;
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
        job.runSizeMultiply = 12;
        Solver.runJob(job);
        job.printRecorder.outputNow();
        Optional<ItemSet> best = job.resultSet;
        outputResultSimple(best, model, true);
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

    public static void outputTweaked(Optional<ItemSet> bestSet, EquipOptionsMap reforgedItems, ModelCombined model) {
        bestSet.ifPresent(itemSet -> outputTweaked(itemSet, reforgedItems, model));
    }

    public static void outputTweaked(ItemSet bestSet, EquipOptionsMap reforgedItems, ModelCombined model) {
        ItemSet tweakSet = Tweaker.tweak(bestSet, model, reforgedItems);
        if (bestSet != tweakSet) {
            OutputText.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");

            OutputText.println(tweakSet.getTotals().toStringExtended() + " " + model.calcRating(tweakSet));
            for (SlotEquip slot : SlotEquip.values()) {
                ItemData orig = bestSet.items.get(slot);
                ItemData change = tweakSet.items.get(slot);
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

    private static void outputFailureDetails(ModelCombined model, EquipOptionsMap runItems, JobInfo job) {
        FindStatRange.checkSetReportOnly(model, runItems, job);
    }

    public static void paladinMultiSpecSolve(Instant startTime) {
        FindMultiSpec multi = new FindMultiSpec(itemCache);
        multi.addFixedForge(86802, ReforgeRecipe.empty()); // lei shen trinket

//        multi.addFixedForge(86219, new ReforgeRecipe(StatType.Hit, StatType.Haste)); // 1h sword
//        multi.addFixedForge(89280, new ReforgeRecipe(StatType.Crit, StatType.Haste)); // voice greathelm
//        multi.addFixedForge(85991, new ReforgeRecipe(StatType.Hit, StatType.Expertise)); // Soulgrasp Choker
//        multi.addFixedForge(86794, new ReforgeRecipe(StatType.Hit, StatType.Expertise)); // Starcrusher Gauntlets
//        multi.addFixedForge(89069, new ReforgeRecipe(StatType.Crit, StatType.Haste)); // ring golden stair
//        multi.addFixedForge(89954, new ReforgeRecipe(StatType.Expertise, StatType.Haste));// warbelt
//        multi.addFixedForge(89346, new ReforgeRecipe(StatType.Dodge, StatType.Haste)); // autumn shoulder

//        multi.addFixedForge(86852, new ReforgeRecipe(null, null)); // Foot Impaling Treads
//        map.put(84807, List.of(new ReforgeRecipe(null, null))); // Back Malevolent Gladiator's Cloak of Alacrity
//        map.put(85991, List.of(new ReforgeRecipe(Hit, Expertise))); // Neck Soulgrasp Choker (Hit->Expertise)
//        map.put(89069, List.of(new ReforgeRecipe(Expertise, Hit))); // Ring Ring of the Golden Stair (Expertise->Hit)
//        map.put(90862, List.of(new ReforgeRecipe(Haste, Hit))); // Ring Seal of the Bloodseeker (Haste->Hit)
//        multi.addFixedForge(87024, new ReforgeRecipe(Crit, Expertise)); // Head Nullification Greathelm (Crit->Expertise)
////        map.put(86802, List.of(new ReforgeRecipe(null, null))); // Trinket Lei Shen's Final Orders
//        multi.addFixedForge(86680, new ReforgeRecipe(Mastery, Haste)); // Leg White Tiger Legplates (Mastery->Haste)
//        multi.addFixedForge(86683, new ReforgeRecipe(Crit, Expertise)); // Chest White Tiger Battleplate (Crit->Expertise)
//        multi.addFixedForge(85339, new ReforgeRecipe(Hit, Expertise)); // Shoulder White Tiger Pauldrons (Hit->Expertise)
//        multi.addFixedForge(86682, new ReforgeRecipe(Expertise, Haste)); // Hand White Tiger Gauntlets (Expertise->Haste)
//        map.put(86906, List.of(new ReforgeRecipe(Mastery, Expertise))); // Weapon Kilrak, Jaws of Terror (Mastery->Expertise)
//        multi.addFixedForge(84910, new ReforgeRecipe(Mastery, Haste)); // Shield
//        multi.addFixedForge(8607, new ReforgeRecipe(Mastery, Haste)); // Shield

        FindMultiSpec.SpecDetails ret = new FindMultiSpec.SpecDetails(
                "RET",
                DataLocation.gearRetFile,
                ModelCombined.extendedRetModel(true, false),
                1,
                new int[]{
////                        81113, // spike-soled stompers
                        88862, // tankiss
//                        86742, // jasper clawfeet
//                        86852, // impaling treads
////                        81694, // command bracers
////                        82856, // dark blaze gauntlets
////                        84950 // pvp belt
                        86753, // cloak peacock feathers
//                        89954, // warbelt pods
//                        87060, // star-stealer waist
//                        84949, // mal glad girdle accuracy
//                        89280 // voice helm
//                        87024 // null greathelm
                },
                0,
                false,
                Map.of());

        FindMultiSpec.SpecDetails protDamage = new FindMultiSpec.SpecDetails(
                "PROT-DAMAGE",
                DataLocation.gearProtDpsFile,
                ModelCombined.damageProtModel(),
                4,
                new int[]{
//                        88862, // tankiss
//                        84870, // pvp legs
//                        87060, // star waistguard
//                        86682, // white tiger gloves
//                        86753, // peacock cloak
//                        89345, // stonetoe spaulders
//                        86680, // white tiger legs
//                        84949 // mal glad girdle accuracy
                },
                0,
                false,
                Map.of());

        FindMultiSpec.SpecDetails protDefence = new FindMultiSpec.SpecDetails(
                "PROT-DEFENCE",
                DataLocation.gearProtDefenceFile,
                ModelCombined.defenceProtModel(),
                1,
                new int[]{
//                        89280, // voice amp
////                        87024, // null greathelm
//                        89345, // autumn shoulder
////                        85339, // white tiger pauldrons
////                        89345, // stonetoe spaulders
//                        82980, // gauntlets ancient steel
//                        85983, // bracers six oxen
//                        89075, // yi cloak
                        90594, // golden lotus durable necklace
//                        84807, // mav glad cloak alacrity
                },
                0,
                false,
                Map.of(89934, 899340));

//        ItemUtil.validateRet(ret.itemOptions);
//        ItemUtil.validateProt(protDamage.itemOptions);
//        ItemUtil.validateProt(protDefence.itemOptions);

        multi.addSpec(ret);
        multi.addSpec(protDamage);
        multi.addSpec(protDefence);

        multi.haveDuplicateItem(89934);

        multi.solve(startTime);
    }

    public static void druidMultiSpecSolve(Instant startTime) {
        FindMultiSpec multi = new FindMultiSpec(itemCache);

        FindMultiSpec.SpecDetails boom = new FindMultiSpec.SpecDetails(
                "BOOM",
                DataLocation.gearBoomFile,
                ModelCombined.standardBoomModel(),
                177,
                new int[]{
                },
                0,
                false,
                Map.of());

        FindMultiSpec.SpecDetails tree = new FindMultiSpec.SpecDetails(
                "TREE",
                DataLocation.gearTreeFile,
                ModelCombined.standardTreeModel(),
                1,
                new int[]{
                },
                0,
                false,
                Map.of());

        multi.addSpec(boom);
        multi.addSpec(tree);

        multi.solve(startTime);
    }

    public static void compareBestReforgesWithCommon(Path file, ModelCombined model, Map<Integer, List<ReforgeRecipe>> commonOne, Map<Integer, List<ReforgeRecipe>> commonTwo) {
        EquipOptionsMap optionsOne = ItemUtil.readAndLoad(itemCache, true, file, model.reforgeRules(), commonOne);
        EquipOptionsMap optionsTwo = ItemUtil.readAndLoad(itemCache, true, file, model.reforgeRules(), commonTwo);

        int runSizeMultiply = 2;

        JobInfo jobOne = new JobInfo();
        jobOne.printRecorder.outputImmediate = true;
        jobOne.runSizeMultiply = runSizeMultiply;
        jobOne.model = model;
        jobOne.itemOptions = optionsOne;
        Solver.runJob(jobOne);

        OutputText.println("111111111111111111111111111111111111");
        jobOne.resultSet.orElseThrow().outputSetDetailed(model);
        double ratingOne = model.calcRating(jobOne.resultSet.orElseThrow());

        JobInfo jobTwo = new JobInfo();
        jobTwo.printRecorder.outputImmediate = true;
        jobTwo.runSizeMultiply = runSizeMultiply;
        jobTwo.model = model;
        jobTwo.itemOptions = optionsTwo;
        Solver.runJob(jobTwo);

        OutputText.println("22222222222222222222222222222222222222");
        jobTwo.resultSet.orElseThrow().outputSetDetailed(model);
        double ratingTwo = model.calcRating(jobTwo.resultSet.orElseThrow());

        OutputText.printf("COMMON ITEM PENALTY PERCENT %1.3f\n", ratingOne / ratingTwo * 100);
    }
}
