package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.BigStreamUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue"})
public class SolverRandom {
    public static Optional<ItemSet> runSolver(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, Instant startTime, long count) {
        Stream<ItemSet> finalSets = runSolverPartial(model, items, adjustment, startTime, count);
        return finalSets.max(Comparator.comparingLong(model::calcRating));
    }

    public static Stream<ItemSet> runSolverPartial(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, Instant startTime, long count) {
        Stream<Long> dumbStream = generateDumbStream(count);
        Stream<ItemSet> setStream = dumbStream.parallel()
                                              .map(x -> makeSet(items, adjustment));
        if (startTime != null)
            setStream = BigStreamUtil.countProgress(count, startTime, setStream);
        return model.filterSets(setStream);
    }

    public static Optional<ItemSet> runSolverSingleThread(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, long count) {
        Random random = ThreadLocalRandom.current();
        StatRequirements require = model.statRequirements();
        BestHolder<ItemSet> best = new BestHolder<>();
        for (int i = 0; i < count; ++i) {
            ItemSet set = makeSet(items, adjustment, random);
            if (require.filter(set)) {
                best.add(set, model.calcRating(set));
            }
        }
        return Optional.ofNullable(best.get());
    }

    private static ItemSet makeSet(EquipOptionsMap items, StatBlock adjustment) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return makeSet(items, adjustment, random);
    }

    private static ItemSet makeSet(EquipOptionsMap items, StatBlock adjustment, Random random) {
        EquipMap chosen = EquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] itemList = items.get(slot);
            if (itemList != null) {
                ItemData item = ArrayUtil.rand(itemList, random);
                chosen.put(slot, item);
            }
        }
        return ItemSet.manyItems(chosen, adjustment);
    }

    private static Stream<Long> generateDumbStream(long count) {
        return Stream.iterate(0L, x -> x < count, x -> x + 1);
    }
}
