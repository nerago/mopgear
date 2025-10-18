package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.model.StatRequirementsOriginal;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.BigStreamUtil;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue"})
public class SolverRandom {
    public static Optional<ItemSet> runSolver(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, Instant startTime, long count, boolean parallel, Predicate<ItemSet> specialFilter) {
        if (parallel) {
            Stream<ItemSet> finalSets = runSolverPartial(model, items, adjustment, startTime, count);
            if (specialFilter != null)
                finalSets = finalSets.filter(specialFilter);
            return finalSets.max(Comparator.comparingLong(model::calcRating));
        } else {
            return runSolverSingleThread(model, items, adjustment, count, specialFilter);
        }
    }

    public static Stream<ItemSet> runSolverPartial(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, Instant startTime, long count) {
        Stream<Long> dumbStream = generateDumbStream(count);
        Stream<ItemSet> setStream = dumbStream.parallel()
                                              .map(x -> makeSet(items, adjustment));
        if (startTime != null)
            setStream = BigStreamUtil.countProgress(count, startTime, setStream);
        return model.filterSets(setStream, true);
    }

    public static Optional<ItemSet> runSolverSingleThread(ModelCombined model, EquipOptionsMap items, StatBlock adjustment, long count, Predicate<ItemSet> specialFilter) {
        Random random = ThreadLocalRandom.current();
        StatRequirements require = model.statRequirements();
        BestHolder<ItemSet> best = new BestHolder<>();
        for (int i = 0; i < count; ++i) {
            ItemSet set = makeSet(items, adjustment, random);
            if (model.filterOneSet(set) && (specialFilter == null || specialFilter.test(set))) {
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
