package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.util.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class SolverCapPhased {
    protected final ModelCombined model;
    protected final StatRequirements requirements;
    protected final StatBlock adjustment;
    protected final PrintRecorder printRecorder;
    protected SolvableEquipOptionsMap fullItems;
    protected List<SkinnyItem[]> skinnyOptions;
    protected BigInteger estimate;

    public SolverCapPhased(ModelCombined model, StatBlock adjustment, PrintRecorder printRecorder) {
        if (!supportedModel(model))
            throw new IllegalArgumentException("can't use this model without skinny support");
        this.model = model;
        this.requirements = model.statRequirements();
        this.adjustment = adjustment;
        this.printRecorder = printRecorder;
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

    public Optional<SolvableItemSet> runSolver(boolean parallel, Predicate<SolvableItemSet> specialFilter, boolean indexed, boolean topOnly, Integer generateComboCount, Integer topCombosFilterCount, Instant startTime) {
        StreamNeedClose<SkinnyItemSet> skinnyCombos = makeSkinnyCombos(parallel, indexed, generateComboCount, startTime);

        if (topOnly) {
            skinnyCombos = filterBestCapsOnly(skinnyCombos, topCombosFilterCount);
        } else {
            printRecorder.println("SKINNY COMBOS ALL");
        }

        StreamNeedClose<SolvableItemSet> partialSets = skinnyCombos.map(this::makeFromSkinny);
        StreamNeedClose<SolvableItemSet> finalSets = model.filterSets(partialSets, true);
        if (specialFilter != null)
            finalSets = finalSets.filter(specialFilter);
        if (parallel)
            finalSets = finalSets.parallel();
        return finalSets.collect(new TopCollector1<>(model::calcRating));
    }

    private StreamNeedClose<SkinnyItemSet> makeSkinnyCombos(boolean parallel, boolean indexed, Integer generateComboCount, Instant startTime) {
        StreamNeedClose<SkinnyItemSet> initialSets;
        if (indexed) {
            BigInteger targetCombos = BigInteger.valueOf(generateComboCount);
            Stream<SkinnyItemSet> generatedSets = generateSkinnyComboStreamIndexed(parallel, targetCombos);
            initialSets = BigStreamUtil.countProgress(generateComboCount, startTime, generatedSets);
        } else {
            Stream<SkinnyItemSet> generatedSets = generateSkinnyComboStreamFull(parallel);
            initialSets = BigStreamUtil.countProgress(estimate.doubleValue(), startTime, generatedSets);
        }

        return requirements.filterSetsSkinny(initialSets);
    }

    private StreamNeedClose<SkinnyItemSet> filterBestCapsOnly(StreamNeedClose<SkinnyItemSet> filteredSets, int topCombosFilterCount) {
        printRecorder.printf("SKINNY COMBOS BEST %,d\n", topCombosFilterCount);
        ToLongFunction<SkinnyItemSet> ratingFunc = requirements.skinnyRatingMinimiseFunc();

        // try a Top Collector (with good merging), re-stream combo
        Collection<SkinnyItemSet> bottomCollection = filteredSets.collect(new BottomCollectorN<>(topCombosFilterCount, ratingFunc));

        // TODO minimise hit only?
        // TODO multiple sets with equal combohit may be lost
        return new StreamNeedClose<>(bottomCollection.parallelStream());
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

    protected Stream<SkinnyItemSet> generateSkinnyComboStreamFull(boolean parallel) {
        Stream<SkinnyItemSet> stream = null;

        // TODO sort by biggest cap values so filter out faster

        for (SkinnyItem[] slotOptions : skinnyOptions) {
            if (stream == null) {
                stream = newCombinationStream(slotOptions, parallel);
            } else {
                stream = addSlotToCombination(stream, slotOptions);
            }
            stream = requirements.filterSetsMaxSkinny(stream);
        }

        return stream;
    }

    private Stream<SkinnyItemSet> generateSkinnyComboStreamIndexed(boolean parallel, BigInteger targetCombos) {
        BigInteger skip = BigInteger.ONE;
        if (estimate.compareTo(targetCombos) > 0) {
            skip = Primes.roundToPrime(estimate.divide(targetCombos));
        }
        printRecorder.println("generateSkinnyComboStream skip=" + skip + " trying " + estimate.divide(skip));
        Stream<BigInteger> numberStream = generateDumbStream(estimate, skip);
        if (parallel)
            //noinspection DataFlowIssue
            numberStream = numberStream.parallel();
        return numberStream.map(this::makeSkinnyFromIndex);
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

    private static Stream<BigInteger> generateDumbStream(BigInteger max, BigInteger skip) {
        long start = ThreadLocalRandom.current().nextLong(skip.longValueExact());
        return Stream.iterate(BigInteger.valueOf(start), x -> x.compareTo(max) < 0, x -> x.add(skip));
    }

    private SkinnyItemSet makeSkinnyFromIndex(BigInteger mainIndex) {
        SkinnyItemSet itemSet = null;

        for (SkinnyItem[] list : skinnyOptions) {
            int size = list.length;

            BigInteger[] divRem = mainIndex.divideAndRemainder(BigInteger.valueOf(size));

            int thisIndex = divRem[1].intValueExact();
            mainIndex = divRem[0];

            SkinnyItem choice = list[thisIndex];
            if (itemSet == null) {
                itemSet = SkinnyItemSet.single(choice);
            } else {
                itemSet = itemSet.withAddedItem(choice);
            }
        }

        return itemSet;
    }
}
