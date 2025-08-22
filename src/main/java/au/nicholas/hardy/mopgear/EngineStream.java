package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.BigStreamUtil;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class EngineStream {
    public static Optional<ItemSet> runSolver(ModelCombined model, EquipOptionsMap items, Instant startTime, StatBlock adjustment, ItemSet otherSet, long estimate) {
        Stream<ItemSet> finalSets = runSolverPartial(model, items, startTime, adjustment, otherSet, estimate);
        return BigStreamUtil.findBest(model, finalSets);
    }

    public static Stream<ItemSet> runSolverPartial(ModelCombined model, EquipOptionsMap items, Instant startTime, StatBlock adjustment, ItemSet otherSet, long estimate) {
        if (estimate == 0)
            estimate = ItemUtil.estimateSets(items);

        Stream<ItemSet> initialSets = generateItemCombinations(items, model, otherSet, adjustment);

        if (startTime != null)
            initialSets = BigStreamUtil.countProgress(estimate, startTime, initialSets);

        return model.filterSets(initialSets).parallel();
    }

    // NOTES: we could dig right down a path to find its max/min hit/exp limits, then know if we're on a bad path
    private static Stream<ItemSet> generateItemCombinations(EquipOptionsMap itemsBySlot, ModelCombined model, ItemSet otherSet, StatBlock adjustment) {
        Stream<ItemSet> stream = null;

        List<Tuple.Tuple2<SlotEquip, ItemData[]>> sortedEntries =
                itemsBySlot.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(x -> -x.b().length)).toList();
        for (Tuple.Tuple2<SlotEquip, ItemData[]> slotEntry : sortedEntries) {
            if (stream == null) {
                stream = newCombinationStream(slotEntry.a(), slotEntry.b(), otherSet, adjustment);
            } else {
                stream = applyItemsToCombination(stream, slotEntry.a(), slotEntry.b());
            }
            stream = model.filterSetsMax(stream);
        }
        return stream;
    }

    private static Stream<ItemSet> newCombinationStream(SlotEquip slot, ItemData[] slotItems, ItemSet otherSet, StatBlock adjustment) {
        final ItemSet[] initialSets = new ItemSet[slotItems.length];
        for (int i = 0; i < slotItems.length; ++i) {
            initialSets[i] = ItemSet.singleItem(slot, slotItems[i], otherSet, adjustment);
        }
        return ArrayUtil.arrayStream(initialSets);
    }

    private static Stream<ItemSet> applyItemsToCombination(Stream<ItemSet> stream, SlotEquip slot, ItemData[] slotItems) {
//        return stream.flatMap(upstream -> ArrayUtil.arrayStream(slotItems).map(add -> upstream.copyWithAddedItem(slot, add)));

        Stream<ItemSet> n = stream.mapMulti((set, sink) -> {
            for (ItemData add : slotItems) {
                sink.accept(set.copyWithAddedItem(slot, add));
            }
        });
        return n.parallel();
    }
}
