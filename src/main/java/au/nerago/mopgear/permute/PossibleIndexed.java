package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.FullItemData;
import au.nerago.mopgear.domain.ItemRef;
import au.nerago.mopgear.util.BigStreamUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class PossibleIndexed {
    public static Stream<Map<ItemRef, FullItemData>> runSolverPartial(Map<ItemRef, List<FullItemData>> itemMap, long count, long skip) {
        LongStream dumbStream = BigStreamUtil.generateDumbStream(count, skip);
        return dumbStream.parallel()
                .mapToObj(idx -> makeSet(itemMap, idx));
    }

    private static Map<ItemRef, FullItemData> makeSet(Map<ItemRef, List<FullItemData>> items, long mainIndex) {
        HashMap<ItemRef, FullItemData> possible = new HashMap<>();
        for (Map.Entry<ItemRef, List<FullItemData>> entry : items.entrySet()) {
            List<FullItemData> list = entry.getValue();
            int size = list.size();

            int thisIndex = (int) (mainIndex % size);
            mainIndex /= size;

            FullItemData choice = list.get(thisIndex);
            possible.put(entry.getKey(), choice);
        }
        return possible;
    }
}
