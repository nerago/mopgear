package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.BigStreamUtil;
import au.nerago.mopgear.util.Primes;
import au.nerago.mopgear.util.StreamNeedClose;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SolverIndexed {
    public static Optional<SolvableItemSet> runFullScan(ModelCombined model, SolvableEquipOptionsMap itemOptions, StatBlock adjustment, long comboCount, Predicate<SolvableItemSet> specialFilter) {
        Stream<Long> dumbStream = generateDumbStream(comboCount).parallel();
        Stream<SolvableItemSet> partialSets = dumbStream.map(index -> makeSet(itemOptions, adjustment, index));
        StreamNeedClose<SolvableItemSet> countedSets = BigStreamUtil.countProgress(comboCount, null, partialSets);
        return finishToResult(model, specialFilter, countedSets);
    }

    public static Optional<SolvableItemSet> runSolverSkipping(ModelCombined model, SolvableEquipOptionsMap itemOptions, StatBlock adjustment, Instant startTime, long desiredRunSize, BigInteger estimateFullCombos, Predicate<SolvableItemSet> specialFilter) {
        BigInteger skipSize = estimateFullCombos.divide(BigInteger.valueOf(desiredRunSize));
        if (skipSize.compareTo(BigInteger.ONE) < 0)
            skipSize = BigInteger.ONE;
        else if (skipSize.compareTo(BigInteger.ONE) > 0)
            skipSize = Primes.roundToPrime(skipSize);
        BigInteger plannedCount = estimateFullCombos.divide(skipSize);

        if (fitsLong(estimateFullCombos) && fitsLong(plannedCount) && fitsLong(skipSize)) {
            Stream<Long> dumbStream = generateDumbStream(estimateFullCombos.longValueExact(), skipSize.longValueExact()).parallel();
            Stream<SolvableItemSet> partialSets = dumbStream.map(index -> makeSet(itemOptions, adjustment, index));
            StreamNeedClose<SolvableItemSet> countedSets = BigStreamUtil.countProgress(plannedCount.doubleValue(), startTime, partialSets);
            return finishToResult(model, specialFilter, countedSets);
        } else {
            Stream<BigInteger> dumbStream = generateDumbStream(estimateFullCombos, skipSize).parallel();
            Stream<SolvableItemSet> partialSets = dumbStream.map(index -> makeSet(itemOptions, adjustment, index));
            StreamNeedClose<SolvableItemSet> countedSets = BigStreamUtil.countProgress(plannedCount.doubleValue(), startTime, partialSets);
            return finishToResult(model, specialFilter, countedSets);
        }
    }

    private static Optional<SolvableItemSet> finishToResult(ModelCombined model, Predicate<SolvableItemSet> specialFilter, StreamNeedClose<SolvableItemSet> partialSets) {
        StreamNeedClose<SolvableItemSet> finalSets = model.filterSets(partialSets, true);
        if (specialFilter != null)
            finalSets = finalSets.filter(specialFilter);
        return finalSets.max(Comparator.comparingLong(model::calcRating));
    }

    private static SolvableItemSet makeSet(SolvableEquipOptionsMap itemOptions, StatBlock adjustment, long mainIndex) {
        SolvableEquipMap map = SolvableEquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            SolvableItem[] list = itemOptions.get(slot);
            if (list != null) {
                int size = list.length;

                int thisIndex = (int) (mainIndex % size);
                mainIndex /= size;

                SolvableItem choice = list[thisIndex];
                map.put(slot, choice);
            }
        }
        return SolvableItemSet.manyItems(map, adjustment);
    }

    private static SolvableItemSet makeSet(SolvableEquipOptionsMap itemOptions, StatBlock adjustment, BigInteger mainIndex) {
        SolvableEquipMap map = SolvableEquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            SolvableItem[] list = itemOptions.get(slot);
            if (list != null) {
                int size = list.length;

                BigInteger[] divRem = mainIndex.divideAndRemainder(BigInteger.valueOf(size));

                int thisIndex = divRem[1].intValueExact();
                mainIndex = divRem[0];

                SolvableItem choice = list[thisIndex];
                map.put(slot, choice);
            }
        }
        return SolvableItemSet.manyItems(map, adjustment);
    }

    private static Stream<Long> generateDumbStream(long max) {
        return Stream.iterate(0L, x -> x < max, x -> x + 1);
    }

    private static Stream<BigInteger> generateDumbStream(BigInteger max, BigInteger skip) {
        long start = ThreadLocalRandom.current().nextLong(0, skip.longValueExact());
        return Stream.iterate(BigInteger.valueOf(start), x -> x.compareTo(max) < 0, x -> x.add(skip));
    }

    private static Stream<Long> generateDumbStream(long max, long skip) {
        long start = ThreadLocalRandom.current().nextLong(0, skip);
        return Stream.iterate(start, x -> x < max, x -> x + skip);
    }

    private static final BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
    private static boolean fitsLong(BigInteger number) {
        return number.compareTo(maxLong) < 0;
    }
}
