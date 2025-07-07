package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;
import au.nicholas.hardy.mopgear.util.TopCollector1;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class Engine {
    static Collection<ItemSet> runSolver(List<ItemData> items, Instant startTime) throws IOException {
        List<List<ItemData>> reforgedItems = items.stream().map(Engine::reforgeItem).toList();

        long estimate = estimateSets(reforgedItems);
        Stream<CurryQueue<ItemData>> initialSets = generateItemCombinations(reforgedItems);
        initialSets = countProgress(estimate, startTime, initialSets);

        Stream<ItemSet> summarySets = makeSummarySets(initialSets);
        Stream<ItemSet> filteredSets = filterSets(summarySets);
        return filteredSets.collect(new TopCollector1<>(20, s -> s.statRating));
    }

    private static long estimateSets(List<List<ItemData>> reforgedItems) {
        return reforgedItems.stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
    }

    static <T> Stream<T> countProgress(final long estimate, Instant startTime, Stream<T> sets) {
        final double estimateFloat = estimate / 100.0;
        AtomicLong count = new AtomicLong();
        return sets.peek(set -> {
            long curr = count.incrementAndGet();
            if (curr % 5000000 == 0) {
                double percent = ((double) curr) / estimateFloat;
                synchronized (System.out) {
                    System.out.print(curr);
                    System.out.print(" ");
                    System.out.printf("%.2f", percent);
                    Duration estimateRemain = estimateRemain(startTime, percent);
                    System.out.print(" ");
                    System.out.print(estimateRemain);
                    System.out.println();
                }
            }
        });
    }

    public static Duration estimateRemain(Instant startTime, double percent) {
        final int factor = 100;

        long multiply = (long) (factor * 100 / percent);

        Duration timeTaken = Duration.between(startTime, Instant.now());
        Duration totalEstimate = timeTaken.multipliedBy(multiply).dividedBy(factor);
        return totalEstimate.minus(timeTaken);
    }

    private static Stream<ItemSet> filterSets(Stream<ItemSet> sets) {
        return sets.filter(set -> inRange2(set.totals));
    }

    private static boolean inRange2(ItemData totals) {
        EnumMap<Secondary, Integer> targets = ModelParams.requiredAmounts;
        for (Map.Entry<Secondary, Integer> entry : targets.entrySet()) {
            int val = totals.get(entry.getKey()), cap = entry.getValue();
            if (val < cap || val > cap + ModelParams.PERMITTED_EXCEED)
                return false;
        }
        return true;
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
                        modified.name += " (" + originalStat + "->" + targetStat + ")";
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
