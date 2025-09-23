package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.ArrayUtil;

import java.util.*;
import java.util.stream.Stream;

public class PossibleStreams {
    public static Stream<Map<ItemRef, ItemData>> runSolverPartial(Map<Integer, List<ItemData>> items) {
        Stream<Map<ItemRef, ItemData>> initialSets = generateItemCombinations(items);
        return initialSets.parallel();
    }

    private static Stream<Map<ItemRef, ItemData>> generateItemCombinations(Map<Integer, List<ItemData>> items) {
        Stream<Map<ItemRef, ItemData>> stream = null;

        for (Map.Entry<Integer, List<ItemData>> slotEntry : items.entrySet()) {
            if (stream == null) {
                stream = newCombinationStream(slotEntry.getValue());
            } else {
                stream = applyItemsToCombination(stream, slotEntry.getValue());
            }
            stream = stream.parallel().unordered();
        }

        return stream;
    }

    @SuppressWarnings("unchecked")
    private static Stream<Map<ItemRef, ItemData>> newCombinationStream(List<ItemData> options) {
        Map<ItemRef, ItemData>[] array = (Map<ItemRef, ItemData>[]) new Map[options.size()];
        for (int i = 0; i < options.size(); ++i) {
            ItemData item = options.get(i);
            array[i] = Map.of(item.ref, item);
        }
        return ArrayUtil.arrayStream(array);
    }

    private static Stream<Map<ItemRef, ItemData>> applyItemsToCombination(Stream<Map<ItemRef, ItemData>> stream, List<ItemData> options) {
        return stream.mapMulti((map, sink) -> {
            for (ItemData add : options) {
                sink.accept(copyWithAddedItem(map, add));
            }
        });
    }

    private static Map<ItemRef, ItemData> copyWithAddedItem(Map<ItemRef, ItemData> oldMap, ItemData add) {
        Map<ItemRef, ItemData> map = new HashMap<>(oldMap);
        map.put(add.ref, add);
        return map;
    }
}
