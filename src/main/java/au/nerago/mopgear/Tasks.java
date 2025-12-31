package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.DataLocation;
import au.nerago.mopgear.io.SourcesOfItems;
import au.nerago.mopgear.io.StandardModels;
import au.nerago.mopgear.io.WowSimDB;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static au.nerago.mopgear.domain.StatType.Crit;
import static au.nerago.mopgear.domain.StatType.Haste;
import static au.nerago.mopgear.io.SourcesOfItems.*;

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

    public static void findBIS(ModelCombined model, CostedItem[] allItems, Instant startTime, int upgradeLevel, boolean requireFullSetBonus) {
        EquipOptionsMap optionsMap = EquipOptionsMap.empty();
        Arrays.stream(allItems).flatMap(equip -> ItemLoadUtil.loadItemBasicWithRandomVariants(equip.itemId(), upgradeLevel).stream())
                .filter(item -> item.slot() != SlotItem.Weapon2H)
                .forEach(item -> {
                    item = ItemLoadUtil.defaultEnchants(item, model, true, false);
                    SlotEquip[] slotOptions = item.slot().toSlotEquipOptions();
                    List<FullItemData> reforged = Reforger.reforgeItem(model.reforgeRules(), item);
                    for (SlotEquip slot : slotOptions) {
                        optionsMap.put(slot, ArrayUtil.concatNullSafe(optionsMap.get(slot), reforged));
                    }
                });

        JobInput job = new JobInput();
        job.model = model;
        job.setItemOptions(optionsMap);
        job.startTime = startTime;

        job.forceSkipIndex = true;
        job.forcedRunSized = BILLION/6;

//        job.forcePhased = true;
//        job.runSizeMultiply = 10;

//        job.forcedRunSized = BILLION;
//        job.forcedRunSized = BILLION*4;
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
        Arrays.stream(allItems)
                .peek(costed -> costs.put(costed.itemId(), costed.cost()))
                .map(equip -> ItemLoadUtil.loadItemBasic(equip.itemId(), upgradeLevel))
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
            FullItemData item = ItemLoadUtil.loadItemBasic(itemId, 2);
            ranked.add(item, model.calcRating(item));
        }

        ranked.forEach((item, rate) -> {
            OutputText.printf("%10d %s\n", Math.round(rate), item.toStringExtended());
        });
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

            FullItemSet set = Solver.chooseEngineAndRun(model, submitMap, null, null).orElseThrow();
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
        Optional<FullItemSet> resultSet = output.getFinalResultSet();

        outputResultSimple(resultSet, model, true);
//        outputReforgeJson(resultSet);
        outputTweaked(output.resultSet, itemOptions, model);
    }

    @SuppressWarnings("SameParameterValue")
    public static void reforgeProcessPlus(EquipOptionsMap itemOptions, ModelCombined model, Instant startTime, SlotEquip slot, int extraItemId, int upgradeLevel, boolean replace, EnchantMode enchantMode, StatBlock adjustment, boolean alternateEnchantsAllSlots) {
        FullItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel);

        if (slot == null)
            slot = extraItem.slot().toSlotEquip();

        EquipOptionsMap runItems = itemOptions.deepClone();
        extraItem = addExtra(runItems, model, extraItemId, upgradeLevel, slot, enchantMode, null, replace, true);
        OutputText.println("EXTRA " + extraItem);

        if (alternateEnchantsAllSlots)
            ItemLoadUtil.duplicateAlternateEnchants(runItems, model);

        JobInput job = new JobInput();
        job.printRecorder.outputImmediate = true;
        job.config(model, runItems, startTime, adjustment);
        job.runSizeMultiply = 8;
        JobOutput output = Solver.runJob(job);
        Optional<FullItemSet> resultSet = output.getFinalResultSet();

        outputResultSimple(resultSet, model, true);
        if (resultSet.isEmpty()) {
            outputFailureDetails(model, runItems, job.printRecorder);
        }
    }

    public static FullItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, int upgradeLevel, EnchantMode enchantMode, ReforgeRecipe reforge, boolean replace, boolean errorOnExists) {
        FullItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel);
        return addExtra(reforgedItems, model, extraItem, enchantMode, reforge, replace, errorOnExists);
    }

    public static FullItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, int extraItemId, int upgradeLevel, SlotEquip slot, EnchantMode enchantMode, ReforgeRecipe reforge, boolean replace, boolean errorOnExists) {
        FullItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel);
        return addExtra(reforgedItems, model, extraItem, slot, enchantMode, reforge, replace, errorOnExists);
    }

    public static FullItemData addExtra(EquipOptionsMap reforgedItems, ModelCombined model, FullItemData extraItem, EnchantMode enchantMode, ReforgeRecipe reforge, boolean replace, boolean errorOnExists) {
        return addExtra(reforgedItems, model, extraItem, extraItem.slot().toSlotEquip(), enchantMode, reforge, replace, errorOnExists);
    }

    @Nullable
    private static FullItemData addExtra(EquipOptionsMap itemOptions, ModelCombined model, FullItemData extraItem, SlotEquip slot, EnchantMode enchantMode, ReforgeRecipe recipe, boolean replace, boolean errorOnExists) {
        ItemRef ref = extraItem.ref();
        FullItemData[] existing = itemOptions.get(slot);
        HashSet<FullItemData> resultingList = new HashSet<>();

        if (slot == SlotEquip.Weapon && existing != null && extraItem.slot() != existing[0].slot()) {
            OutputText.println("WRONG WEAPON " + extraItem);
            return null;
        } else if (ArrayUtil.anyMatch(existing, item -> item.ref().equalsTyped(ref))) {
            if (errorOnExists)
                throw new IllegalArgumentException("item already included " + extraItem);
            OutputText.println("ALREADY INCLUDED " + extraItem);
            return null;
        } else if (existing == null) {
            throw new IllegalArgumentException("can't add extra to empty slot");
        } else if (replace) {
            OutputText.println("REPLACING " + existing[0]);
        }

        StatBlock gemStat = model.gemChoiceBestAlternate();
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

        listSlotContent(itemOptions, slot);

        return forged.getFirst();
    }

    private static void listSlotContent(EquipOptionsMap reforgedItems, SlotEquip slot) {
        FullItemData[] slotArray = reforgedItems.get(slot);
        HashSet<ItemRef> seen = new HashSet<>();
        ArrayUtil.forEach(slotArray, it -> {
            if (seen.add(it.ref())) {
                OutputText.println("NEW " + slot + " " + it);
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

        JobInput job = new JobInput();
        job.config(model, runItems, startTime, adjustment);
        job.runSizeMultiply = 16;
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
        for (CostedItem entry : extraItems) {
            int extraItemId = entry.itemId();
            if (SourcesOfItems.ignoredItems.contains(extraItemId)) continue;
            FullItemData extraItem = ItemLoadUtil.loadItemBasic(extraItemId, upgradeLevel);
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

        JobInput job = new JobInput();
        job.model = model;
        job.setItemOptions(items);
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
//        job.runSizeMultiply = 2;
//        job.runSizeMultiply = 12;
//        job.runSizeMultiply = 42;

        job.forcePhased = true;
        job.runSizeMultiply = 512;
        job.specialFilter = specialFilter;

        JobOutput output = Solver.runJob(job);
        Optional<FullItemSet> resultSet = output.getFinalResultSet();
        outputResultSimple(resultSet, model, true);
        outputTweaked(output.resultSet, items, model);
    }

    public static void reforgeProcessPlusMany(EquipOptionsMap items, ModelCombined model, Instant startTime, List<EquippedItem> extraItems) {
        EquipOptionsMap itemsOriginal = items.deepClone();

        for (EquippedItem entry : extraItems) {
            if (SourcesOfItems.ignoredItems.contains(entry.itemId())) continue;
            FullItemData extraItem = ItemLoadUtil.loadItem(entry, model.enchants(), true);
            for (SlotEquip slot : extraItem.slot().toSlotEquipOptions()) {
                FullItemData[] existing = items.get(slot);
                if (existing == null) {
                    OutputText.println("SKIP SLOT NOT NEEDED " + extraItem);
                } else if (ArrayUtil.anyMatch(existing, item -> item.ref().equalsTyped(extraItem.ref()))) {
                    OutputText.println("SKIP DUP " + extraItem);
                } else {
                    addExtra(items, model, extraItem, slot, EnchantMode.BothDefaultAndAlternate, null, false, true);
                }
            }
        }

        JobInput job = new JobInput();
        job.model = model;
        job.setItemOptions(items);
        job.startTime = startTime;
        job.printRecorder.outputImmediate = true;
        job.runSizeMultiply = 20;
        JobOutput output = Solver.runJob(job);
        job.printRecorder.outputNow();
        FullItemSet best = output.getFinalResultSet().orElseThrow();
        outputResultChanges(itemsOriginal, best, model);
    }

    public static void outputResultSimple(Optional<FullItemSet> bestSet, ModelCombined model, boolean detailedOutput) {
        if (bestSet.isPresent()) {
            if (detailedOutput) {
                bestSet.get().outputSetDetailed(model);
                OutputText.println();
                AsWowSimJson.writeToOutBasic(bestSet.get().items());
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

    public static void paladinMultiSpecSolve() {
        FindMultiSpec multi = new FindMultiSpec();
//        multi.addFixedForge(95757, new ReforgeRecipe(Crit, Haste)); // Primordius trinket
//        multi.addFixedForge(86802, ReforgeRecipe.empty()); // lei shen trinket
//        multi.addFixedForge(94526, ReforgeRecipe.empty()); // zandalar trinket
//        multi.addFixedForge(87050, new ReforgeRecipe(Parry, Haste)); // Offhand Steelskin, Qiang's Impervious Shield
//        multi.addFixedForge(87026, new ReforgeRecipe(Expertise, Haste)); // Back Cloak of Peacock Feathers
//        multi.addFixedForge(86957, new ReforgeRecipe(null, null)); // Ring Ring of the Bladed Tempest
//        multi.addFixedForge(86955, new ReforgeRecipe(Mastery, Expertise)); // Belt Waistplate of Overwhelming Assault
//        multi.addFixedForge(86387, new ReforgeRecipe(Hit, Haste)); // Weapon1H Kilrak, Jaws of Terror


        int extraUpgrade = 2;
        boolean preUpgrade = false;

        multi.addSpec(
                "RET",
                DataLocation.gearRetFile,
                StandardModels.pallyRetModel(),
                0.05,
                new int[]{
//                        87036, // heroic soulgrasp
//                        87026, // heroic peacock cloak
//                        86880, // dread shadow ring
//                        86955, // heroic overwhelm assault belt
//                        89954, // warbelt pods
//                        84949, // mal glad girdle accuracy
//                        89280, // voice helm
//                        86957, // heroic bladed tempest
//                        85343, // normal ret chest
//                        87071, // yang-xi heroic
//                        86681, // celestial ret head

//                        87015, // heroic clawfeet
                        86979, // heroic impaling treads
//                        87024, // null greathelm
//                        87145, // defiled earth
//                        85340, // normal ret legs
//                        87101, // heroic ret head
                        94726, // cloudbreaker belt
//                        95914, // ret tier15 shoulder
//                        95924, // prot tier15 shoulder
//                        95652, // Puncture-Proof Greathelm head
                        95535, // normal lightning legs

                        95914, // ret tier shoulder celestial
                        95910, // ret tier chest celestial
                        95910, // ret tier gloves celestial

                        95142, // striker's battletags
                        95205, // terra-cotta neck
//                        87036, // soulgrasp heroic

                },
                extraUpgrade,
                preUpgrade
        )
                .addRemoveItem(86680) // remove celestial ret legs
                .addRemoveItem(86681)// remove celestial ret head
        ;

        multi.addSpec(
                "PROT-DAMAGE",
                DataLocation.gearProtDpsFile,
                StandardModels.pallyProtDpsModel(),
                0.60,
                new int[]{
//                        86979, // heroic impaling treads
//                        87062 // elegion heroic
//                        86957, // heroic bladed tempest
//                        85343, // normal ret chest

//                        87015, // heroic clawfeet
//                        86979, // heroic impaling treads
//                        87071, // yang-xi heroic
//                        87145, // defiled earth
//                        85340, // normal ret legs
//                        87101, // heroic ret head
//                        86946, // ruby signet heroic
                        94726, // cloudbreaker belt

//                        87026, // heroic peacock cloak
                        86955, // heroic overwhelm assault belt
                        95535, // normal lightning legs

//                        87050, // steelskin heroic
//                        95768, // greatshield gloaming celestial
//                        95652, // Puncture-Proof Greathelm head
                        95687, // celestial beakbreaker cloak
                        95914, // ret tier shoulder celestial
                        95910, // ret tier chest celestial
                        95910, // ret tier gloves celestial
//                        95924, // prot tier shoulder celestial

                        95142, // striker's battletags
                        95205, // terra-cotta neck
                        87036, // soulgrasp heroic

                        96182, // ultimate prot of the emperor thunder
                },
                extraUpgrade,
                preUpgrade
        )
                .addRemoveItem(86680) // remove celestial ret legs
//                .setDuplicatedItems(Map.of(89934, 1)) // soul bracer
//                .setWorstCommonPenalty(98.5)
                .setWorstCommonPenalty(99)
        ;

        multi.addSpec(
                "PROT-DEFENCE",
                DataLocation.gearProtDefenceFile,
                StandardModels.pallyProtMitigationModel(),
                0.35,
                new int[]{
//                        86979, // heroic impaling treads
//                        87015, // clawfeet
//                        87062, // elegion heroic
//                        86957, // heroic bladed tempest
//                        87071, // yang-xi heroic
//                        87145, // defiled earth
//                        89934, // soul bracer
//                        87101, // heroic ret head
//                        87024, // null greathelm
//                        86946, // ruby signet heroic
//                        95142, // striker's battletags
//                        94726, // cloudbreaker belt
                        86955, // heroic overwhelm assault belt
//                        87060, // Star-Stealer Waistguard

                        87026, // heroic peacock cloak
                        86325, // daybreak
                        95687, // celestial beakbreaker cloak

                        95535, // normal lightning legs

//                        87050, // steelskin heroic
//                        95768, // greatshield gloaming celestial
//                        95652, // Puncture-Proof Greathelm head
//                        95808, // celestial whipping legs
//                        95874, // celestial bloody shoulders
//                        95914, // ret tier shoulder celestial
//                        95924, // prot tier shoulder celestial

//                        95142, // striker's battletags
                        95205, // terra-cotta neck
//                        87036, // soulgrasp heroic

                        96182, // ultimate prot of the emperor thunder
                },
                extraUpgrade,
                preUpgrade
        )
//                .setDuplicatedItems(Map.of(89934, 2)) // soul bracer
//                .addRemoveItem(89934) // soul bracer
//                .setWorstCommonPenalty(98.5)
                .setWorstCommonPenalty(99)
        ;

//        multi.multiSetFilter(proposedResults -> {
//            Set<Integer> uniqueItems = proposedResults.resultJobs().stream()
//                    .map(job -> job.resultSet.orElseThrow())
//                    .flatMap(itemSet -> itemSet.items().itemStream())
//                    .map(SolvableItem::itemId)
//                    .collect(Collectors.toSet());
//            return !uniqueItems.contains(87111) || !uniqueItems.contains(87101);
//        });

//        multi.suppressSlotCheck(86957);
//        multi.suppressSlotCheck(84829);
//        multi.suppressSlotCheck(86946);

//        multi.overrideEnchant(86905, StatBlock.of(StatType.Primary, 500));

//        multi.solve(1000);
//        multi.solve(15000);
//        multi.solve(50000);
//        multi.solve(120000);
//        multi.solve(220000);
//        multi.solve(490000);
        multi.solve(1490000);
//        multi.solve(4000000);
    }

    public static void druidMultiSpecSolve() {
        FindMultiSpec multi = new FindMultiSpec();

        multi.addSpec(
                "BOOM",
                DataLocation.gearBoomFile,
                StandardModels.modelFor(SpecType.DruidBoom),
                0.5,
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
                StandardModels.modelFor(SpecType.DruidTree),
                0.5,
                new int[]{
//                        86909 // regail dagger
                        88885, // Clever Ashyo's Armbands
                        86810, // Worldwaker Cabochon
//                        89078, // sagewhisper cloak
                },
                0,
                false
        );

//        multi.overrideEnchant(86865, StatBlock.empty); // no sha gem
//        multi.overrideEnchant(86893, StatBlock.empty); // no sha gem

        multi.solve(3000);
    }

    public static void compareBestReforgesWithCommon(Path file, ModelCombined model, Map<Integer, List<ReforgeRecipe>> commonOne, Map<Integer, List<ReforgeRecipe>> commonTwo) {
        EquipOptionsMap optionsOne = ItemLoadUtil.readAndLoad(file, model, commonOne, true);
        EquipOptionsMap optionsTwo = ItemLoadUtil.readAndLoad(file, model, commonTwo, true);

        int runSizeMultiply = 2;

        JobInput jobOne = new JobInput();
        jobOne.printRecorder.outputImmediate = true;
        jobOne.runSizeMultiply = runSizeMultiply;
        jobOne.model = model;
        jobOne.setItemOptions(optionsOne);
        JobOutput outputOne = Solver.runJob(jobOne);
        Optional<FullItemSet> resultOne = outputOne.getFinalResultSet();

        OutputText.println("111111111111111111111111111111111111");
        resultOne.orElseThrow().outputSetDetailed(model);
        double ratingOne = model.calcRating(resultOne.orElseThrow());

        JobInput jobTwo = new JobInput();
        jobTwo.printRecorder.outputImmediate = true;
        jobTwo.runSizeMultiply = runSizeMultiply;
        jobTwo.model = model;
        jobTwo.setItemOptions(optionsTwo);
        JobOutput outputTwo = Solver.runJob(jobTwo);
        Optional<FullItemSet> resultTwo = outputTwo.getFinalResultSet();

        OutputText.println("22222222222222222222222222222222222222");
        resultTwo.orElseThrow().outputSetDetailed(model);
        double ratingTwo = model.calcRating(resultTwo.orElseThrow());

        OutputText.printf("COMMON ITEM PENALTY PERCENT %1.3f\n", ratingOne / ratingTwo * 100);
    }

    public static void determineRatingMultipliers() {
        StatRatingsWeights tankMitigation = new StatRatingsWeights(StandardModels.specToWeightFile(SpecType.PaladinProtMitigation), false, true, false);
        StatRatingsWeights tankDps = new StatRatingsWeights(StandardModels.specToWeightFile(SpecType.PaladinProtDps), false, true, false);
        StatRatingsWeights retRet = new StatRatingsWeights(StandardModels.specToWeightFile(SpecType.PaladinRet));

        EquipOptionsMap itemsRet = ItemLoadUtil.readAndLoad(DataLocation.gearRetFile, ReforgeRules.melee(), new DefaultEnchants(SpecType.PaladinRet, true), null, true);
        EquipOptionsMap itemsTank = ItemLoadUtil.readAndLoad(DataLocation.gearProtDpsFile, ReforgeRules.tank(), new DefaultEnchants(SpecType.PaladinProtDps, true), null, true);

        double rateMitigation = determineRatingMultipliersOne(tankMitigation, itemsTank, StatRequirementsHitExpertise.protFlexibleParry(), SpecType.PaladinProtMitigation);
        double rateTankDps = determineRatingMultipliersOne(tankDps, itemsTank, StatRequirementsHitExpertise.protFlexibleParry(), SpecType.PaladinProtDps);
        double rateRet = determineRatingMultipliersOne(retRet, itemsRet, StatRequirementsHitExpertise.ret(), SpecType.PaladinRet);

        double targetCombined = 1000000000;

        OutputText.printf("MITIGATION %,d\n", (long)rateMitigation);
        OutputText.printf("TANK_DPS   %,d\n", (long)rateTankDps);
        OutputText.printf("RET        %,d\n", (long)rateRet);
        OutputText.println();

        int dmgPercentMit = 20, dmgPercentDps = 100 - dmgPercentMit;
        OutputText.printf("damageProtModel %d%% mitigation, %d%% dps\n", dmgPercentMit , dmgPercentDps);
        long dmgMultiplyA = Math.round(targetCombined * (dmgPercentMit / 100.0) / rateMitigation * 10);
        long dmgMultiplyB = Math.round(targetCombined * (dmgPercentDps / 100.0) / rateTankDps * 10);
        OutputText.printf("USE mitigation %d dps %d\n", dmgMultiplyA, dmgMultiplyB);
        double dmgTotal = dmgMultiplyA * rateMitigation + dmgMultiplyB * rateTankDps;
        OutputText.printf("EFFECTIVE %.2f %.2f\n",
                dmgMultiplyA * rateMitigation / dmgTotal,
                dmgMultiplyB * rateTankDps / dmgTotal);
        StatRatingsWeights dmgMix = StatRatingsWeights.mix(tankMitigation, (int) dmgMultiplyA, tankDps, (int) dmgMultiplyB, null);
        StatType dmgBestStat = dmgMix.bestNonHit();
        OutputText.printf("BEST STAT %s\n\n", dmgBestStat);

        int defPercentMit = 80, defPercentDps = 100 - defPercentMit;
        OutputText.printf("defenceProtModel %d%% mitigation, %d%% dps\n", defPercentMit , defPercentDps);
        long defMultiplyA = Math.round(targetCombined * (defPercentMit / 100.0) / rateMitigation * 10);
        long defMultiplyB = Math.round(targetCombined * (defPercentDps / 100.0) / rateTankDps * 10);
        OutputText.printf("USE mitigation %d dps %d\n", defMultiplyA, defMultiplyB);
        double defTotal = defMultiplyA * rateMitigation + defMultiplyB * rateTankDps;
        OutputText.printf("EFFECTIVE %.2f %.2f\n",
                defMultiplyA * rateMitigation / defTotal,
                defMultiplyB * rateTankDps / defTotal);
        StatRatingsWeights defMix = StatRatingsWeights.mix(tankMitigation, (int) defMultiplyA, tankDps, (int) defMultiplyB, null);
        StatType defBestStat = defMix.bestNonHit();
        OutputText.printf("BEST STAT %s\n\n", defBestStat);

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
        ModelCombined model = new ModelCombined(weights, req, ReforgeRules.tank(), null, SetBonus.empty(), spec, null);
        JobInput job = new JobInput();
        job.model = model;
        job.setItemOptions(items);
        JobOutput output = Solver.runJob(job);
        FullItemSet set = output.getFinalResultSet().orElseThrow();
        return model.calcRating(set);
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
////                    String ids = group.getValue().stream().map(item -> item.itemId() + "u" + item.shared.ref().upgradeLevel()).collect(Collectors.joining(","));
//                    String ids = group.getValue().stream().map(item -> String.valueOf(item.itemId()) ).collect(Collectors.joining(","));
//                    OutputText.printf("%d %d %s %s %s\n", minLevel, maxLevel, minLevel==maxLevel ? "SINGLE" : has535 ? "" : "MISSING535", name, ids);
//                });
                ;

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
}
