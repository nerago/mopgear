package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.ArrayUtil;

import java.util.*;
import java.util.stream.Stream;

public class PossibleStreams {
    public static Stream<Map<Integer, ItemData>> runSolverPartial(Map<Integer, List<ItemData>> items) {
        Stream<Map<Integer, ItemData>> initialSets = generateItemCombinations(items);
        return initialSets.parallel();
    }

    private static Stream<Map<Integer, ItemData>> generateItemCombinations(Map<Integer, List<ItemData>> items) {
        Stream<Map<Integer, ItemData>> stream = null;

//        List<Map.Entry<Integer, List<ItemData>>> sortedEntries =
//                itemsBySlot
//                        .entrySet()
//                        .stream()
//                        .sorted(Comparator.comparingInt(x -> x.getValue().size()))
//                        .toList();

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
    private static Stream<Map<Integer, ItemData>> newCombinationStream(List<ItemData> options) {
        Map<Integer, ItemData>[] array = (Map<Integer, ItemData>[]) new Map[options.size()];
        for (int i = 0; i < options.size(); ++i) {
            ItemData item = options.get(i);
            array[i] = Map.of(item.id, item);
        }
        return ArrayUtil.arrayStream(array);
//        return options.parallelStream().map(forgedItem -> Map.of(forgedItem.id, forgedItem));
    }

    private static Stream<Map<Integer, ItemData>> applyItemsToCombination(Stream<Map<Integer, ItemData>> stream, List<ItemData> options) {
        return stream.mapMulti((map, sink) -> {
            for (ItemData add : options) {
                sink.accept(copyWithAddedItem(map, add));
            }
        });
    }

    private static Map<Integer, ItemData> copyWithAddedItem(Map<Integer, ItemData> oldMap, ItemData add) {
        Map<Integer, ItemData> map = new HashMap<>(oldMap);
        map.put(add.id, add);
        return map;
    }
}
