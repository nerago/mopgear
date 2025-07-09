package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.BigStreamUtil;
import au.nicholas.hardy.mopgear.util.CurryQueue;
import au.nicholas.hardy.mopgear.util.TopCollector1;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class Engine {
    static Collection<ItemSet> runSolver(Map<SlotItem, List<ItemData>> items, Instant startTime) {
        long estimate = estimateSets(items);
        Stream<CurryQueue<ItemData>> initialSets = generateItemCombinations(items);
        initialSets = BigStreamUtil.countProgress(estimate, startTime, initialSets);

        Stream<ItemSet> summarySets = makeSummarySets(initialSets);
        Stream<ItemSet> filteredSets = filterSets(summarySets);
        return filteredSets.collect(new TopCollector1<>(20, s -> s.statRating));
    }

    private static long estimateSets(Map<SlotItem, List<ItemData>> reforgedItems) {
        return reforgedItems.values().stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
    }

    private static Stream<ItemSet> filterSets(Stream<ItemSet> sets) {
        return sets.filter(set -> inRange2(set.totals));
    }

    private static boolean inRange2(ItemData totals) {
        EnumMap<Secondary, Integer> targets = ModelParams.requiredAmounts;
        for (Map.Entry<Secondary, Integer> entry : targets.entrySet()) {
            int val = totals.get(entry.getKey()), cap = entry.getValue();
            if (val < cap || val > cap + ModelParams.RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        return true;
    }

    private static Stream<ItemSet> makeSummarySets(Stream<CurryQueue<ItemData>> initialSets) {
        return initialSets.map(ItemSet::new);
    }

    private static Stream<CurryQueue<ItemData>> generateItemCombinations(Map<SlotItem, List<ItemData>> itemsBySlot) {
        Stream<CurryQueue<ItemData>> stream = null;
        for (List<ItemData> slotItems : itemsBySlot.values()) {
            if (stream == null) {
                stream = newCombinationStream(slotItems);
            } else {
                stream = applyItemsToCombination(stream, slotItems);
            }
        }
        return stream;
    }

    private static Stream<CurryQueue<ItemData>> newCombinationStream(List<ItemData> slotItems) {
        return slotItems.parallelStream().unordered().map(CurryQueue::single);
    }

    private static Stream<CurryQueue<ItemData>> applyItemsToCombination(Stream<CurryQueue<ItemData>> stream, List<ItemData> slotItems) {
        return stream.mapMulti((set, sink) -> {
            for (ItemData add : slotItems) {
                sink.accept(set.prepend(add));
            }
        });
    }
}
