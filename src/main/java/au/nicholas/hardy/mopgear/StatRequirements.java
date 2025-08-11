package au.nicholas.hardy.mopgear;

import java.util.stream.Stream;

public class StatRequirements {
    private StatRequirements(int hit, int expertise, int exceed) {
        requiredHit = hit;
        requiredExpertise = expertise;
        maxExceed = exceed;
    }

    public static StatRequirements ret() {
        return new StatRequirements(TARGET_RATING_REGULAR, TARGET_RATING_REGULAR, DEFAULT_CAP_ALLOW_EXCEED);
    }

    public static StatRequirements retWideCapRange() {
        return new StatRequirements(TARGET_RATING_REGULAR, TARGET_RATING_REGULAR, DEFAULT_CAP_ALLOW_EXCEED * 5);
    }

    public static StatRequirements prot() {
        return new StatRequirements(TARGET_RATING_REGULAR, TARGET_RATING_TANK, DEFAULT_CAP_ALLOW_EXCEED);
    }

    public static StatRequirements zero() {
        return new StatRequirements(0, 0, DEFAULT_CAP_ALLOW_EXCEED);
    }

    private static final double RATING_PER_PERCENT = 339.9534;
    static final double TARGET_PERCENT_REGULAR = 7.5;
    static final double TARGET_PERCENT_TANK = 15;
    private static final int TARGET_RATING_REGULAR = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_REGULAR); // 2550
    private static final int TARGET_RATING_TANK = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_TANK); // 5100

    private static final int DEFAULT_CAP_ALLOW_EXCEED = 50;

    private final int requiredHit;
    private final int requiredExpertise;
    private final int maxExceed;

    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
//        return sets.filter(set -> hasNoDuplicate(set.items) && inRange2(set.getTotals()));
        return stream.filter(set -> inRange(set.getTotals()));
    }

    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return stream.filter(set -> inRangeMax(set.getTotals()));
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean inRange(StatBlock totals) {
        if (requiredHit != 0) {
            if (totals.hit < requiredHit || totals.hit > requiredHit + maxExceed)
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
            if (totals.hit > requiredHit + maxExceed)
                return false;
        }
        if (requiredExpertise != 0) {
            if (totals.expertise > requiredExpertise + maxExceed)
                return false;
        }
        return true;
    }
}
