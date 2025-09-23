package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.BigStreamUtil;
import au.nerago.mopgear.util.Primes;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

public class SolverIndexed {
    public static Optional<ItemSet> runSolver(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, Instant startTime, long comboCount, Predicate<ItemSet> specialFilter) {
        Stream<ItemSet> partialSets = runSolverPartial(model, itemOptions, adjustment, startTime, comboCount);
        return finishToResult(model, specialFilter, partialSets);
    }

    private static Optional<ItemSet> finishToResult(ModelCombined model, Predicate<ItemSet> specialFilter, Stream<ItemSet> partialSets) {
        Stream<ItemSet> finalSets = model.filterSets(partialSets, true);
        if (specialFilter != null)
            finalSets = finalSets.filter(specialFilter);
        return finalSets.max(Comparator.comparingLong(model::calcRating));
    }

    public static Optional<ItemSet> runSolverSkipping(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, Instant startTime, long desiredRunSize, BigInteger estimateFullCombos, Predicate<ItemSet> specialFilter) {
        BigInteger skipSize = estimateFullCombos.divide(BigInteger.valueOf(desiredRunSize));
        if (skipSize.compareTo(BigInteger.ONE) < 0)
            skipSize = BigInteger.ONE;
        else if (skipSize.compareTo(BigInteger.ONE) > 0)
            skipSize = Primes.roundToPrime(skipSize);
        BigInteger plannedCount = estimateFullCombos.divide(skipSize);

        Stream<BigInteger> dumbStream = generateDumbStream(estimateFullCombos, skipSize).parallel();
        Stream<ItemSet> partialSets = dumbStream.map(index -> makeSet(itemOptions, adjustment, index));
        partialSets = BigStreamUtil.countProgress(plannedCount, startTime, partialSets);
        return finishToResult(model, specialFilter, partialSets);
    }

    private static Stream<ItemSet> runSolverPartial(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, Instant startTime, long comboCount) {
        Stream<Long> dumbStream = generateDumbStream(comboCount, 1).parallel();
        return dumbStream.map(index -> makeSet(itemOptions, adjustment, index));
    }

    private static ItemSet makeSet(EquipOptionsMap itemOptions, StatBlock adjustment, long mainIndex) {
        EquipMap map = EquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] list = itemOptions.get(slot);
            if (list != null) {
                int size = list.length;

                int thisIndex = (int) (mainIndex % size);
                mainIndex /= size;

                ItemData choice = list[thisIndex];
                map.put(slot, choice);
            }
        }
        return ItemSet.manyItems(map, adjustment);
    }

    private static ItemSet makeSet(EquipOptionsMap itemOptions, StatBlock adjustment, BigInteger mainIndex) {
//        System.out.println(mainIndex);
        EquipMap map = EquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] list = itemOptions.get(slot);
            if (list != null) {
                int size = list.length;

                BigInteger[] divRem = mainIndex.divideAndRemainder(BigInteger.valueOf(size));

                int thisIndex = divRem[1].intValueExact();
                mainIndex = divRem[0];

                ItemData choice = list[thisIndex];
                map.put(slot, choice);
            }
        }

        ItemSet set = ItemSet.manyItems(map, adjustment);
//        System.out.println(set.items.get(SlotEquip.Ring1) + " "  + set.items.get(SlotEquip.Ring2));
        return set;
    }

    private static Stream<Long> generateDumbStream(long max, long skip) {
        return Stream.iterate(0L, x -> x < max, x -> x + skip);
    }

    private static Stream<BigInteger> generateDumbStream(BigInteger max, BigInteger skip) {
        int start = ThreadLocalRandom.current().nextInt(0, 61);
        return Stream.iterate(BigInteger.valueOf(start), x -> x.compareTo(max) < 0, x -> x.add(skip));
    }
}
