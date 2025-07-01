package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;
import au.nicholas.hardy.mopgear.util.TopCollector1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class Engine {
    static Collection<ItemSet> runSolver(List<ItemData> items) throws IOException {
        List<List<ItemData>> reforgedItems = items.stream().map(Engine::reforgeItem).toList();

        System.out.println("total items " + reforgedItems.size());
        reforgedItems = new ArrayList<>(reforgedItems);
//        while (reforgedItems.size() >= 14) reforgedItems.removeLast();

        //       initial     summary   no-nullable  avoid-lowest avoid2-lowest
        // 11    0.969s      14s
        // 12    8.05s       1M30      27s          4.9s
        // 13    1M2                                28s          3s
        // 14

        Stream<CurryQueue<ItemData>> initialSets = generateItemCombinations(reforgedItems);
//        System.out.println(initialSets.count());
        Stream<ItemSet> summarySets = makeSummarySets(initialSets);
        Stream<ItemSet> filteredSets = filterSets(summarySets);
        return filteredSets.collect(new TopCollector1<>(20, s -> s.statRating));
    }

    private static Stream<ItemSet> filterSets(Stream<ItemSet> sets) {
        return sets.filter(set -> inRange(set.totals.hit, ModelParams.TARGET_HIT) && inRange(set.totals.expertise, ModelParams.TARGET_EXPERTISE));
    }

    private static boolean inRange(int value, int target) {
        return target <= value && value <= target + ModelParams.PERMITTED_EXCEED;
    }

    private static Stream<ItemSet> makeSummarySets(Stream<CurryQueue<ItemData>> initialSets) {
        return initialSets.map(ItemSet::new);
    }

    private static Stream<CurryQueue<ItemData>> generateItemCombinations(List<List<ItemData>> itemsBySlot) {
        Stream<CurryQueue<ItemData>> stream = null;
        for (List<ItemData> slotItems : itemsBySlot) {
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

    static List<ItemData> reforgeItem(ItemData baseItem) {
        List<ItemData> outputItems = new ArrayList<>();
        outputItems.add(baseItem);

        for (Secondary originalStat : Secondary.values()) {
            int originalValue = baseItem.get(originalStat);
            if (originalValue != 0) {
                int reforgeQuantity = (originalValue * 4) / 10;
                int remainQuantity = originalValue - reforgeQuantity;
                for (Secondary targetStat : ModelParams.reforgeTargets) {
                    if (baseItem.get(targetStat) == 0) {
                        ItemData modified = baseItem.copy();
                        modified.set(originalStat, remainQuantity);
                        modified.set(targetStat, reforgeQuantity);
                        outputItems.add(modified);
                    }
                }
            }
        }

        return outputItems;
    }
}
