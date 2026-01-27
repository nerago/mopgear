package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.FullItemData;
import au.nerago.mopgear.domain.ItemRef;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BigStreamUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class PossibleRandom {
    public static Stream<Map<ItemRef, FullItemData>> runSolverPartial(Map<ItemRef, List<FullItemData>> itemMap, long count) {
        LongStream dumbStream = BigStreamUtil.generateDumbStream(count, 1);
        return dumbStream.parallel()
                .mapToObj(x -> makeSet(itemMap));
    }

    private static Map<ItemRef, FullItemData> makeSet(Map<ItemRef, List<FullItemData>> items) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return makeSet(items, random);
    }

    private static Map<ItemRef, FullItemData> makeSet(Map<ItemRef, List<FullItemData>> items, ThreadLocalRandom random) {
        HashMap<ItemRef, FullItemData> possible = new HashMap<>();
        for (Map.Entry<ItemRef, List<FullItemData>> entry : items.entrySet()) {
            FullItemData choice = ArrayUtil.rand(entry.getValue(), random);
            possible.put(entry.getKey(), choice);
        }
        return possible;
    }
}
