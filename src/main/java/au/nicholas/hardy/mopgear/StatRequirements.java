package au.nicholas.hardy.mopgear;

import java.util.stream.Stream;

public class StatRequirements {
    private StatRequirements(int hit, int expertise, int exceed, boolean combine) {
        requiredHit = hit;
        requiredExpertise = expertise;
        maxExceed = exceed;
        combineHitLike = combine;
    }

    public static StatRequirements ret() {
        return new StatRequirements(TARGET_RATING_MELEE, TARGET_RATING_MELEE, DEFAULT_CAP_ALLOW_EXCEED, false);
    }

    public static StatRequirements retWideCapRange() {
        return new StatRequirements(TARGET_RATING_MELEE, TARGET_RATING_MELEE, DEFAULT_CAP_ALLOW_EXCEED * 5, false);
    }

    public static StatRequirements prot() {
        return new StatRequirements(TARGET_RATING_MELEE, TARGET_RATING_TANK, DEFAULT_CAP_ALLOW_EXCEED, false);
    }

    public static StatRequirements boom() {
        return new StatRequirements(TARGET_RATING_CAST, 0, DEFAULT_CAP_ALLOW_EXCEED, true);
    }

    public static StatRequirements zero() {
        return new StatRequirements(0, 0, DEFAULT_CAP_ALLOW_EXCEED, false);
    }

    private static final double RATING_PER_PERCENT = 339.9534;
    private static final double TARGET_PERCENT_MELEE = 7.5;
    private static final double TARGET_PERCENT_TANK = 15;
    private static final double TARGET_PERCENT_CAST = 15;
    private static final int TARGET_RATING_MELEE = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_MELEE); // 2550
    private static final int TARGET_RATING_TANK = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_TANK); // 5100
    private static final int TARGET_RATING_CAST = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_CAST); // 5100

    private static final int DEFAULT_CAP_ALLOW_EXCEED = 150;

    private final int requiredHit;
    private final int requiredExpertise;
    private final int maxExceed;
    private final boolean combineHitLike;

    private int effectiveHit(StatBlock totals) {
        return combineHitLike ? totals.hit + totals.expertise + totals.spirit : totals.hit;
    }

    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        return stream.filter(set -> inRange(set.getTotals()));
    }

    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return stream.filter(set -> inRangeMax(set.getTotals()));
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean inRange(StatBlock totals) {
        if (requiredHit != 0) {
            int hit = effectiveHit(totals);
            if (hit < requiredHit || hit > requiredHit + maxExceed)
                return false;
        }
        if (requiredExpertise != 0) {
            if (totals.expertise < requiredExpertise || totals.expertise > requiredExpertise + maxExceed)
                return false;
        }
        return true;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean inRangeMax(StatBlock totals) {
        if (requiredHit != 0) {
            int hit = effectiveHit(totals);
            if (hit > requiredHit + maxExceed)
                return false;
        }
        if (requiredExpertise != 0) {
            if (totals.expertise > requiredExpertise + maxExceed)
                return false;
        }
        return true;
    }
}
