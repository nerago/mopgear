package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.BigStreamUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue"})
public class EngineRandom {
    public static Optional<ItemSet> runSolver(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, Instant startTime, ItemSet otherSet, long count) {
        Stream<ItemSet> finalSets = runSolverPartial(model, items, adjustment, startTime, otherSet, count);
        return finalSets.max(Comparator.comparingLong(x -> model.calcRating(x.totals)));
    }

    public static Stream<ItemSet> runSolverPartial(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, Instant startTime, ItemSet otherSet, long count) {
        Stream<Long> dumbStream = generateDumbStream(count);
        Stream<ItemSet> setStream = dumbStream.parallel()
                                              .map(x -> makeSet(items, otherSet, adjustment));
        if (startTime != null)
            setStream = BigStreamUtil.countProgress(count, startTime, setStream);
        return model.filterSets(setStream);
    }

    private static ItemSet makeSet(EquipOptionsMap items, ItemSet otherSet, StatBlock adjustment) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        EquipMap chosen = EquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] itemList = items.get(slot);
            if (itemList != null) {
                ItemData item = ArrayUtil.rand(itemList, random);
                chosen.put(slot, item);
            }
        }
        return ItemSet.manyItems(chosen, otherSet, adjustment);
    }

    private static Stream<Long> generateDumbStream(long count) {
        return Stream.iterate(0L, x -> x < count, x -> x + 1);
    }

}
