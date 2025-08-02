package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.ArrayUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue"})
public class EngineRandom {
    public static Collection<ItemSet> runSolver(ModelCombined model, EnumMap<SlotEquip, ItemData[]> items, ItemSet otherSet) {
        Stream<ItemSet> finalSets = runSolverPartial(model, items, otherSet);
        Optional<ItemSet> opt = finalSets.max(Comparator.comparingLong(x -> model.calcRating(x.totals)));
        return opt.isPresent() ? Collections.singleton(opt.get()) : Collections.emptyList();
    }

    private static Stream<ItemSet> runSolverPartial(ModelCombined model, EnumMap<SlotEquip, ItemData[]> items, ItemSet otherSet) {
        Stream<Long> dumbStream = generateDumbStream(1000L * 1000L);
        Stream<ItemSet> setStream = dumbStream.parallel()
                                              .map(x -> makeSet(items, otherSet));
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
