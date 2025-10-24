package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.FullItemData;
import au.nerago.mopgear.domain.ItemRef;
import au.nerago.mopgear.util.ArrayUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PossibleStreams {
    public static Stream<Map<ItemRef, FullItemData>> runSolverPartial(Map<Integer, List<FullItemData>> items) {
        Stream<Map<ItemRef, FullItemData>> initialSets = generateItemCombinations(items);
        return initialSets.parallel();
    }

    private static Stream<Map<ItemRef, FullItemData>> generateItemCombinations(Map<Integer, List<FullItemData>> items) {
        Stream<Map<ItemRef, FullItemData>> stream = null;

        for (Map.Entry<Integer, List<FullItemData>> slotEntry : items.entrySet()) {
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
    private static Stream<Map<ItemRef, FullItemData>> newCombinationStream(List<FullItemData> options) {
        Map<ItemRef, FullItemData>[] array = (Map<ItemRef, FullItemData>[]) new Map[options.size()];
        for (int i = 0; i < options.size(); ++i) {
            FullItemData item = options.get(i);
            array[i] = Map.of(item.ref(), item);
        }
        return ArrayUtil.arrayStream(array);
    }

    private static Stream<Map<ItemRef, FullItemData>> applyItemsToCombination(Stream<Map<ItemRef, FullItemData>> stream, List<FullItemData> options) {
        return stream.mapMulti((map, sink) -> {
            for (FullItemData add : options) {
                sink.accept(copyWithAddedItem(map, add));
            }
        });
    }

    private static Map<ItemRef, FullItemData> copyWithAddedItem(Map<ItemRef, FullItemData> oldMap, FullItemData add) {
        Map<ItemRef, FullItemData> map = new HashMap<>(oldMap);
        map.put(add.ref(), add);
        return map;
    }
}
