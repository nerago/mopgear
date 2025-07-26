package au.nicholas.hardy.mopgear;

import java.util.HashMap;
import java.util.Map;

public class GemData {
    static final Map<Integer, StatBlock> known = buildKnown();

    private static Map<Integer, StatBlock> buildKnown() {
        Map<Integer, StatBlock> map = new HashMap<>();
        map.put(76886, new StatBlock(216, 0, 0, 0, 0, 0, 0, 0));
        map.put(77542, new StatBlock(0, 0, 0, 0, 600, 0, 0, 0));
        map.put(77545, new StatBlock(0, 0, 0, 600, 0, 0, 0, 0));
        map.put(77541, new StatBlock(0, 0, 600, 0, 0, 0, 0, 0));
        map.put(77547, new StatBlock(0, 600, 0, 0, 0, 0, 0, 0));
        map.put(77543, new StatBlock(0, 0, 0, 0, 0, 600, 0, 0));
        map.put(76537, new StatBlock(60, 0, 0, 0, 120, 0, 0, 0));
        return map;
    }

    public static StatBlock process(int[] gemIds) {
        StatBlock result = StatBlock.empty;
        for (int id : gemIds) {
            StatBlock stats = known.get(id);
            if (stats == null)
                throw new IllegalArgumentException("unknown gem " + id);
            result = result.plus(stats);
        }
        return result;
    }
}
