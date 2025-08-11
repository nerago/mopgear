package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.ArrayUtil;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ItemLevel {
    private static final double CHALLENGE_TARGET_LEVEL = 463.0;
    private static final Map<Integer, Double> multiplier = buildMultiplier();
    private static Map<Integer, Double> buildMultiplier() {
        Map<Integer, Double> map = new HashMap<>();
        map.put(463, 1.0);
        map.put(476, 0.8851);
        map.put(483, 0.8293);
        map.put(489, 0.7852);
        map.put(502, 0.6958);
        return map;
    }

    private static Map<Integer, Integer> levelLookup = buildLookup();

    private static Map<Integer, Integer> buildLookup() {
        Map<Integer, Integer> map = new HashMap<>();
        // https://www.wowhead.com/mop-classic/items/armor/plate/min-level:502/max-level:502/quality:3:4?filter=20;1;0
        for (int id : new int[]{81694, 82812, 81270, 81284, 81241, 81190, 81098, 81574, 81187, 81230, 81073, 82857, 81100, 81130, 82856, 81083, 81287, 81113, 81572, 81070, 81065, 82821, 81274, 81101, 81687, 90600, 81248, 82852, 81696, 90603, 81086}) {
            map.put(id, 463);
        }
        for (int id : new int[]{77539, 82975, 82976, 82979, 90579, 82980, 90912, 90576, 86803, 86742, 90910, 89976, 90577, 86751, 86752, 86779, 89969, 86794, 86780, 98786, 86793, 86760, 98930, 91656, 91789, 91506, 91504, 91620, 91622, 91500, 98847, 98784, 98785, 91652, 91791, 98787, 98928, 98929, 91626, 91654, 91658, 91785, 91659, 98864, 91628, 98861, 91783, 95524, 98843, 98845, 98859, 91508, 98788, 98846, 98860, 91787, 98926, 91502, 91624, 91650, 95533, 98844, 98862, 98863, 98927}) {
            map.put(id, 476);
        }
        for (int id : new int[]{84950, 84949, 84870, 85063, 84876, 84822, 84835, 84985, 84856, 84872, 84810, 84840, 84797, 89958, 85103, 84986, 86849, 86860, 86904, 84918, 86673, 85077, 86868, 89954, 86903, 86666, 84853, 84915, 84922, 86870, 89963, 86852, 84999, 86832, 86822, 86854, 84794, 86823, 89956, 89981, 85091, 86681, 84851, 86659, 84834, 86656, 84795, 86678, 84987, 85046, 85057, 86677, 86658, 86668, 84993, 85028, 86661, 86664, 85027, 86670, 86676, 85032, 85102, 86654, 86671, 86672, 85000, 86674, 86682, 84992, 86675, 86680, 85044, 85086, 86667, 86848, 85059, 86683, 86660, 86891, 86655, 86662, 86669, 86679, 85019, 86657, 86663, 86665}) {
            map.put(id, 483);
        }
        for (int id : new int[]{87015, 87071, 87048, 87024, 89941, 87059, 87049, 87060, 89934, 87035, 87025, 95789, 95724, 95806, 95920, 95652, 95986, 98143, 95834, 95874, 95975, 95978, 95979, 95629, 95831, 95832, 95733, 95921, 95650, 95683, 95735, 95807, 95924, 95987, 95674, 95703, 95753, 95833, 95702, 95734, 95752, 95808, 95827, 95873, 95913, 95976, 98141, 95631, 95651, 95732, 95754, 95778, 95798, 95826, 95828, 95829, 95910, 95911, 95914, 95922, 95990, 95992, 95995, 95630, 95684, 95725, 95825, 95830, 95912, 95923, 95988, 95989, 95991, 95993, 95994}) {
            map.put(id, 502);
        }
        map.put(85991, 489);
        map.put(84807, 483);
        map.put(81129, 463);
        map.put(89503, 463);
        map.put(89649, 458);
        map.put(89665, 458);
        map.put(90862, 489);
        map.put(89069, 489);
        map.put(81268, 463);
        map.put(81138, 463);
        map.put(82822, 463);
        map.put(84790, 483);
        map.put(82814, 463);
        return map;
    }

    public static EnumMap<SlotEquip, ItemData[]> scaleForChallengeMode(EnumMap<SlotEquip, ItemData[]> itemMap) {
        EnumMap<SlotEquip, ItemData[]> result = new EnumMap<>(SlotEquip.class);
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] items = itemMap.get(slot);
            if (items != null) {
                result.put(slot, ArrayUtil.mapAsNew(items, ItemLevel::scaleForChallengeMode));
            }
        }
        return result;
    }

    public static ItemData scaleForChallengeMode(ItemData item) {
        Integer level = levelLookup.get(item.id);
        if (level == null) {
            throw new IllegalArgumentException("item level not known " + item.id + " " + item);
        }
        if (level <= CHALLENGE_TARGET_LEVEL) {
            return item;
        }
        if (!multiplier.containsKey(level)) {
            throw new IllegalArgumentException("missing level scaling for " + level);
        }

        double factor = multiplier.get(level);
        StatBlock stats = item.stat;
        for (StatType type : StatType.values()) {
            double val = stats.get(type);
            if (val != 0) {
                val *= factor;
                stats = stats.withChange(type, (int) Math.round(val));
            }

        }
        System.out.println("SCALED " + item.name + " " + stats);
        return item.changeStats(stats);
    }
}
