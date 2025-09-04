package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;

import java.util.*;
import java.util.stream.Stream;

public class SolverCompleteStreams {
    public static Stream<Map<Integer, ItemData>> runSolverPartial(ModelCombined model, Map<Integer, List<ItemData>> items) {
        Stream<Map<Integer, ItemData>> initialSets = generateItemCombinations(items, model);
        return initialSets.parallel();
    }

    private static Stream<Map<Integer, ItemData>> generateItemCombinations(Map<Integer, List<ItemData>> items, ModelCombined model) {
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
        }

        return stream;
    }

    private static Stream<Map<Integer, ItemData>> newCombinationStream(List<ItemData> options) {
        return options.parallelStream().map(forgedItem -> Map.of(forgedItem.id, forgedItem));
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
