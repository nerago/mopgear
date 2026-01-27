package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class SolverIndexed {
    public static Optional<SolvableItemSet> runFullScan(ModelCombined model, SolvableEquipOptionsMap itemOptions, StatBlock adjustment, long comboCount, Predicate<SolvableItemSet> specialFilter) {
        LongStream dumbStream = BigStreamUtil.generateDumbStream(comboCount, 1).parallel();
        Stream<SolvableItemSet> partialSets = dumbStream.mapToObj(index -> makeSet(itemOptions, adjustment, index));
        try (StreamNeedClose<SolvableItemSet> countedSets = BigStreamUtil.countProgress(comboCount, null, partialSets)) {
            return finishToResult(model, specialFilter, countedSets);
        }
    }

    public static Optional<SolvableItemSet> runSolverSkipping(ModelCombined model, SolvableEquipOptionsMap itemOptions, StatBlock adjustment, Instant startTime, long desiredRunSize, BigInteger estimateFullCombos, Predicate<SolvableItemSet> specialFilter) {
        BigInteger skipSize = estimateFullCombos.divide(BigInteger.valueOf(desiredRunSize));
        if (skipSize.compareTo(BigInteger.ONE) < 0)
            skipSize = BigInteger.ONE;
        else if (skipSize.compareTo(BigInteger.ONE) > 0)
            skipSize = Primes.roundToPrime(skipSize);
        BigInteger plannedCount = estimateFullCombos.divide(skipSize);

        Stream<SolvableItemSet> partialSets;
        if (BigStreamUtil.fitsMaxLong(estimateFullCombos) && BigStreamUtil.fitsMaxLong(plannedCount) && BigStreamUtil.fitsMaxLong(skipSize)) {
            partialSets = BigStreamUtil.generateDumbStream(estimateFullCombos.longValueExact(), skipSize.longValueExact())
                    .parallel()
                    .mapToObj(index -> makeSet(itemOptions, adjustment, index));
        } else {
            partialSets = BigStreamUtil.generateDumbStream(estimateFullCombos, skipSize)
                    .parallel()
                    .map(index -> makeSet(itemOptions, adjustment, index));
        }

        try (StreamNeedClose<SolvableItemSet> countedSets = BigStreamUtil.countProgress(plannedCount.doubleValue(), startTime, partialSets)) {
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
}
