package au.nerago.mopgear;

import au.nerago.mopgear.domain.ItemData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PossibleIndexed {
    public static Stream<Map<Integer, ItemData>> runSolverPartial(Map<Integer, List<ItemData>> itemMap, long count, long skip) {
        // TODO check if aligns with find non-one set
        Stream<Long> dumbStream = generateDumbStream(count, skip);
        return dumbStream.parallel()
                .map(x -> makeSet(itemMap, x));
    }

    private static Stream<Long> generateDumbStream(long count, long skip) {
        return Stream.iterate(0L, x -> x < count, x -> x + skip);
    }

    private static Map<Integer, ItemData> makeSet(Map<Integer, List<ItemData>> items, long mainIndex) {
        HashMap<Integer, ItemData> possible = new HashMap<>();
        for (Map.Entry<Integer, List<ItemData>> entry : items.entrySet()) {
            List<ItemData> list = entry.getValue();
            int size = list.size();

            int thisIndex = (int) (mainIndex % size);
            mainIndex /= size;

            ItemData choice = list.get(thisIndex);
            possible.put(entry.getKey(), choice);
        }
        return possible;
    }
}
