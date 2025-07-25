package au.nicholas.hardy.mopgear;

import java.util.Arrays;
import java.util.EnumMap;

public class ModelParams {
    static final Secondary[] priority = new Secondary[]{Secondary.Haste, Secondary.Mastery, Secondary.Crit};
//    static final Secondary[] reforgeTargets = new Secondary[]{Secondary.Hit, Secondary.Expertise, Secondary.Haste, Secondary.Mastery};
    static final Secondary[] reforgeTargets = new Secondary[]{Secondary.Hit, Secondary.Expertise, Secondary.Haste};

    static final double RATING_PER_PERCENT = 339.9534;
//    static final double TARGET_PERCENT = 7.5; // for bosses
    static final double TARGET_PERCENT = 6.5; // for heroics
    static final int TARGET_RATING = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT); // 2040 / 2550

    static final int RATING_CAP_ALLOW_EXCEED = 300;

    static final EnumMap<Secondary, Integer> requiredAmounts = buildRequired();
    private static EnumMap<Secondary, Integer> buildRequired() {
        EnumMap<Secondary, Integer> map = new EnumMap<>(Secondary.class);
        map.put(Secondary.Hit, TARGET_RATING);
        map.put(Secondary.Expertise, TARGET_RATING);
        return map;
    }

    @SuppressWarnings("ConstantValue")
    public static void validate() {
        if (priority.length > 3)
            throw new IllegalStateException("can't use current number ranking");
        if (Arrays.stream(priority).distinct().count() != priority.length)
            throw new IllegalStateException("priorities not distinct");
        if (Arrays.stream(reforgeTargets).distinct().count() != reforgeTargets.length)
            throw new IllegalStateException("reforgeTargets not distinct");
        if (!Arrays.asList(reforgeTargets).containsAll(requiredAmounts.keySet()))
            throw new IllegalStateException("todo");
    }
}
