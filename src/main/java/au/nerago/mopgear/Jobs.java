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

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue", "unused"})
public class Jobs {
    public static final long BILLION = 1000 * 1000 * 1000;
    public static ItemCache itemCache;

    public static void combinationDumb(EquipOptionsMap items, ModelCombined model, Instant startTime) {
        for (int extraId : new int[]{89503, 81129, 89649, 87060, 89665, 82812, 90910, 81284, 82814, 84807, 84870, 84790, 82822}) {
            ItemData extraItem = addExtra(items, model, extraId, Function.identity(), null, false, true, true);
            OutputText.println("EXTRA " + extraItem);
        }
        //        ItemUtil.disenchant(items);
        ItemUtil.defaultEnchants(items, model, true);
        ItemUtil.bestForgesOnly(items, model);
        ItemLevel.scaleForChallengeMode(items);

        ModelCombined dumbModel = model.withNoRequirements();

        Optional<ItemSet> bestSet = Solver.chooseEngineAndRun(dumbModel, items, startTime, null, null);
        outputResultSimple(bestSet, model, true);
    }

    public static void findUpgradeSetup(EquipOptionsMap baseItems, CostedItem[] extraItems, ModelCombined model, boolean allowHacks, StatBlock adjustment) {
        new FindUpgrades(itemCache, model, allowHacks).run(baseItems, extraItems, adjustment);
    }

    public static void findBIS(ModelCombined model, CostedItem[] allItems, Instant startTime) {
        EquipOptionsMap optionsMap = EquipOptionsMap.empty();
        Arrays.stream(allItems).map(equip -> ItemUtil.loadItemBasic(itemCache, equip.itemId()))
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
        job.forceRandom = true;
        job.runSize = BILLION / 4;
        job.printRecorder.outputImmediate = true;
        Solver.runJob(job);

        outputResultSimple(job.resultSet, model, true);
    }

    public static void rankAlternativesAsSingleItems(ModelCombined model, int[] itemIds, Map<Integer, StatBlock> enchants, boolean scaleChallenge) {
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
                .sorted(Comparator.comparingLong(model::calcRating))
                .toList();
        for (ItemData item : reforgedItems) {
            OutputText.println(item + " " + model.calcRating(item));
        }
    }

    public static void rankAlternativeCombos(EquipOptionsMap baseOptions, ModelCombined model, Instant startTime, List<List<Integer>> comboListList) {
        BestHolder<List<ItemData>> best = new BestHolder<>();
        for (List<Integer> combo : comboListList) {
            Function<ItemData, ItemData> enchants = t -> ItemUtil.defaultEnchants(t, model, true);
            EquipOptionsMap submitMap = baseOptions.deepClone();
            List<ItemData> optionItems = new ArrayList<>();
            for (int extraId : combo) {
                ItemData item = addExtra(submitMap, model, extraId, enchants, null, true, true, true);
                optionItems.add(item);
            }

            ItemSet set = Solver.chooseEngineAndRun(model, submitMap, null, BILLION/1000, null).orElseThrow();
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
        job.config(model, itemOptions, startTime, null, null);
        Solver.runJob(job);

        outputResultSimple(job.resultSet, model, true);
        outputTweaked(job.resultSet, itemOptions, model);
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlus(EquipOptionsMap itemOptions, ModelCombined model, Instant startTime, SlotEquip slot, int extraItemId, boolean replace, boolean defaultEnchants, StatBlock adjustment) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);

        Function<ItemData, ItemData> enchanting = defaultEnchants ? x -> ItemUtil.defaultEnchants(x, model, true) : Function.identity();
        if (slot == null)
            slot = extraItem.slot.toSlotEquip();

        EquipOptionsMap runItems = itemOptions.deepClone();
        extraItem = addExtra(runItems, model, extraItemId, slot, enchanting, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem);

        JobInfo job = new JobInfo();
//        job.config(model, runItems, startTime, null, adjustment);
        job.config(model, runItems, startTime, BILLION, adjustment);
        Solver.runJob(job);

        job.printRecorder.outputNow();
        outputResultSimple(job.resultSet, model, true);
        if (job.resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job);
        }
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot, boolean errorOnExists) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        return addExtra(reforgedItems, model, extraItemId, extraItem.slot.toSlotEquip(), customiseItem, reforge, replace, customiseOthersInSlot, errorOnExists);
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, SlotEquip slot, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot, boolean errorOnExists) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        extraItem = customiseItem.apply(extraItem);
        ItemData[] extraForged = reforge != null ?
                new ItemData[]{Reforger.presetReforge(extraItem, reforge)} :
                Reforger.reforgeItem(model.reforgeRules(), extraItem);
        if (replace) {
            OutputText.println("REPLACING " + (reforgedItems.get(slot) != null ? reforgedItems.get(slot)[0] : "NOTHING"));
            reforgedItems.put(slot, extraForged);
        } else {
            ItemData[] existing = reforgedItems.get(slot);
            if (ArrayUtil.anyMatch(existing, item -> item.id == extraItemId)) {
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
        HashSet<Integer> seen = new HashSet<>();
        ArrayUtil.forEach(slotArray, it -> {
            if (seen.add(it.id)) {
                OutputText.println("NEW " + slot + " " + it);
            }
        });
        return extraItem;
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlusPlus(EquipOptionsMap runItems, ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2, boolean replace, StatBlock adjustment) {
        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, model, true);
//        Function<ItemData, ItemData> enchant2 = x -> x.changeFixed(new StatBlock(285,90,0,165,0,0,320,0,0,0));

        ItemData extraItem1 = addExtra(runItems, model, extraItemId1, enchant, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem1);
        OutputText.println();

        ItemData extraItem2 = addExtra(runItems, model, extraItemId2, enchant, null, replace, true, true);
        OutputText.println("EXTRA " + extraItem2);
        OutputText.println();

        JobInfo job = new JobInfo();
        job.config(model, runItems, startTime, BILLION, adjustment);
        job.printRecorder.outputImmediate = true;
        Solver.runJob(job);

        outputResultSimple(job.resultSet, model, true);
        if (job.resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job);
        }
        job.printRecorder.outputNow();
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, CostedItem[] extraItems) {
        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, model, true);

        for (CostedItem entry : extraItems) {
            int extraItemId = entry.itemId();
            if (SourcesOfItems.ignoredItems.contains(extraItemId)) continue;
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            SlotEquip slot = extraItem.slot.toSlotEquip();
            ItemData[] existing = items.get(slot);
            if (existing == null) {
                OutputText.println("SKIP SLOT NOT NEEDED " + extraItem);
            } else if (ArrayUtil.anyMatch(existing, item -> item.id == extraItemId)) {
                OutputText.println("SKIP DUP " + extraItem);
            } else {
                if (slot == SlotEquip.Trinket1) {
                    addExtra(items, model, extraItemId, SlotEquip.Trinket1, enchant, null, false, true, true);
                    addExtra(items, model, extraItemId, SlotEquip.Trinket2, enchant, null, false, true, true);
                } else if (slot == SlotEquip.Ring1) {
                    addExtra(items, model, extraItemId, SlotEquip.Ring1, enchant, null, false, true, true);
                    addExtra(items, model, extraItemId, SlotEquip.Ring2, enchant, null, false, true, true);
                } else {
                    addExtra(items, model, extraItemId, slot, enchant, null, false, true, true);
                }
            }
        }

        JobInfo job = new JobInfo();
        job.model = model;
        job.itemOptions = items;
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
        job.runSize = null;
//        job.runSize = BILLION;
//        job.runSize = BILLION / 10;
        job.adjustment = null;
        Solver.runJob(job);
        job.printRecorder.outputNow();
        Optional<ItemSet> best = job.resultSet;
        outputResultSimple(best, model, true);
    }

    public static void outputResultSimple(Optional<ItemSet> bestSet, ModelCombined model, boolean detailedOutput) {
        if (bestSet.isPresent()) {
            bestSet.get().outputSet(model);
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
//        multi.addFixedForge(86802, ReforgeRecipe.empty()); // lei shen trinket
//        multi.addFixedForge(86219, new ReforgeRecipe(StatType.Hit, StatType.Haste)); // 1h sword
//        multi.addFixedForge(89280, new ReforgeRecipe(StatType.Crit, StatType.Haste)); // voice greathelm

//        multi.addFixedForge(85991, new ReforgeRecipe(StatType.Hit, StatType.Expertise)); // Soulgrasp Choker
//        multi.addFixedForge(86794, new ReforgeRecipe(StatType.Hit, StatType.Expertise)); // Starcrusher Gauntlets

//        multi.addFixedForge(89069, new ReforgeRecipe(StatType.Crit, StatType.Haste)); // ring golden stair
//        multi.addFixedForge(89954, new ReforgeRecipe(StatType.Expertise, StatType.Haste));// warbelt

//        multi.addFixedForge(89346, new ReforgeRecipe(StatType.Dodge, StatType.Haste)); // autumn shoulder

        FindMultiSpec.SpecDetails ret = new FindMultiSpec.SpecDetails(
                "RET",
                DataLocation.gearRetFile,
                ModelCombined.extendedRetModel(true, false),
                1,
                new int[]{
////                        81113, // spike-soled stompers
////                        88862, // tankiss
//                        86742, // jasper clawfeet
//                        86852, // impaling treads
////                        81694, // command bracers
////                        82856, // dark blaze gauntlets
////                        84950 // pvp belt
////                        86753, // cloak peacock feathers
//                        89954, // warbelt pods
//                        87060, // star-stealer waist
//                        89280 // voice helm
                },
                false);

        FindMultiSpec.SpecDetails protDamage = new FindMultiSpec.SpecDetails(
                "PROT-DAMAGE",
                DataLocation.gearProtDpsFile,
                ModelCombined.damageProtModel(),
                3,
                new int[]{
//                        88862, // tankiss
//                        84870, // pvp legs
//                        87060, // star waistguard
//                        86682, // white tiger gloves
//                        86753, // peacock cloak
//                        89345, // stonetoe spaulders
//                        86680, // white tiger legs
                }, false);

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
                }, false);

//        ItemUtil.validateRet(ret.itemOptions);
//        ItemUtil.validateProt(protDamage.itemOptions);
//        ItemUtil.validateProt(protDefence.itemOptions);

        multi.addSpec(ret);
        multi.addSpec(protDamage);
        multi.addSpec(protDefence);

        // TODO solve for challenge dps too

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
                false);

        FindMultiSpec.SpecDetails tree = new FindMultiSpec.SpecDetails(
                "TREE",
                DataLocation.gearTreeFile,
                ModelCombined.standardTreeModel(),
                1,
                new int[]{
                }, false);

        multi.addSpec(boom);
        multi.addSpec(tree);

        multi.solve(startTime);
    }

}
