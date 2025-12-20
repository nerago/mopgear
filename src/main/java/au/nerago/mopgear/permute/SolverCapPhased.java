package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.util.*;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class SolverCapPhased {
    public static final int TOP_HIT_COMBO_FILTER = 400;
    protected final ModelCombined model;
    protected final StatRequirements requirements;
    protected final StatBlock adjustment;
    protected final PrintRecorder printRecorder;
    protected SolvableEquipOptionsMap fullItems;
    protected List<SkinnyItem[]> skinnyOptions;
    protected BigInteger estimate;
    protected final Long topCombosMultiply;

    public SolverCapPhased(ModelCombined model, StatBlock adjustment, PrintRecorder printRecorder, Long topCombosMultiply) {
        if (!supportedModel(model))
            throw new IllegalArgumentException("can't use this model without skinny support");
        this.model = model;
        this.requirements = model.statRequirements();
        this.adjustment = adjustment;
        this.printRecorder = printRecorder;
        this.topCombosMultiply = topCombosMultiply;
    }

    public static boolean supportedModel(ModelCombined model) {
        return model.statRequirements().skinnyRecommended();
    }

    public BigInteger initAndCheckSizes(SolvableEquipOptionsMap items) {
        fullItems = items;
        skinnyOptions = convertToSkinny(items);
        estimate = BigStreamUtil.estimateSets(skinnyOptions);
        return estimate;
    }

    public Optional<SolvableItemSet> runSolver(boolean parallel, Predicate<SolvableItemSet> specialFilter) {
        Stream<SolvableItemSet> partialSets = runSolverPartial(parallel);
        Stream<SolvableItemSet> finalSets = model.filterSets(partialSets, true);
        if (specialFilter != null)
            finalSets = finalSets.filter(specialFilter);
        return BigStreamUtil.findBest(model, finalSets);
    }

    private Stream<SolvableItemSet> runSolverPartial(boolean parallel) {
        Stream<SkinnyItemSet> initialSets = generateSkinnyComboStream(skinnyOptions, parallel);

        Stream<SkinnyItemSet> filteredSets = requirements.filterSetsSkinny(initialSets);

        if (topCombosMultiply != null) {
            int actualTop = (int) (topCombosMultiply * TOP_HIT_COMBO_FILTER);
            printRecorder.printf("SKINNY COMBOS BEST %,d\n", actualTop);
            ToLongFunction<SkinnyItemSet> ratingFunc = requirements.skinnyRatingMinimiseFunc();

//            filteredSets = filteredSets.filter(new BottomNFilter<>(actualTop, ratingFunc));

            // try a Top Collector (with good merging), re-stream combo
            filteredSets = filteredSets.collect(new BottomCollectorN<>(actualTop, ratingFunc))
                    .parallelStream();

            // TODO minimise hit only?

            // TODO multiple sets with equal combohit may be lost
        } else {
            printRecorder.println("SKINNY COMBOS ALL");
        }

        return filteredSets.map(this::makeFromSkinny);
    }

    private SolvableItemSet makeFromSkinny(SkinnyItemSet skinnySet) {
        SolvableEquipMap chosenItems = SolvableEquipMap.empty();
        CurryQueue<SkinnyItem> itemQueue = skinnySet.items();
        while (itemQueue != null) {
            SkinnyItem skinny = itemQueue.item();
            SlotEquip slot = skinny.slot();
            SolvableItem[] fullSlotItems = fullItems.get(slot);

            BestHolder<SolvableItem> bestSlotItem = new BestHolder<>();
            for (SolvableItem item : fullSlotItems) {
                if (requirements.skinnyMatch(skinny, item)) {
                    long rating = model.calcRating(item);
                    bestSlotItem.add(item, rating);
                }
            }

            chosenItems.put(slot, bestSlotItem.get());
            itemQueue = itemQueue.tail();
        }
        return SolvableItemSet.manyItems(chosenItems, adjustment);
    }

    private List<SkinnyItem[]> convertToSkinny(SolvableEquipOptionsMap items) {
        List<SkinnyItem[]> optionsList = new ArrayList<>();
        for (SlotEquip slot : SlotEquip.values()) {
            SolvableItem[] fullOptions = items.get(slot);
            if (fullOptions != null) {
                HashSet<SkinnyItem> slotSet = new HashSet<>();
                for (SolvableItem item : fullOptions) {
                    SkinnyItem skinny = requirements.toSkinny(slot, item);
                    slotSet.add(skinny);
                }
                SkinnyItem[] slotArray = slotSet.toArray(SkinnyItem[]::new);
                optionsList.add(slotArray);
            }
        }
        return optionsList;
    }

    protected Stream<SkinnyItemSet> generateSkinnyComboStream(List<SkinnyItem[]> optionsList, boolean parallel) {
        Stream<SkinnyItemSet> stream = null;

//        optionsList.sort(Comparator.comparingInt(array -> array.length));
        // TODO sort by biggest cap values so filter out faster

        // TODO move to indexed, away from CurryQueue

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
