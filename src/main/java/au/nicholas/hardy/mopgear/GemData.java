package au.nicholas.hardy.mopgear;

import java.util.Arrays;
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
        map.put(76633, new StatBlock(0, 0, 0, 0, 320, 0, 0, 0));
        map.put(76699, new StatBlock(0, 0, 0, 0, 320, 0, 0, 0));
        map.put(76618, new StatBlock(80, 0, 0, 160, 0, 0, 0, 0));
        map.put(76642, new StatBlock(0, 0, 0, 160, 160, 0, 0, 0));
        map.put(76636, new StatBlock(0, 0, 0, 320, 0, 0, 0, 0));
        map.put(76570, new StatBlock(0, 0, 0, 320, 0, 0, 0, 0));
        map.put(76693, new StatBlock(0, 0, 0, 0, 0, 320, 0, 0));
        map.put(76576, new StatBlock(0, 0, 0, 160, 160, 0, 0, 0));
        map.put(4419, new StatBlock(80, 0, 0, 0, 0, 0, 0, 0)); // chest stats
        map.put(4411, new StatBlock(0, 170, 0, 0, 0, 0, 0, 0)); // bracer
        map.put(4432, new StatBlock(170, 0, 0, 0, 0, 0, 0, 0)); // glove
        map.put(4426, new StatBlock(0, 0, 0, 0, 175, 0, 0, 0)); // foot
        map.put(4099, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0)); // weap
        map.put(4441, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0)); // weap
        map.put(5001, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0)); // shield spike
        map.put(4412, new StatBlock(0, 0, 0, 0, 0, 0, 170, 0)); // bracer
        map.put(4427, new StatBlock(0, 0, 0, 175, 0, 0, 170, 0)); // foot
        return map;
    }

    public static StatBlock process(int[] gemIds, SlotItem slot) {
//        if (ModelCommon.blacksmith && (slot == SlotItem.Wrist || slot == SlotItem.Hand) && gemIds.length > 0) {
//            gemIds = Arrays.copyOf(gemIds, gemIds.length - 1);
//        }

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
