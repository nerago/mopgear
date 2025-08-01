package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.BigStreamUtil;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.shuffle;
import static java.util.Spliterator.*;
import static java.util.Spliterator.IMMUTABLE;

public class EngineStream {
    public static Collection<ItemSet> runSolver(ModelCombined model, Map<SlotEquip, List<ItemData>> items, Instant startTime, ItemSet otherSet) {
        Stream<ItemSet> finalSets = runSolverPartial(model, items, startTime, otherSet);
        Optional<ItemSet> opt = finalSets.max(Comparator.comparingLong(x -> model.calcRating(x.totals)));
        return opt.isPresent() ? Collections.singleton(opt.get()) : Collections.emptyList();
//        return finalSets.collect(new TopCollector1<>(20, ItemSet::getStatRating));
    }

    public static Stream<ItemSet> runSolverPartial(ModelCombined model, Map<SlotEquip, List<ItemData>> items, Instant startTime, ItemSet otherSet) {
        long estimate = estimateSets(items);
        Stream<ItemSet> initialSets = generateItemCombinations(items, model, otherSet);

        if (startTime != null)
            initialSets = BigStreamUtil.countProgress(estimate, startTime, initialSets);

        return model.filterSets(initialSets).parallel();
    }

    // NOTES: we could dig right down a path to find its max/min hit/exp limits, then know if we're on a bad path

    private static long estimateSets(Map<SlotEquip, List<ItemData>> reforgedItems) {
        return reforgedItems.values().stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
    }

//    private static Stream<ItemSet> makeFinalisedSets(Model model, Stream<ItemSet> initialSets) {
//        return initialSets.map(x -> x.finished(model::calcRating));
//    }

    private static Stream<ItemSet> generateItemCombinations(Map<SlotEquip, List<ItemData>> itemsBySlot, ModelCombined model, ItemSet otherSet) {
        Stream<ItemSet> stream = null;

        List<Map.Entry<SlotEquip, List<ItemData>>> sortedEntries =
                itemsBySlot.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> -e.getValue().size())).toList();
        for (Map.Entry<SlotEquip, List<ItemData>> slotEntry : sortedEntries) {
            if (stream == null) {
                stream = newCombinationStream(slotEntry.getKey(), slotEntry.getValue(), otherSet);
            } else {
                stream = applyItemsToCombination(stream, slotEntry.getKey(), slotEntry.getValue());
            }
            stream = model.filterSetsMax(stream);
        }
        return stream;
    }

    private static Stream<ItemSet> newCombinationStream(SlotEquip slot, List<ItemData> slotItems, ItemSet otherSet) {
//        shuffle(slotItems);
        final ItemSet[] initialSets = new ItemSet[slotItems.size()];
        for (int i = 0; i < slotItems.size(); ++i) {
            initialSets[i] = ItemSet.singleItem(slot, slotItems.get(i), otherSet);
        }
        final Spliterator<ItemSet> split = Spliterators.spliterator(initialSets, SIZED | SUBSIZED | ORDERED | DISTINCT | NONNULL | IMMUTABLE);
        return StreamSupport.stream(split, true);
    }

    private static Stream<ItemSet> applyItemsToCombination(Stream<ItemSet> stream, SlotEquip slot, List<ItemData> slotItems) {
//        shuffle(slotItems);
        Stream<ItemSet> n = stream.mapMulti((set, sink) -> {
            for (ItemData add : slotItems) {
                sink.accept(set.copyWithAddedItem(slot, add));
            }
        });
        return n.parallel();
    }
}
