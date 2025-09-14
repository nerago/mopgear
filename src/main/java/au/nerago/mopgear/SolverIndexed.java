package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class SolverIndexed {
    public static Optional<ItemSet> runSolver(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, Instant startTime, long comboCount) {
        Stream<ItemSet> partialSets = runSolverPartial(model, itemOptions, adjustment, startTime, comboCount);
        Stream<ItemSet> finalSets = model.filterSets(partialSets, true);
        return finalSets.max(Comparator.comparingLong(model::calcRating));
    }

    private static Stream<ItemSet> runSolverPartial(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, Instant startTime, long comboCount) {
        Stream<Long> dumbStream = generateDumbStream(comboCount, 1).parallel();
        return dumbStream.map(index -> makeSet(itemOptions, adjustment, index));
    }

    private static ItemSet makeSet(EquipOptionsMap itemOptions, StatBlock adjustment, long mainIndex) {
        EquipMap map = EquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] list = itemOptions.get(slot);
            int size = list.length;

            int thisIndex = (int) (mainIndex % size);
            mainIndex /= size;

            ItemData choice = list[thisIndex];
            map.put(slot, choice);
        }
        return ItemSet.manyItems(map, adjustment);
    }

    private static Stream<Long> generateDumbStream(long count, long skip) {
        return Stream.iterate(0L, x -> x < count, x -> x + skip);
    }
}
