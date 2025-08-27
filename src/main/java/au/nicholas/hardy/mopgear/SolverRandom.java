package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.model.StatRequirements;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.BestHolder;
import au.nicholas.hardy.mopgear.util.BigStreamUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue"})
public class SolverRandom {
    public static Optional<ItemSet> runSolver(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, Instant startTime, long count) {
        Stream<ItemSet> finalSets = runSolverPartial(model, items, adjustment, startTime, count);
        return finalSets.max(Comparator.comparingLong(x -> model.calcRating(x.totals)));
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
        BestHolder<ItemSet> best = new BestHolder<>(null, 0);
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
