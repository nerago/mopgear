package au.nicholas.hardy.mopgear;

import java.util.EnumMap;

public class ModelParams {
    static final Secondary[] priority = new Secondary[]{Secondary.Haste, Secondary.Mastery, Secondary.Crit};
    static final Secondary[] reforgeTargets = new Secondary[]{Secondary.Hit, Secondary.Expertise, Secondary.Haste};

    static final double RATING_PER_PERCENT = 102.46;
    static final double TARGET_PERCENT = 7.5;
    static final int TARGET_RATING = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT); // 768.485373827269

    static final int PERMITTED_EXCEED = 100;

    static final EnumMap<Secondary, Integer> requiredAmounts = buildRequired();
    private static EnumMap<Secondary, Integer> buildRequired() {
        EnumMap<Secondary, Integer> map = new EnumMap<>(Secondary.class);
        map.put(Secondary.Hit, TARGET_RATING);
        map.put(Secondary.Expertise, TARGET_RATING);
        return map;
    }
}
