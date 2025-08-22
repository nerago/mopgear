package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.io.DataLocation;
import au.nicholas.hardy.mopgear.io.ItemCache;
import au.nicholas.hardy.mopgear.io.SourcesOfItems;
import au.nicholas.hardy.mopgear.model.ItemLevel;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.TopCollectorReporting;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static au.nicholas.hardy.mopgear.EngineUtil.chooseEngineAndRun;

public class Jobs {
    public static final long BILLION = 1000 * 1000 * 1000;
    public static ItemCache itemCache;

    public static void combinationDumb(EquipOptionsMap items, ModelCombined model, Instant startTime) {
        for (int extraId : new int[]{89503, 81129, 89649, 87060, 89665, 82812, 90910, 81284, 82814, 84807, 84870, 84790, 82822}) {
            ItemData extraItem = addExtra(items, model, extraId, Function.identity(), null, false, true);
            System.out.println("EXTRA " + extraItem);
        }
        //        ItemUtil.disenchant(items);
        ItemUtil.defaultEnchants(items, model, true);
        ItemUtil.bestForgesOnly(items, model);
        ItemLevel.scaleForChallengeMode(items);

        ModelCombined dumbModel = model.withNoRequirements();

        Optional<ItemSet> bestSet = chooseEngineAndRun(dumbModel, items, startTime, null, null, null);
        outputResult(bestSet, model, true);
    }

    public static void findUpgradeSetup(EquipOptionsMap items, Tuple.Tuple2<Integer, Integer>[] tuple2s, ModelCombined model) {
        new FindUpgrades(itemCache).findUpgradeSetup(model, items, tuple2s);
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
                .sorted(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())))
                .toList();
        for (ItemData item : reforgedItems) {
            System.out.println(item + " " + model.calcRating(item.totalStatCopy()));
        }
    }

    public static void multiSpecSequential(Instant startTime) throws IOException {
        ModelCombined modelNull = ModelCombined.nullMixedModel();
        ModelCombined modelRet = ModelCombined.extendedRetModel(true, false);
        ModelCombined modelProt = ModelCombined.standardProtModel();

        System.out.println("RET GEAR CURRENT");
        EquipOptionsMap retMap = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearRetFile, modelRet.reforgeRules(), null);
        System.out.println("PROT GEAR CURRENT");
        EquipOptionsMap protMap = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearProtFile, modelProt.reforgeRules(), null);
        ItemUtil.validateDualSets(retMap, protMap);
        EquipOptionsMap commonMap = ItemUtil.commonInDualSet(retMap, protMap);

        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, modelRet, true);
//        addExtra(retMap, modelRet, 81113, enchant, null, false, false); // spike boots
//        addExtra(retMap, modelRet, 89075, enchant, null, false, false); // yi's cloak
//        addExtra(retMap, modelRet, 81694, enchant, null, false, false); // command bracer

//        commonMap.replaceWithFirstOption(SlotEquip.Neck);
//        commonMap.replaceWithFirstOption(SlotEquip.Hand);
//        commonMap.replaceWithFirstOption(SlotEquip.Trinket1);
//        commonMap.replaceWithSpecificForge(SlotEquip.Ring1, new ReforgeRecipe(Haste, Hit));
//        commonMap.replaceWithSpecificForge(SlotEquip.Ring2, new ReforgeRecipe(Crit, Haste));
//        commonMap.replaceWithSpecificForge(SlotEquip.Trinket1, new ReforgeRecipe(Haste, Expertise));

        System.out.println("COMMON COMBOS " + ItemUtil.estimateSets(commonMap));

        Stream<ItemSet> commonStream = EngineStream.runSolverPartial(modelNull, commonMap, startTime, null, null,0);
//        long initialSize = 50000;
//        Stream<ItemSet> commonStream = EngineRandom.runSolverPartial(modelNull, commonMap, startTime, null, initialSize);

//        Long runSize = BILLION / 1000;
        Long runSize = 200000L;
        Stream<ItemSet> protStream = commonStream.map(r -> subSolveBoth(r, retMap, modelRet, protMap, modelProt, runSize))
                .filter(Objects::nonNull);

        Collection<ItemSet> best = protStream.collect(
                new TopCollectorReporting<>(s -> dualRating(s, modelRet, modelProt),
                        s -> reportBetter(s, modelRet, modelProt, retMap, protMap)));
        outputResult(best, modelProt, true);

        // TODO solve for challenge dps too
    }

    public static long dualRating(ItemSet set, ModelCombined modelRet, ModelCombined modelProt) {
        return modelRet.calcRating(set.otherSet) + modelProt.calcRating(set);
    }

    public static ItemSet subSolveBoth(ItemSet chosenSet, EquipOptionsMap retMap, ModelCombined modelRet, EquipOptionsMap protMap, ModelCombined modelProt, Long runSize) {
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
        return chooseEngineAndRun(model, submitMap, null, runSize, otherSet, null);
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcess(EquipOptionsMap reforgedItems, ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        Optional<ItemSet> bestSet = chooseEngineAndRun(model, reforgedItems, startTime, BILLION, null, null);
        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, reforgedItems, model);
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlus(EquipOptionsMap reforgedItems, ModelCombined model, Instant startTime, boolean detailedOutput, int extraItemId, boolean replace, boolean defaultEnchants, StatBlock extraItemEnchants) throws IOException {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        Function<ItemData, ItemData> enchanting =
                extraItemEnchants != null ? x -> x.changeFixed(extraItemEnchants) :
                        defaultEnchants ? x -> ItemUtil.defaultEnchants(x, model, true) :
                                Function.identity();

        SlotEquip slot = extraItem.slot.toSlotEquip();
        EquipOptionsMap runItems = reforgedItems.deepClone();
        ItemData extraItem1 = addExtra(runItems, model, extraItemId, slot, enchanting, null, replace, true);
        ArrayUtil.mapInPlace(runItems.get(slot), enchanting);

        if (detailedOutput) {
            System.out.println("EXTRA " + extraItem1);
        }

        long runSize = BILLION;
        Optional<ItemSet> bestSet = chooseEngineAndRun(model, runItems, startTime, runSize, null, null);
        outputResult(bestSet, model, detailedOutput);
        if (bestSet.isEmpty() && detailedOutput) {
            outputFailureDetails(model, runItems);
        }
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        return addExtra(reforgedItems, model, extraItemId, extraItem.slot.toSlotEquip(), customiseItem, reforge, replace, customiseOthersInSlot);
    }

    public static ItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, SlotEquip slot, Function<ItemData, ItemData> customiseItem, ReforgeRecipe reforge, boolean replace, boolean customiseOthersInSlot) {
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
    public static void reforgeAlternatives(Path file, ModelCombined model, Instant startTime, int[] alternateItems) throws IOException {
        EquipOptionsMap reforgedItems = ItemUtil.readAndLoad(itemCache, false, file, model.reforgeRules(), null);

        for (int extraItemId : alternateItems) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            EquipOptionsMap itemMap = reforgedItems.copyWithReplaceSingle(extraItem.slot.toSlotEquip(), extraItem);
            Optional<ItemSet> bestSets = EngineStream.runSolver(model, itemMap, null, null, null, 0);
            outputResult(bestSets, model, false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlusPlus(EquipOptionsMap reforgedItems, ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2) throws IOException {
        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, model, true);

        ItemData extraItem1 = addExtra(reforgedItems, model, extraItemId1, enchant, null, false, true);
        System.out.println("EXTRA " + extraItem1);

        ItemData extraItem2 = addExtra(reforgedItems, model, extraItemId2, enchant, null, false, true);
        System.out.println("EXTRA " + extraItem2);

        Optional<ItemSet> best = chooseEngineAndRun(model, reforgedItems, startTime, BILLION * 3, null, null);
        outputResult(best, model, true);
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, Tuple.Tuple2<Integer, Integer>[] extraItems) {
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
        Optional<ItemSet> best = chooseEngineAndRun(model, items, startTime, runSize, null, null);
        outputResult(best, model, true);
    }

    public static void outputResult(Collection<ItemSet> bestSets, ModelCombined model, boolean detailedOutput) {
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

    public static void outputResult(Optional<ItemSet> bestSet, ModelCombined model, boolean detailedOutput) {
        if (bestSet.isPresent()) {
            bestSet.get().outputSet(model);
        } else {
            System.out.println("@@@@@@@@@ NO VALID SET RESULTS @@@@@@@@@");
        }
    }

    public static void outputTweaked(Optional<ItemSet> bestSet, EquipOptionsMap reforgedItems, ModelCombined model) {
        if (bestSet.isPresent()) {
            outputTweaked(bestSet.get(), reforgedItems, model);
        }
    }

    public static void outputTweaked(ItemSet bestSet, EquipOptionsMap reforgedItems, ModelCombined model) {
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

    private static void reportBetter(ItemSet itemSet, ModelCombined modelRet, ModelCombined modelProt, EquipOptionsMap itemsRet, EquipOptionsMap itemsProt) {
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

    private static void outputFailureDetails(ModelCombined model, EquipOptionsMap runItems) {
        FindStatRange.checkSetReport(model, runItems);
    }
}
