package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.BigStreamUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue"})
public class EngineRandom {
    public static ItemSet runSolver(ModelCombined model, EnumMap<SlotEquip, ItemData[]> items, Instant startTime, ItemSet otherSet, long count) {
        Stream<ItemSet> finalSets = runSolverPartial(model, items, startTime, otherSet, count);
        Optional<ItemSet> opt = finalSets.max(Comparator.comparingLong(x -> model.calcRating(x.totals)));
        return opt.orElseThrow();
    }

    public static Stream<ItemSet> runSolverPartial(ModelCombined model, EnumMap<SlotEquip, ItemData[]> items, Instant startTime, ItemSet otherSet, long count) {
        Stream<Long> dumbStream = generateDumbStream(count);
        Stream<ItemSet> setStream = dumbStream.parallel()
                                              .map(x -> makeSet(items, otherSet));
        if (startTime != null)
            setStream = BigStreamUtil.countProgress(count, startTime, setStream);
        return model.filterSets(setStream);
    }

    private static ItemSet makeSet(EnumMap<SlotEquip, ItemData[]> items, ItemSet otherSet) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        EnumMap<SlotEquip, ItemData> chosen = new EnumMap<>(SlotEquip.class);
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] itemList = items.get(slot);
            if (itemList != null) {
                ItemData item = ArrayUtil.rand(itemList, random);
                chosen.put(slot, item);
            }
        }
        return ItemSet.manyItems(chosen, otherSet);
    }

    private static Stream<Long> generateDumbStream(long count) {
        return Stream.iterate(0L, x -> x < count, x -> x + 1);
    }

}
