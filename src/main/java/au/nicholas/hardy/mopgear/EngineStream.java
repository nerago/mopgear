package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.BigStreamUtil;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

public class EngineStream {
    public static ItemSet runSolver(ModelCombined model, Map<SlotEquip, ItemData[]> items, Instant startTime, ItemSet otherSet) {
        Stream<ItemSet> finalSets = runSolverPartial(model, items, startTime, otherSet);
        Optional<ItemSet> opt = finalSets.max(Comparator.comparingLong(x -> model.calcRating(x.totals)));
        return opt.orElseThrow();
//        return finalSets.collect(new TopCollector1<>(20, ItemSet::getStatRating));
    }

    public static Stream<ItemSet> runSolverPartial(ModelCombined model, Map<SlotEquip, ItemData[]> items, Instant startTime, ItemSet otherSet) {
        long estimate = estimateSets(items);
        Stream<ItemSet> initialSets = generateItemCombinations(items, model, otherSet);

        if (startTime != null)
            initialSets = BigStreamUtil.countProgress(estimate, startTime, initialSets);

        return model.filterSets(initialSets).parallel();
    }

    // NOTES: we could dig right down a path to find its max/min hit/exp limits, then know if we're on a bad path

    private static long estimateSets(Map<SlotEquip, ItemData[]> reforgedItems) {
        return reforgedItems.values().stream().mapToLong(x -> (long) x.length).reduce((a, b) -> a * b).orElse(0);
    }

//    private static Stream<ItemSet> makeFinalisedSets(Model model, Stream<ItemSet> initialSets) {
//        return initialSets.map(x -> x.finished(model::calcRating));
//    }

    private static Stream<ItemSet> generateItemCombinations(Map<SlotEquip, ItemData[]> itemsBySlot, ModelCombined model, ItemSet otherSet) {
        Stream<ItemSet> stream = null;

        List<Map.Entry<SlotEquip, ItemData[]>> sortedEntries =
                itemsBySlot.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> -e.getValue().length)).toList();
        for (Map.Entry<SlotEquip, ItemData[]> slotEntry : sortedEntries) {
            if (stream == null) {
                stream = newCombinationStream(slotEntry.getKey(), slotEntry.getValue(), otherSet);
            } else {
                stream = applyItemsToCombination(stream, slotEntry.getKey(), slotEntry.getValue());
            }
            stream = model.filterSetsMax(stream);
        }
        return stream;
    }

    private static Stream<ItemSet> newCombinationStream(SlotEquip slot, ItemData[] slotItems, ItemSet otherSet) {
//        shuffle(slotItems);
        final ItemSet[] initialSets = new ItemSet[slotItems.length];
        for (int i = 0; i < slotItems.length; ++i) {
            initialSets[i] = ItemSet.singleItem(slot, slotItems[i], otherSet);
        }
        final Spliterator<ItemSet> split = Spliterators.spliterator(initialSets, SIZED | SUBSIZED | ORDERED | DISTINCT | NONNULL | IMMUTABLE);
        return StreamSupport.stream(split, true);
    }

    private static Stream<ItemSet> applyItemsToCombination(Stream<ItemSet> stream, SlotEquip slot, ItemData[] slotItems) {
//        shuffle(slotItems);
        Stream<ItemSet> n = stream.mapMulti((set, sink) -> {
            for (ItemData add : slotItems) {
                sink.accept(set.copyWithAddedItem(slot, add));
            }
        });
        return n.parallel();
    }
}
