package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.util.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class SolverCapPhased {
    public static final int TOP_HIT_COMBO_FILTER = 400;
    private final ModelCombined model;
    private final StatRequirements.StatRequirementsSkinnySupport requirements;
    private final StatBlock adjustment;
    private final PrintRecorder printRecorder;
    private EquipOptionsMap fullItems;
    private List<SkinnyItem[]> skinnyOptions;

    public SolverCapPhased(ModelCombined model, StatBlock adjustment, PrintRecorder printRecorder) {
        if (!supportedModel(model))
            throw new IllegalArgumentException("can't use this model without skinny support");
        this.model = model;
        this.requirements = (StatRequirements.StatRequirementsSkinnySupport) model.statRequirements();
        this.adjustment = adjustment;
        this.printRecorder = printRecorder;
    }

    public static boolean supportedModel(ModelCombined model) {
        return (model.statRequirements() instanceof StatRequirements.StatRequirementsSkinnySupport);
    }

    public long initAndCheckSizes(EquipOptionsMap items) {
        fullItems = items;
        skinnyOptions = convertToSkinny(items);

        return BigStreamUtil.estimateSets(skinnyOptions);
    }

    public Optional<ItemSet> runSolver(boolean parallel, Predicate<ItemSet> specialFilter, Long topCombosMultiply) {
        try {
            Stream<ItemSet> partialSets = runSolverPartial(parallel, topCombosMultiply);
            Stream<ItemSet> finalSets = model.filterSets(partialSets, true);
            if (specialFilter != null)
                finalSets = finalSets.filter(specialFilter);
            return BigStreamUtil.findBest(model, finalSets);
        } finally {
            postSolve();
        }
    }

    private void postSolve() {
//        System.out.printf("PHASED COUNTS est=%d init=%d filter=%d filter2=%d\n", estimate, initCount.get(), filterCount.get(), filter2Count.get());
    }

    private Stream<ItemSet> runSolverPartial(boolean parallel, Long topCombosMultiply) {
        Stream<SkinnyItemSet> initialSets = generateSkinnyComboStream(skinnyOptions, parallel);

        Stream<SkinnyItemSet> filteredSets = requirements.filterSetsSkinny(initialSets);

        if (topCombosMultiply != null) {
            int actualTop = (int) (topCombosMultiply * TOP_HIT_COMBO_FILTER);
            printRecorder.printf("SKINNY COMBOS TOO BIG JUST CONSIDERING %,d\n", actualTop);
            ToLongFunction<SkinnyItemSet> ratingFunc = is -> is.totalOne() + is.totalTwo();

//            filteredSets = filteredSets.filter(new BottomNFilter<>(actualTop, ratingFunc));

            // try a Top Collector (with good merging), re-stream combo
            filteredSets = filteredSets.collect(new BottomCollectorN<>(actualTop, ratingFunc))
                    .parallelStream();

            // TODO multiple sets with equal superhit may be lost
        }

        return filteredSets.map(this::makeFromSkinny);
    }

    private ItemSet makeFromSkinny(SkinnyItemSet skinnySet) {
        EquipMap chosenItems = EquipMap.empty();
        CurryQueue<SkinnyItem> itemQueue = skinnySet.items();
        while (itemQueue != null) {
            SkinnyItem skinny = itemQueue.item();
            SlotEquip slot = skinny.slot();
            ItemData[] fullSlotItems = fullItems.get(slot);

            BestHolder<ItemData> bestSlotItem = new BestHolder<>();
            for (ItemData item : fullSlotItems) {
                if (requirements.skinnyMatch(skinny, item)) {
                    long rating = model.statRatings().calcRating(item.stat, item.statFixed);
                    bestSlotItem.add(item, rating);
                }
            }

            chosenItems.put(slot, bestSlotItem.get());
            itemQueue = itemQueue.tail();
        }
        return ItemSet.manyItems(chosenItems, adjustment);
    }

    private List<SkinnyItem[]> convertToSkinny(EquipOptionsMap items) {
        List<SkinnyItem[]> optionsList = new ArrayList<>();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] fullOptions = items.get(slot);
            if (fullOptions != null) {
                HashSet<SkinnyItem> slotSet = new HashSet<>();
                for (ItemData item : fullOptions) {
                    SkinnyItem skinny = requirements.toSkinny(slot, item);
                    slotSet.add(skinny);
                }
                SkinnyItem[] slotArray = slotSet.toArray(SkinnyItem[]::new);
                optionsList.add(slotArray);
            }
        }
        return optionsList;
    }

    private Stream<SkinnyItemSet> generateSkinnyComboStream(List<SkinnyItem[]> optionsList, boolean parallel) {
        Stream<SkinnyItemSet> stream = null;

//        optionsList.sort(Comparator.comparingInt(array -> array.length));

        for (SkinnyItem[] slotOptions : optionsList) {
            if (stream == null) {
                stream = newCombinationStream(slotOptions, parallel);
            } else {
                stream = addSlotToCombination(stream, slotOptions);
            }
            stream = requirements.filterSetsMaxSkinny(stream);
        }

        return stream;
    }

    private Stream<SkinnyItemSet> addSlotToCombination(Stream<SkinnyItemSet> stream, SkinnyItem[] slotOptions) {
        return stream.mapMulti((set, next) -> {
            for (SkinnyItem item : slotOptions) {
                next.accept(set.withAddedItem(item));
            }
        });
    }

    private Stream<SkinnyItemSet> newCombinationStream(SkinnyItem[] slotOptions, boolean parallel) {
        final SkinnyItemSet[] initialSets = new SkinnyItemSet[slotOptions.length];
        for (int i = 0; i < slotOptions.length; ++i) {
            SkinnyItem item = slotOptions[i];
            initialSets[i] = SkinnyItemSet.single(item);
        }
        return parallel ? ArrayUtil.arrayStream(initialSets) : Arrays.stream(initialSets);
    }
}
