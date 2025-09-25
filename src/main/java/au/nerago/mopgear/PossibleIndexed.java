package au.nerago.mopgear;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemRef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class PossibleIndexed {
    public static Stream<Map<ItemRef, ItemData>> runSolverPartial(Map<ItemRef, List<ItemData>> itemMap, long count, long skip) {
        Stream<Long> dumbStream = generateDumbStream(count, skip);
        return dumbStream.parallel()
                .map(idx -> makeSet(itemMap, idx));
    }

    private static Stream<Long> generateDumbStream(long count, long skip) {
        long start = ThreadLocalRandom.current().nextLong(skip);
        return Stream.iterate(start, x -> x < count, x -> x + skip);
    }

    private static Map<ItemRef, ItemData> makeSet(Map<ItemRef, List<ItemData>> items, long mainIndex) {
        HashMap<ItemRef, ItemData> possible = new HashMap<>();
        for (Map.Entry<ItemRef, List<ItemData>> entry : items.entrySet()) {
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
