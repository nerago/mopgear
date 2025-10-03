package au.nerago.mopgear;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemRef;
import au.nerago.mopgear.util.ArrayUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class PossibleRandom {
    public static Stream<Map<ItemRef, ItemData>> runSolverPartial(Map<ItemRef, List<ItemData>> itemMap, long count) {
        Stream<Long> dumbStream = generateDumbStream(count);
        return dumbStream.parallel()
                .map(x -> makeSet(itemMap));
    }

    private static Stream<Long> generateDumbStream(long count) {
        return Stream.iterate(0L, x -> x < count, x -> x + 1);
    }

    private static Map<ItemRef, ItemData> makeSet(Map<ItemRef, List<ItemData>> items) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return makeSet(items, random);
    }

    private static Map<ItemRef, ItemData> makeSet(Map<ItemRef, List<ItemData>> items, ThreadLocalRandom random) {
        HashMap<ItemRef, ItemData> possible = new HashMap<>();
        for (Map.Entry<ItemRef, List<ItemData>> entry : items.entrySet()) {
            ItemData choice = ArrayUtil.rand(entry.getValue(), random);
            possible.put(entry.getKey(), choice);
        }
        return possible;
    }
}
