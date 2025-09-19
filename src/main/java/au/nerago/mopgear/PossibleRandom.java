package au.nerago.mopgear;

import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.domain.SlotEquip;
import au.nerago.mopgear.util.ArrayUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class PossibleRandom {
    public static Stream<Map<Integer, ItemData>> runSolverPartial(Map<Integer, List<ItemData>> itemMap, long count) {
        Stream<Long> dumbStream = generateDumbStream(count);
        return dumbStream.parallel()
                .map(x -> makeSet(itemMap));
    }

    private static Stream<Long> generateDumbStream(long count) {
        return Stream.iterate(0L, x -> x < count, x -> x + 1);
    }

    private static Map<Integer, ItemData> makeSet(Map<Integer, List<ItemData>> items) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return makeSet(items, random);
    }

    private static Map<Integer, ItemData> makeSet(Map<Integer, List<ItemData>> items, ThreadLocalRandom random) {
        HashMap<Integer, ItemData> possible = new HashMap<>();
        for (Map.Entry<Integer, List<ItemData>> entry : items.entrySet()) {
            ItemData choice = ArrayUtil.rand(entry.getValue(), random);
            possible.put(entry.getKey(), choice);
        }
        return possible;
    }
}
