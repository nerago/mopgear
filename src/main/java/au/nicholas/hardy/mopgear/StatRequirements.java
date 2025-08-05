package au.nicholas.hardy.mopgear;

import java.util.stream.Stream;

public class StatRequirements {
    public StatRequirements(boolean tankExpertise) {
        requiredHit = TARGET_RATING_REGULAR;
        if (tankExpertise)
            requiredExpertise = TARGET_RATING_TANK;
//            requiredExpertise = 0;
        else
            requiredExpertise = TARGET_RATING_REGULAR;
    }

    private static final double RATING_PER_PERCENT = 339.9534;
    static final double TARGET_PERCENT_REGULAR = 7.5;
    static final double TARGET_PERCENT_TANK = 15;
    private static final int TARGET_RATING_REGULAR = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_REGULAR); // 2550
    private static final int TARGET_RATING_TANK = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_TANK); // 5100

    private static final int RATING_CAP_ALLOW_EXCEED = 50;

    private final int requiredHit;
    private final int requiredExpertise;

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
            if (totals.hit < requiredHit || totals.hit > requiredHit + RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        if (requiredExpertise != 0) {
            if (totals.expertise < requiredExpertise || totals.expertise > requiredExpertise + RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        return true;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean inRangeMax(StatBlock totals) {
        if (requiredHit != 0) {
            if (totals.hit > requiredHit + RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        if (requiredExpertise != 0) {
            if (totals.expertise > requiredExpertise + RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        return true;
    }
}
