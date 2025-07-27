package au.nicholas.hardy.mopgear;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public class ModelCommon {

//    public final int str;
//    public final int mastery;
//    public final int crit;
//    public final int hit;
//    public final int haste;
//    public final int expertise;
//    public final int dodge;
//    public final int parry;

    public static StatType[] reforgeSource = new StatType[]{StatType.Mastery, StatType.Crit, StatType.Hit, StatType.Haste, StatType.Expertise, StatType.Dodge, StatType.Parry};

    //    static final Secondary[] reforgeTargets = new Secondary[]{Secondary.Hit, Secondary.Expertise, Secondary.Haste, Secondary.Mastery};
    public static final StatType[] reforgeTargets = new StatType[]{StatType.Hit, StatType.Expertise, StatType.Haste};

    private static final double RATING_PER_PERCENT = 339.9534;
    //    static final double TARGET_PERCENT = 7.5; // for bosses
    private static final double TARGET_PERCENT = 6; // for heroics
    private static final int TARGET_RATING = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT); // 2040 / 2550

    private static final int RATING_CAP_ALLOW_EXCEED = 300;

    public static final EnumMap<StatType, Integer> requiredAmounts = buildRequired();

    public static boolean blacksmith = true;

    private static EnumMap<StatType, Integer> buildRequired() {
        EnumMap<StatType, Integer> map = new EnumMap<>(StatType.class);
        map.put(StatType.Hit, TARGET_RATING);
        map.put(StatType.Expertise, TARGET_RATING);
        return map;
    }

    @SuppressWarnings("ConstantValue")
    public static void validate() {
        if (Arrays.stream(reforgeTargets).distinct().count() != reforgeTargets.length)
            throw new IllegalStateException("reforgeTargets not distinct");
        if (!Arrays.asList(reforgeTargets).containsAll(requiredAmounts.keySet()))
            throw new IllegalStateException("todo");
    }

    public static Stream<ItemSet> filterSets(Stream<ItemSet> sets) {
        return sets.filter(set -> inRange2(set.getTotals()));
    }

    public static boolean inRange2(StatBlock totals) {
        for (Map.Entry<StatType, Integer> entry : requiredAmounts.entrySet()) {
            int val = totals.get(entry.getKey()), cap = entry.getValue();
            if (val < cap || val > cap + RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        return true;
    }
}
