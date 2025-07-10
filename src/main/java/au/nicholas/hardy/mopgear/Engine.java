package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.BigStreamUtil;
import au.nicholas.hardy.mopgear.util.TopCollector1;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class Engine {
    static Collection<ItemSet> runSolver(Map<SlotEquip, List<ItemData>> items, Instant startTime) {
        long estimate = estimateSets(items);
        Stream<ItemSet> initialSets = generateItemCombinations(items);
        initialSets = BigStreamUtil.countProgress(estimate, startTime, initialSets);

        Stream<ItemSet> summarySets = makeFinalisedSets(initialSets);
        Stream<ItemSet> filteredSets = filterSets(summarySets);
        return filteredSets.collect(new TopCollector1<>(20, ItemSet::getStatRating));
    }

    private static long estimateSets(Map<SlotEquip, List<ItemData>> reforgedItems) {
        return reforgedItems.values().stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
    }

    private static Stream<ItemSet> filterSets(Stream<ItemSet> sets) {
        return sets.filter(set -> inRange2(set.getTotals()));
    }

    private static boolean inRange2(StatBlock totals) {
        EnumMap<Secondary, Integer> targets = ModelParams.requiredAmounts;
        for (Map.Entry<Secondary, Integer> entry : targets.entrySet()) {
            int val = totals.get(entry.getKey()), cap = entry.getValue();
            if (val < cap || val > cap + ModelParams.RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        return true;
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
