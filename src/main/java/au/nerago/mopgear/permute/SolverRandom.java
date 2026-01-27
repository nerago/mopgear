package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.BigStreamUtil;
import au.nerago.mopgear.util.StreamNeedClose;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue"})
public class SolverRandom {
    public static Optional<SolvableItemSet> runSolver(ModelCombined model, SolvableEquipOptionsMap items, StatBlock adjustment, Instant startTime, long count, boolean parallel, Predicate<SolvableItemSet> specialFilter) {
        if (parallel) {
            try (StreamNeedClose<SolvableItemSet> possibleSets = runSolverPartial(model, items, adjustment, startTime, count)) {
                StreamNeedClose<SolvableItemSet> finalSets = possibleSets;
                if (specialFilter != null)
                    finalSets = finalSets.filter(specialFilter);
                return finalSets.max(Comparator.comparingLong(model::calcRating));
            }
        } else {
            return runSolverSingleThread(model, items, adjustment, count, specialFilter);
        }
    }

    public static StreamNeedClose<SolvableItemSet> runSolverPartial(ModelCombined model, SolvableEquipOptionsMap items, StatBlock adjustment, Instant startTime, long count) {
        LongStream dumbStream = BigStreamUtil.generateDumbStream(count, 1);
        Stream<SolvableItemSet> setStream = dumbStream.parallel()
                                              .mapToObj(x -> makeSet(items, adjustment));
        StreamNeedClose<SolvableItemSet> countedStream = BigStreamUtil.countProgress(count, startTime, setStream);
        return model.filterSets(countedStream, true);
    }

    public static Optional<SolvableItemSet> runSolverSingleThread(ModelCombined model, SolvableEquipOptionsMap items, StatBlock adjustment, long count, Predicate<SolvableItemSet> specialFilter) {
        Random random = ThreadLocalRandom.current();
        BestHolder<SolvableItemSet> best = new BestHolder<>();
        for (int i = 0; i < count; ++i) {
            SolvableItemSet set = makeSet(items, adjustment, random);
            if (model.filterOneSet(set) && (specialFilter == null || specialFilter.test(set))) {
                best.add(set, model.calcRating(set));
            }
        }
        return Optional.ofNullable(best.get());
    }

    private static SolvableItemSet makeSet(SolvableEquipOptionsMap items, StatBlock adjustment) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return makeSet(items, adjustment, random);
    }

    private static SolvableItemSet makeSet(SolvableEquipOptionsMap items, StatBlock adjustment, Random random) {
        SolvableEquipMap chosen = SolvableEquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            SolvableItem[] itemList = items.get(slot);
            if (itemList != null) {
                SolvableItem item = ArrayUtil.rand(itemList, random);
                chosen.put(slot, item);
            }
        }
        return SolvableItemSet.manyItems(chosen, adjustment);
    }
}
