package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.TopCollector1;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class EngineStream {
    static Collection<ItemSet> runSolver(Map<SlotEquip, List<ItemData>> items, Instant startTime) {
        long estimate = estimateSets(items);
        Stream<ItemSet> initialSets = generateItemCombinations(items);
//        if (startTime != null)
//            initialSets = BigStreamUtil.countProgress(estimate, startTime, initialSets);

        Stream<ItemSet> filteredSets = EngineUtil.filterSets(initialSets);
        Stream<ItemSet> finalSets = makeFinalisedSets(filteredSets);
        return finalSets.collect(new TopCollector1<>(20, ItemSet::getStatRating));
    }

    private static long estimateSets(Map<SlotEquip, List<ItemData>> reforgedItems) {
        return reforgedItems.values().stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
    }

    private static Stream<ItemSet> makeFinalisedSets(Stream<ItemSet> initialSets) {
        return initialSets.map(ItemSet::finished);
    }

    private static Stream<ItemSet> generateItemCombinations(Map<SlotEquip, List<ItemData>> itemsBySlot) {
        Stream<ItemSet> stream = null;
        for (List<ItemData> slotItems : itemsBySlot.values()) {
            if (stream == null) {
                stream = newCombinationStream(slotItems);
            } else {
                stream = applyItemsToCombination(stream, slotItems);
            }
        }
        return stream;
    }

    private static Stream<ItemSet> newCombinationStream(List<ItemData> slotItems) {
        return slotItems.parallelStream().unordered().map(ItemSet::singleItem);
    }

    private static Stream<ItemSet> applyItemsToCombination(Stream<ItemSet> stream, List<ItemData> slotItems) {
        return stream.mapMulti((set, sink) -> {
            for (ItemData add : slotItems) {
                sink.accept(set.copyWithAddedItem(add));
            }
        });
    }
}
