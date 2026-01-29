package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.*;
import au.nerago.mopgear.model.*;
import au.nerago.mopgear.permute.Solver;
import au.nerago.mopgear.process.*;
import au.nerago.mopgear.results.*;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.RankedGroupsCollection;
import au.nerago.mopgear.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static au.nerago.mopgear.domain.StatType.*;
import static au.nerago.mopgear.io.SourcesOfItems.*;
import static au.nerago.mopgear.results.JobInput.RunSizeCategory.*;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue", "unused"})
public class Tasks {
    public static final long BILLION = 1000 * 1000 * 1000;

    public static void findUpgrade(EquipOptionsMap baseItems, CostedItem[] extraItems, ModelCombined model, boolean allowHacks, StatBlock adjustment, int upgradeLevel, long multiply) {
        new FindUpgrades(model, allowHacks).setRunSizeMultiply(multiply).run(baseItems, extraItems, adjustment, upgradeLevel);
    }

    public static void findUpgrade(EquipOptionsMap baseItems, List<EquippedItem> extraItems, ModelCombined model, boolean allowHacks, StatBlock adjustment, long multiply) {
        new FindUpgrades(model, allowHacks).setRunSizeMultiply(multiply).run(baseItems, extraItems, adjustment);
    }

    public static void findUpgradeMaxedItems(EquipOptionsMap baseItems, List<EquippedItem> extraItems, ModelCombined model, boolean allowHacks, StatBlock adjustment) {
        new FindUpgrades(model, allowHacks).runMaxedItems(baseItems, extraItems, adjustment);
    }

    public static void findBIS(ModelCombined model, CostedItem[] allItems, Instant startTime, int upgradeLevel, boolean requireFullSetBonus) {
        EquipOptionsMap optionsMap = EquipOptionsMap.empty();
        Arrays.stream(allItems).flatMap(equip -> ItemLoadUtil.loadItemBasicWithRandomVariants(equip.itemId(), upgradeLevel, PrintRecorder.withAutoOutput())
                .stream())
                .filter(item -> item.slot() != SlotItem.Weapon2H)
                .forEach(item -> {
                    item = ItemLoadUtil.defaultEnchants(item, model, true, false);
                    SlotEquip[] slotOptions = item.slot().toSlotEquipOptions();
                    List<FullItemData> reforged = Reforger.reforgeItem(model.reforgeRules(), item);
                    for (SlotEquip slot : slotOptions) {
                        optionsMap.put(slot, ArrayUtil.concatNullSafe(optionsMap.get(slot), reforged));
                    }
                });

        JobInput job = new JobInput(Final, 1, false);
        job.model = model;
        job.setItemOptions(optionsMap);
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
        if (requireFullSetBonus) {
            job.specialFilter = set -> model.setBonus().countInAnySet(set.items()) >= 4;
        }
        JobOutput output = Solver.runJob(job);

        outputResultSimple(output.getFinalResultSet(), model, true);
    }

    public static void findBestBySlot(ModelCombined model, CostedItem[] allItems, Instant startTime, int upgradeLevel) {
        Map<Integer, Integer> costs = new HashMap<>();
        EquipOptionsMap optionsMap = EquipOptionsMap.empty();
        PrintRecorder print = PrintRecorder.swallow();
        Arrays.stream(allItems)
                .peek(costed -> costs.put(costed.itemId(), costed.cost()))
                .map(equip -> ItemLoadUtil.loadItemBasic(equip.itemId(), upgradeLevel, print))
                .filter(item -> item.slot() != SlotItem.Weapon2H)
                .forEach(item -> {
                    item = ItemLoadUtil.defaultEnchants(item, model, true, false);
                    SlotEquip[] slotOptions = item.slot().toSlotEquipOptions();
                    FullItemData[] reforged = Reforger.reforgeItemBest(model, item);
//                    ItemData[] reforged = new ItemData[] { item };
                    for (SlotEquip slot : slotOptions) {
                        optionsMap.put(slot, ArrayUtil.concatNullSafe(optionsMap.get(slot), reforged));
                    }
                });

        optionsMap.entryStream().forEach(
                tuple -> {
                    SlotEquip slot = tuple.a();
                    FullItemData[] options = tuple.b();
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

    public static void rankSingleItems(ModelCombined model, List<Integer> items) {
        RankedGroupsCollection<FullItemData> ranked = new RankedGroupsCollection<>();
        for (int itemId : items) {
            FullItemData item = ItemLoadUtil.loadItemBasic(itemId, 2, PrintRecorder.withAutoOutput());
            ranked.add(item, model.calcRating(item));
        }

        ranked.forEach((item, rate) ->
            OutputText.printf("%10d %s\n", Math.round(rate), item.toStringExtended())
        );
    }

    public static void rankAlternativeCombos(EquipOptionsMap baseOptions, ModelCombined model, Instant startTime, List<List<Integer>> comboListList) {
        BestHolder<List<FullItemData>> best = new BestHolder<>();
        for (List<Integer> combo : comboListList) {
            EquipOptionsMap submitMap = baseOptions.deepClone();
            List<FullItemData> optionItems = new ArrayList<>();
            for (int extraId : combo) {
                FullItemData item = addExtra(submitMap, model, extraId, 0, EnchantMode.Default, null, true, true);
                optionItems.add(item);
            }

            JobInput job = new JobInput(SubSolveItem, 1, false);
            job.model = model;
            job.setItemOptions(submitMap);
            job.startTime = null;
            job.adjustment = null;
            JobOutput output = Solver.runJob(job);
            job.printRecorder.outputNow();
            FullItemSet set = output.getFinalResultSet().orElseThrow();
            set.outputSet(model);
            long rating = model.calcRating(set);
            OutputText.println("RATING " + rating);
            OutputText.println();

            best.add(optionItems, rating);
        }

        OutputText.println(best.get().toString());
    }

    public static void reforgeProcess(EquipOptionsMap itemOptions, ModelCombined model, Instant startTime) {
        JobInput job = new JobInput(Final, 1, true);
        job.printRecorder.outputImmediate = true;
        job.hackAllow = true;
        job.model = model;
        job.setItemOptions(itemOptions);
        job.startTime = startTime;
        JobOutput output = Solver.runJob(job);
        Optional<FullItemSet> resultSet = output.getFinalResultSet();

        outputResultSimple(resultSet, model, true);
//        outputReforgeJson(resultSet);
        outputTweaked(output.resultSet, itemOptions, model);
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlus(EquipOptionsMap itemOptions, ModelCombined model, Instant startTime, SlotEquip slot, int extraItemId, int upgradeLevel, boolean replace, EnchantMode enchantMode, StatBlock adjustment, boolean alternateEnchantsAllSlots) {
        FullItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel, PrintRecorder.withAutoOutput());

        if (slot == null)
            slot = extraItem.slot().toSlotEquip();

        EquipOptionsMap runItems = itemOptions.deepClone();
        extraItem = addExtra(runItems, model, extraItemId, upgradeLevel, slot, enchantMode, null, replace, true);
        OutputText.println("EXTRA " + extraItem);

        if (alternateEnchantsAllSlots)
            ItemLoadUtil.duplicateAlternateEnchants(runItems, model);

        JobInput job = new JobInput(Final, 1, false);
        job.printRecorder.outputImmediate = true;
        job.model = model;
        job.setItemOptions(runItems);
        job.startTime = startTime;
        job.adjustment = adjustment;
        JobOutput output = Solver.runJob(job);
        Optional<FullItemSet> resultSet = output.getFinalResultSet();

        outputResultSimple(resultSet, model, true);
        if (resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job.printRecorder);
        }
    }

    public static FullItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, int upgradeLevel, EnchantMode enchantMode, ReforgeRecipe reforge, boolean replace, boolean errorOnExists) {
        FullItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel, PrintRecorder.withAutoOutput());
        return addExtra(reforgedItems, model, extraItem, enchantMode, reforge, replace, errorOnExists);
    }

    public static FullItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, int upgradeLevel, SlotEquip slot, EnchantMode enchantMode, ReforgeRecipe reforge, boolean replace, boolean errorOnExists) {
        FullItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel, PrintRecorder.withAutoOutput());
        return addExtra(reforgedItems, model, extraItem, slot, enchantMode, reforge, replace, errorOnExists, PrintRecorder.withAutoOutput());
    }

    public static FullItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, FullItemData extraItem, EnchantMode enchantMode, ReforgeRecipe reforge, boolean replace, boolean errorOnExists) {
        return addExtra(reforgedItems, model, extraItem, extraItem.slot().toSlotEquip(), enchantMode, reforge, replace, errorOnExists, PrintRecorder.withAutoOutput());
    }

    @Nullable
    private static FullItemData addExtra(EquipOptionsMap itemOptions, ModelCombined model, FullItemData extraItem, SlotEquip slot, EnchantMode enchantMode, ReforgeRecipe recipe, boolean replace, boolean errorOnExists, PrintRecorder printer) {
        ItemRef ref = extraItem.ref();
        FullItemData[] existing = itemOptions.get(slot);
        HashSet<FullItemData> resultingList = new HashSet<>();

        if (slot == SlotEquip.Weapon && existing != null && extraItem.slot() != existing[0].slot()) {
            printer.println("WRONG WEAPON " + extraItem);
            return null;
        } else if (ArrayUtil.anyMatch(existing, item -> item.ref().equalsTyped(ref))) {
            if (errorOnExists)
                throw new IllegalArgumentException("item already included " + extraItem);
            printer.println("ALREADY INCLUDED " + extraItem);
            return null;
        } else if (existing == null) {
            throw new IllegalArgumentException("can't add extra to empty slot");
        } else if (replace) {
            printer.println("REPLACING " + existing[0]);
        }

        Function<FullItemData, FullItemData> enchantDefault =
                item -> ItemLoadUtil.defaultEnchants(item, model, true, false);
        Function<FullItemData, FullItemData> enchantAlternate =
                item -> ItemLoadUtil.defaultEnchants(item, model, true, true);

        List<FullItemData> enchanted = new ArrayList<>();
        switch (enchantMode) {
            case None -> {
                enchanted.add(extraItem);
                if (!replace)
                    resultingList.addAll(List.of(existing));
            }
            case Default -> {
                enchanted.add(enchantDefault.apply(extraItem));
                if (!replace) {
                    resultingList.addAll(Arrays.stream(existing).map(enchantDefault).toList());
                }
            }
            case AlternateGem -> {
                enchanted.add(enchantAlternate.apply(extraItem));
                if (!replace) {
                    resultingList.addAll(Arrays.stream(existing).map(enchantAlternate).toList());
                }
            }
            case BothDefaultAndAlternate -> {
                enchanted.add(enchantDefault.apply(extraItem));
                enchanted.add(enchantAlternate.apply(extraItem));
                if (!replace) {
                    resultingList.addAll(Arrays.stream(existing).map(enchantDefault).toList());
                    resultingList.addAll(Arrays.stream(existing).map(enchantAlternate).toList());
                }
            }
        }

        List<FullItemData> forged;
        if (recipe != null) {
            forged = enchanted.stream().map(item -> Reforger.presetReforge(item, recipe)).toList();
        } else {
            forged = enchanted.stream().flatMap(item -> Reforger.reforgeItem(model.reforgeRules(), item).stream()).toList();
        }

        resultingList.addAll(forged);
        itemOptions.put(slot, resultingList.toArray(FullItemData[]::new));

        listSlotContent(itemOptions, slot, printer);

        return forged.getFirst();
    }

    private static void listSlotContent(EquipOptionsMap reforgedItems, SlotEquip slot, PrintRecorder printer) {
        FullItemData[] slotArray = reforgedItems.get(slot);
        HashSet<ItemRef> seen = new HashSet<>();
        ArrayUtil.forEach(slotArray, it -> {
            if (seen.add(it.ref())) {
                printer.println("NEW " + slot + " " + it);
            }
        });
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlusPlus(EquipOptionsMap runItems, ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2, int upgradeLevel, boolean replace, StatBlock adjustment, boolean alternateEnchants) {
//        Function<ItemData, ItemData> enchant2 = x -> x.changeFixed(new StatBlock(285,90,0,165,0,0,320,0,0,0));

        FullItemData extraItem1 = addExtra(runItems, model, extraItemId1, upgradeLevel, EnchantMode.BothDefaultAndAlternate, null, replace, true);
        OutputText.println("EXTRA " + extraItem1);
        OutputText.println();

        FullItemData extraItem2 = addExtra(runItems, model, extraItemId2, upgradeLevel, EnchantMode.BothDefaultAndAlternate,  null, replace, true);
        OutputText.println("EXTRA " + extraItem2);
        OutputText.println();

        if (alternateEnchants)
            ItemLoadUtil.duplicateAlternateEnchants(runItems, model);

        JobInput job = new JobInput(Final, 20, false);
        job.model = model;
        job.setItemOptions(runItems);
        job.startTime = startTime;
        job.adjustment = adjustment;
        job.printRecorder.outputImmediate = true;
        JobOutput output = Solver.runJob(job);
        Optional<FullItemSet> resultSet = output.getFinalResultSet();

        outputResultSimple(resultSet, model, true);
        if (resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job.printRecorder);
        }
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, int[] extraItems, int upgradeLevel, boolean alternateEnchants, Predicate<SolvableItemSet> specialFilter) {
        CostedItem[] extraItemsCost = Arrays.stream(extraItems).mapToObj(id -> new CostedItem(id, -1)).toArray(CostedItem[]::new);
        reforgeProcessPlusMany(items, model, startTime, extraItemsCost, upgradeLevel, alternateEnchants, specialFilter);
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, CostedItem[] extraItems, int upgradeLevel, boolean alternateEnchants, Predicate<SolvableItemSet> specialFilter) {
        for (CostedItem entry : Arrays.stream(extraItems).distinct().toList()) {
            int extraItemId = entry.itemId();
            if (SourcesOfItems.ignoredItems.contains(extraItemId)) continue;
            FullItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel, PrintRecorder.withAutoOutput());
            for (SlotEquip slot : extraItem.slot().toSlotEquipOptions()) {
                FullItemData[] existing = items.get(slot);
                if (existing == null) {
                    OutputText.println("SKIP SLOT NOT NEEDED " + extraItem);
                } else if (ArrayUtil.anyMatch(existing, item -> item.ref().equalsTyped(extraItem.ref()))) {
                    OutputText.println("SKIP DUP " + extraItem);
                } else {
                    addExtra(items, model, extraItemId, upgradeLevel, slot, EnchantMode.BothDefaultAndAlternate, null, false, true);
                }
            }
        }

        if (alternateEnchants)
            ItemLoadUtil.duplicateAlternateEnchants(items, model);

//        items.put(SlotEquip.Leg, Arrays.stream(items.get(SlotEquip.Leg)).filter(x -> x.itemId() == 87071).toList());

//        JobInput job = new JobInput(Final, 4, false);
        JobInput job = new JobInput(Final, 20, false);
        job.model = model;
        job.setItemOptions(items);
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
//        job.forceMethod = JobInput.SolveMethod.PhasedTop;
//        job.runSizeMultiply = 2;
//        job.runSizeMultiply = 12;
//        job.runSizeMultiply = 42;

        job.specialFilter = specialFilter;

        JobOutput output = Solver.runJob(job);
        Optional<FullItemSet> resultSet = output.getFinalResultSet();
        outputResultSimple(resultSet, model, true);
        outputTweaked(output.resultSet, items, model);
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, List<EquippedItem> extraItems) {
        EquipOptionsMap itemsOriginal = items.deepClone();

        PrintRecorder printer = new PrintRecorder();
        printer.outputImmediate = true;
        plusManyItems(items, model, extraItems, printer);

        JobInput job = new JobInput(Final, 1, false);
        job.model = model;
        job.setItemOptions(items);
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
        JobOutput output = Solver.runJob(job);
        job.printRecorder.outputNow();
        FullItemSet best = output.getFinalResultSet().orElseThrow();
        outputResultChanges(itemsOriginal, best, model);
    }

    public static JobOutput reforgeProcessPlusManyQuiet(EquipOptionsMap items, ModelCombined model, List<EquippedItem> extraItems, JobInput.RunSizeCategory sizeCategory, int sizeMultiply) {
        items = items.deepClone();

        PrintRecorder printRecorder = new PrintRecorder();
        plusManyItems(items, model, extraItems, printRecorder);

        JobInput job = new JobInput(sizeCategory, sizeMultiply, false);
        job.printRecorder.append(printRecorder);
        job.model = model;
        job.setItemOptions(items);
        return Solver.runJob(job);
    }

    private static void plusManyItems(EquipOptionsMap items, ModelCombined model, List<EquippedItem> extraItems, PrintRecorder printer) {
        for (EquippedItem entry : extraItems) {
            if (SourcesOfItems.ignoredItems.contains(entry.itemId())) continue;
            FullItemData extraItem = ItemLoadUtil.loadItem(entry, model.enchants(), printer);
            for (SlotEquip slot : extraItem.slot().toSlotEquipOptions()) {
                FullItemData[] existing = items.get(slot);
                if (existing == null) {
                    printer.println("SKIP SLOT NOT NEEDED " + extraItem);
                } else if (ArrayUtil.anyMatch(existing, item -> item.ref().equalsTyped(extraItem.ref()))) {
                    printer.println("SKIP DUP " + extraItem);
                } else {
                    addExtra(items, model, extraItem, slot, EnchantMode.BothDefaultAndAlternate, null, false, true, printer);
                }
            }
        }
    }

    public static void outputResultSimple(Optional<FullItemSet> bestSet, ModelCombined model, boolean detailedOutput) {
        if (bestSet.isPresent()) {
            if (detailedOutput) {
                bestSet.get().outputSetDetailed(model);
                OutputText.println();
                AsWowSimJson.writeFullToOut(bestSet.get().items(), model);
                OutputText.println();
                bestSet.get().outputSetLight();
            } else {
                bestSet.get().outputSet(model);
            }
        } else {
            OutputText.println("@@@@@@@@@ NO VALID SET RESULTS @@@@@@@@@");
        }
    }

    private static void outputResultChanges(EquipOptionsMap baseline, FullItemSet best, ModelCombined model) {
        best.outputSetDetailed(model);
        best.outputSetLight();

        OutputText.println("CHANGES vvvvvvvvvvvvvvv CHANGES");
        for (SlotEquip slot : SlotEquip.values()) {
            FullItemData[] options = baseline.get(slot);
            FullItemData choice = best.items().get(slot);
            if (choice != null) {
                boolean existing = ArrayUtil.anyMatch(options, x -> x.ref().equalsTyped(choice.ref()));
                if (existing)
                    OutputText.println(" == " + choice.toStringExtended());
                else
                    OutputText.println(">>> " + choice.toStringExtended());
            }
        }
    }

    private static void outputReforgeJson(Optional<FullItemSet> resultSet, ModelCombined model) {
        resultSet.ifPresent(itemSet -> AsWowSimJson.writeFullToOut(itemSet.items(), model));
    }

    public static void outputTweaked(Optional<SolvableItemSet> bestSet, EquipOptionsMap itemOptions, ModelCombined model) {
        bestSet.ifPresent(itemSet -> outputTweaked(itemSet, new SolvableEquipOptionsMap(itemOptions), itemOptions, model));
    }

    public static void outputTweaked(SolvableItemSet bestSet, SolvableEquipOptionsMap itemOptions, EquipOptionsMap itemOptionsFull, ModelCombined model) {
        SolvableItemSet tweakSet = Tweaker.tweak(bestSet, model, itemOptions, null);
        Function<SolvableItem, FullItemData> fullItemMapper = ItemMapUtil.mapperToFullItems(itemOptionsFull);
        if (bestSet != tweakSet) {
            OutputText.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");

            OutputText.println(tweakSet.totalForRating().toStringExtended() + " " + model.calcRating(tweakSet));
            OutputText.println(tweakSet.totalForCaps().toStringExtended());
            for (SlotEquip slot : SlotEquip.values()) {
                SolvableItem orig = bestSet.items().get(slot);
                SolvableItem change = tweakSet.items().get(slot);
                if (orig != null && change != null) {
                    if (!orig.isIdenticalItem(change)) {
                        FullItemData change2 = fullItemMapper.apply(change);
                        OutputText.println(change2 + " " + model.calcRating(change2));
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

    public static void druidMultiSpecSolve() {
        FindMultiSpec multi = new FindMultiSpec(1);

        multi.addSpec(
                "BOOM",
                DataLocation.gearBoomFile,
                StandardModels.modelFor(SpecType.DruidBoom),
                0.5,
                false,
                new int[]{
//                        86909 // regail dagger
//                        86694, // Eternal Blossom Mantle
//                        88885, // Clever Ashyo's Armbands
                        86748, // Cape of Three Lanterns
//                        89078, // sagewhisper cloak
                },
                0,
                false);

        multi.addSpec(
                "TREE",
                DataLocation.gearTreeFile,
                StandardModels.modelFor(SpecType.DruidTree),
                0.5,
                false,
                new int[]{
//                        86909 // regail dagger
                        88885, // Clever Ashyo's Armbands
                        86810, // Worldwaker Cabochon
//                        89078, // sagewhisper cloak
                },
                0,
                false);

//        multi.overrideEnchant(86865, StatBlock.empty); // no sha gem
//        multi.overrideEnchant(86893, StatBlock.empty); // no sha gem

        multi.solve(3000);
    }

    public static void determineRatingMultipliers() {
        StatRatingsWeights tankMitigation = new StatRatingsWeights(StandardModels.specToWeightFile(SpecType.PaladinProtMitigation), false, true, false);
        StatRatingsWeights tankDps = new StatRatingsWeights(StandardModels.specToWeightFile(SpecType.PaladinProtDps), false, true, false);
        StatRatingsWeights retRet = new StatRatingsWeights(StandardModels.specToWeightFile(SpecType.PaladinRet));

//        EquipOptionsMap itemsRet = ItemLoadUtil.readAndLoad(DataLocation.gearRetFile, ReforgeRules.melee(), new DefaultEnchants(SpecType.PaladinRet, true), null, PrintRecorder.withAutoOutput());
        EquipOptionsMap itemsTank = ItemLoadUtil.readAndLoad(DataLocation.gearProtDpsFile, ReforgeRules.tank(), new DefaultEnchants(SpecType.PaladinProtDps, true), null, PrintRecorder.withAutoOutput());

        double rateMitigation = determineRatingMultipliersOne(tankMitigation, itemsTank, StatRequirementsHitExpertise.protFlexibleParry(), SpecType.PaladinProtMitigation);
        double rateTankDps = determineRatingMultipliersOne(tankDps, itemsTank, StatRequirementsHitExpertise.protFlexibleParry(), SpecType.PaladinProtDps);
//        double rateRet = determineRatingMultipliersOne(retRet, itemsRet, StatRequirementsHitExpertise.ret(), SpecType.PaladinRet);

        double targetCombined = 1000000000;

        OutputText.printf("MITIGATION %,d\n", (long)rateMitigation);
        OutputText.printf("TANK_DPS   %,d\n", (long)rateTankDps);
//        OutputText.printf("RET        %,d\n", (long)rateRet);
        OutputText.println();

        int defPercentMit = 80, defPercentDps = 100 - defPercentMit;
        OutputText.printf("defenceProtModel %d%% mitigation, %d%% dps\n", defPercentMit , defPercentDps);
        long defMultiplyA = Math.round(targetCombined * (defPercentMit / 100.0) / rateMitigation * 10);
        long defMultiplyB = Math.round(targetCombined * (defPercentDps / 100.0) / rateTankDps * 10);
        OutputText.printf("USE mitigation %d dps %d\n", defMultiplyA, defMultiplyB);
        double defTotal = defMultiplyA * rateMitigation + defMultiplyB * rateTankDps;
        OutputText.printf("EFFECTIVE %.2f %.2f\n",
                defMultiplyA * rateMitigation / defTotal,
                defMultiplyB * rateTankDps / defTotal);
        StatRatingsWeights defMix = StatRatingsWeights.mix(tankMitigation, (int) defMultiplyA, tankDps, (int) defMultiplyB);
        StatType defBestStat = defMix.bestNonHit();
        OutputText.printf("BEST STAT %s\n\n", defBestStat);

        int dmgPercentMit = 10, dmgPercentDps = 100 - dmgPercentMit;
        OutputText.printf("damageProtModel %d%% mitigation, %d%% dps\n", dmgPercentMit , dmgPercentDps);
        long dmgMultiplyA = Math.round(targetCombined * (dmgPercentMit / 100.0) / rateMitigation * 10);
        long dmgMultiplyB = Math.round(targetCombined * (dmgPercentDps / 100.0) / rateTankDps * 10);
        OutputText.printf("USE mitigation %d dps %d\n", dmgMultiplyA, dmgMultiplyB);
        double dmgTotal = dmgMultiplyA * rateMitigation + dmgMultiplyB * rateTankDps;
        OutputText.printf("EFFECTIVE %.2f %.2f\n",
                dmgMultiplyA * rateMitigation / dmgTotal,
                dmgMultiplyB * rateTankDps / dmgTotal);
        StatRatingsWeights dmgMix = StatRatingsWeights.mix(tankMitigation, (int) dmgMultiplyA, tankDps, (int) dmgMultiplyB);
        StatType dmgBestStat = dmgMix.bestNonHit();
        OutputText.printf("BEST STAT %s\n\n", dmgBestStat);

//        double multiTargetCombined = 1000000000000L;
//        int multiRet = 5, multiDmg = 70, multiDef = 25;
//        OutputText.printf("multiSpec %d%% ret %d%% dmg_tank %d%% mitigation\n", multiRet, multiDmg, multiDef);
//        long multiA = Math.round(multiTargetCombined * (multiRet / 100.0) / rateRet);
//        long multiB = Math.round(multiTargetCombined * (multiDmg / 100.0) / dmgTotal);
//        long multiC = Math.round(multiTargetCombined * (multiDef / 100.0) / defTotal);
//        OutputText.printf("USE ret %d dmg_tank %d mitigation %d \n", multiA, multiB, multiC);
//        double multiTotal = multiA * rateRet + multiB * dmgTotal + multiC * defTotal;
//        OutputText.printf("EFFECTIVE %.2f %.2f %.2f\n\n",
//                multiA * rateRet / multiTotal,
//                multiB * dmgTotal / multiTotal,
//                multiC * defTotal / multiTotal);
    }

    private static long determineRatingMultipliersOne(StatRatingsWeights weights, EquipOptionsMap items, StatRequirements req, SpecType spec) {
        ModelCombined model = new ModelCombined(weights, req, ReforgeRules.tank(), null, SetBonus.forSpec(spec), spec, null);
        JobInput job = new JobInput(Medium, 1, true);
        job.model = model;
        job.setItemOptions(items);
        JobOutput output = Solver.runJob(job);
        FullItemSet set = output.getFinalResultSet().orElseThrow();
        return model.calcRating(set);
    }

    public static IntFunction<StatRatingsWeights> determineRatingMultipliersVariable() {
        StatRatingsWeights tankMitigation = new StatRatingsWeights(StandardModels.specToWeightFile(SpecType.PaladinProtMitigation), false, true, false);
        StatRatingsWeights tankDps = new StatRatingsWeights(StandardModels.specToWeightFile(SpecType.PaladinProtDps), false, true, false);

        EquipOptionsMap itemsTank = ItemLoadUtil.readAndLoad(DataLocation.gearProtDpsFile, ReforgeRules.tank(), new DefaultEnchants(SpecType.PaladinProtDps, true), null, PrintRecorder.withAutoOutput());

        double rateMitigation = determineRatingMultipliersOne(tankMitigation, itemsTank, StatRequirementsHitExpertise.protFlexibleParry(), SpecType.PaladinProtMitigation);
        double rateTankDps = determineRatingMultipliersOne(tankDps, itemsTank, StatRequirementsHitExpertise.protFlexibleParry(), SpecType.PaladinProtDps);

        double targetCombined = 10000000000L;

        return percentMiti -> {
            int percentDps = 100 - percentMiti;
            int multiplyA = Math.toIntExact(Math.round(targetCombined * (percentMiti / 100.0) / rateMitigation));
            int multiplyB = Math.toIntExact(Math.round(targetCombined * (percentDps / 100.0) / rateTankDps));
            double total = multiplyA * rateMitigation + multiplyB * rateTankDps;
            if (Math.abs(total - targetCombined) > targetCombined / 100)
                throw new RuntimeException("couldn't hit target within 1%");
            return StatRatingsWeights.mix(tankMitigation, multiplyA, tankDps, multiplyB);
        };
    }

    public static void optimalForVariedRating(EquipOptionsMap items, List<EquippedItem> extraItems) {
        IntFunction<StatRatingsWeights> modelGenerator = determineRatingMultipliersVariable();
        for (int percentMiti = 0; percentMiti <= 100; percentMiti += 5) {
            OutputText.printf("WEIGHTED SET %d\n", percentMiti);
            StatRatingsWeights weights = modelGenerator.apply(percentMiti);
//            ModelCombined model = StandardModels.pallyProtVariableModel(weights, percentMiti > 50);
            ModelCombined model = StandardModels.pallyProtVariableModel(weights, false);
            JobOutput result = reforgeProcessPlusManyQuiet(items, model, extraItems, Final, 1);
//            JobOutput result = reforgeProcessPlusManyQuiet(items, model, extraItems, Medium, 1);
//            result.input.printRecorder.outputNow();
            FullItemSet resultSet = result.getFinalResultSet().orElseThrow();
            resultSet.outputSetDetailed(model);
            AsWowSimJson.writeFullToOut(resultSet.items(), model);
            SimInputModify.makeWithGear(SpecType.PaladinProtMitigation, resultSet.items(), "PERCENT-" + percentMiti);
        }

        // TODO bonus set forced variants
        // what about soul barrier

    }

    public static void dumpTier2Gear() {
        try {
            dumpTier2GearInner();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dumpTier2GearInner() throws IOException {
        // phase == -1, challenge downscales? set items?
        // phase 1 = MSV, HOF, Toes
        // phase 2 = Dominance Offensive
        // phase 3 = thunder
        // phase 4 = unclear
        // phase 5 = seige

        //        WowSimDB.instance.itemStream()
//                .filter(item -> item.shared.phase() == -1)
//                .forEach(item -> OutputText.printf("%d %d %d %s\n", item.itemLevel(), item.shared.phase(), item.itemId(), item.fullName()));

//        WowSimDB.instance.itemStream()
//                .filter(item -> item.shared.phase() == 3)
//                .filter(item -> item.itemId() < 94000 || item.itemId() > 96999)
//                .filter(item -> !item.fullName().contains("Gladiator")
//                        && !item.fullName().contains("Thousandfold Hells")
//                        && !item.fullName().contains("Exorcist")
//                        && !item.fullName().contains("Nine-Tailed")
//                        && !item.fullName().contains("Saurok Stalker's")
//                        && !item.fullName().contains("Last Mogu")
//                        && !item.fullName().contains("All-Consuming Maw")
//                        && !item.fullName().contains("Chromatic Hydra")
//                        && !item.fullName().contains("Haunted Forest")
//                        && !item.fullName().contains("Witch Doctor")
//                        && !item.fullName().contains("Lightning Emperor's")
//                        && !item.fullName().contains("Fire-Charm")
//                )
//                .forEach(item -> OutputText.printf("%d %d %s\n", item.itemLevel(), item.shared.phase(), item.fullName()));


//        WowSimDB.instance.itemStream()
//                .filter(item -> item.shared.phase() == 3)
//                .filter(item -> item.shared.ref().upgradeLevel() == 0)
//                .filter(item -> !item.fullName().contains("Gladiator"))
//                .collect(Collectors.groupingBy(item -> item.shared.name()))
//                .entrySet().stream()
//                .sorted(Comparator.comparing(group -> group.getKey().toLowerCase()))
//                .forEach(group -> {
//                    String name = group.getKey();
//                    int minLevel = group.getValue().stream().mapToInt(FullItemData::itemLevel).min().orElseThrow();
//                    int maxLevel = group.getValue().stream().mapToInt(FullItemData::itemLevel).max().orElseThrow();
//                    boolean has535 = group.getValue().stream().mapToInt(FullItemData::itemLevel).filter(lvl -> lvl == 535).findAny().isPresent();
//                    String ids = group.getValue().stream().map(item -> item.itemId() + "u" + item.shared.ref().upgradeLevel()).collect(Collectors.joining(","));
//                    String ids = group.getValue().stream().map(item -> String.valueOf(item.itemId()) ).collect(Collectors.joining(","));
//                    OutputText.printf("%d %d %s %s %s\n", minLevel, maxLevel, minLevel==maxLevel ? "SINGLE" : has535 ? "" : "MISSING535", name, ids);
//                });


        List<String> lines = Files.readAllLines(Path.of("C:\\Users\\nicholas\\Dropbox\\PC\\Documents\\tempwow.txt"));
        Map<Integer, String> bossLookup = new HashMap<>();
        String currBoss = null;
        Pattern regex = Pattern.compile(".*(\\d{5}).*");
        for (String line : lines) {
            if (line.contains("ToT") || line.contains("--Nalak") || line.contains("--Oondasta") || line.contains("--Sunreaver Onslaught") || line.contains("--Kirin Tor Offensive") || line.contains("--Shado-Pan")) {
                currBoss = line.split("--")[1];
            }
            Matcher match = regex.matcher(line);
            if (match.matches()) {
                String num = match.group(1);
                bossLookup.put(Integer.valueOf(num), currBoss);
//                System.out.println(num + " " + currBoss);
            }
        }


        List<FullItemData> allItems = WowSimDB.instance.itemStream()
                .filter(item -> item.shared.phase() == 3)
                .filter(item -> item.shared.ref().upgradeLevel() == 0)
                .filter(item -> !item.fullName().contains("Gladiator"))
                .collect(Collectors.groupingBy(item -> item.shared.name()))
                .values().stream()
                .map(x -> SourcesOfItems.selectNormalHeroicThunderItem(x, Difficulty.Heroic))
                .sorted(Comparator.comparing(item -> item.fullName().toLowerCase()))
                .toList();

//        allItems.forEach(item -> {
//            String boss = bossLookup.getOrDefault(item.itemId(), "zUnknown");
//            OutputText.printf("%d,%s,%s,%s\n", item.itemId(), item.shared.name(), item.slot(), boss);
//        });

        allItems.stream().map(item -> {
            String boss = bossLookup.getOrDefault(item.itemId(), "Unknown");
            if (boss.equals("Unknown") && SourcesOfItems.isT15ClassSetItem(item)) {
                switch (item.slot()) {
                    case Chest -> boss = "Dark Animus ToT";
                    case Head -> boss = "Twin Consorts ToT";
                    case Shoulder -> boss = "Iron Qon ToT";
                    case Hand -> boss = "Council ToT";
                    case Leg -> boss = "Ji-Kun ToT";
                }
            }
            if (boss.equals("Unknown") && (item.fullName().contains("Haunted Steel") || item.fullName().contains("Dreadrunner")|| item.fullName().contains("Cloud Serpent")|| item.fullName().contains("Falling Blossom")|| item.fullName().contains("Quilen Hide")|| item.fullName().contains("Spirit Keeper")))
                boss = "Crafted";
            return Tuple.create(item.itemId(), item.shared.name(), item.slot(), boss);
        })
                .sorted(Comparator.comparing(Tuple.Tuple4::b))
                .sorted(Comparator.comparing(Tuple.Tuple4::d))
                .forEach(e -> OutputText.printf("%d,\"%s\",%s,%s\n", e.a(), e.b(), e.c(), e.d()));
    }

    public static void everyoneBis() {
//        SpecType spec = SpecType.WarriorProt;
//        ModelCombined model = StandardModels.modelFor(spec);
//        CostedItem[] allItems = ArrayUtil.concat(
//                new CostedItem[][] {
//                        strengthPlateThroneHeroic(),
//                        throneClassGearSetHeroic(spec, true),
//                        tankTrinketsThroneHeroic(),
//                        strengthDpsTrinketsThroneHeroic(),
//                }
//        );

//        SpecType spec = SpecType.MageFrost;
//        ModelCombined model = StandardModels.modelFor(spec);
//        CostedItem[] allItems = ArrayUtil.concat(
//                new CostedItem[][] {
//                        genericThroneHeroic(ArmorType.Cloth, PrimaryStatType.Intellect),
//                        throneClassGearSetHeroic(spec, true),
//                        intellectDpsTrinketsThroneHeroic()
//                }
//        );

        SpecType spec = SpecType.ShamanRestoration;
        ModelCombined model = StandardModels.modelFor(spec);
        CostedItem[] allItems = ArrayUtil.concat(
                new CostedItem[][] {
                        genericThroneNormalHeroic(ArmorType.Mail, PrimaryStatType.Intellect, Difficulty.Heroic),
                        throneClassGearSetHeroic(spec, Difficulty.Heroic),
                        intellectDpsTrinketsThroneHeroic(),
                        healTrinketsThroneHeroic()
                }
        );

        findBIS(model, allItems, Instant.now(), 2, false);
    }

    public static void generateRatingDataFromSims() {
        //        SimOutputReader.main();
        //
        try {
            SpecType spec = SpecType.PaladinProtMitigation;
            SimCliExecute.run(SimInputModify.inputFileFor(spec), SimInputModify.BASELINE_FILE);
            SimOutputReader.readInput(SimInputModify.BASELINE_FILE);

            int add = 800;
            StatType[] statsCheck = new StatType[]{Primary, Stam, Crit, Haste, Expertise, Mastery, Dodge, Parry};
            for (StatType stat : statsCheck) {
                Path inFile = SimInputModify.makeWithBonusStat(spec, stat, add);
                Path outFile = SimInputModify.outName(stat);

                SimCliExecute.run(inFile, outFile);

                System.out.println(stat);
                SimOutputReader.readInput(outFile);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void runPrebuiltSimFiles() {
        Pattern pattern = Pattern.compile("\\D*(\\d+)\\D*");
        ToIntFunction<String> extractNum = str -> { Matcher m = pattern.matcher(str); return m.matches() ? Integer.parseInt(m.group(1)) : -1; };

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(SimInputModify.basePath, "in-PERCENT-*.json")) {
            List<Tuple.Tuple2<Path, SimOutputReader.SimResultStats>> statList = StreamSupport.stream(dirStream.spliterator(), false)
                    .sorted(Comparator.comparingInt(x -> extractNum.applyAsInt(x.getFileName().toString())))
                    .map(inFile -> {
                        Path outFile = inFile.resolveSibling(inFile.getFileName() + ".out");
                        OutputText.println(inFile.toString());
                        SimCliExecute.run(inFile, outFile);
                        return Tuple.create(inFile, SimOutputReader.readInput(outFile));
                    })
                    .toList();

            OutputText.println(statList.stream().map(s -> s.a().getFileName().toString()).collect(Collectors.joining(",")));
            for (ToDoubleFunction<SimOutputReader.SimResultStats> stat : SimOutputReader.SimResultStats.eachStat()) {
                OutputText.println(statList.stream().map(s -> String.valueOf(stat.applyAsDouble(s.b()))).collect(Collectors.joining(",")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
