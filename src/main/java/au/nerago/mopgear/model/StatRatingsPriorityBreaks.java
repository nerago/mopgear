package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.util.Arrays;

public class StatRatingsPriorityBreaks extends StatRatings {
    public static final int STEP = 16;
    public static final long INITIAL = 0x100000;
    private final StatType firstAndLastStat;
    private final int breakpointTarget;
    private final StatType[] remainPriority;
    private final static int OUTPUT_MULTIPLY = 1; // scale to similar rates as weighting

    public StatRatingsPriorityBreaks(StatType firstAndLastStat, int breakpointTarget, StatType[] remainPriority) {
        this.firstAndLastStat = firstAndLastStat;
        this.breakpointTarget = breakpointTarget;
        this.remainPriority = remainPriority;
        chooseGems();
        chooseBestStats();
        validate();
    }

    public void validate() {
        if (remainPriority.length < 2)
            throw new IllegalStateException("need some more stats to consider");
        if (Arrays.asList(remainPriority).contains(firstAndLastStat))
            throw new IllegalStateException("breakpoint stat shouldn't be repeated as remaining");
        if (Arrays.stream(remainPriority).distinct().count() != remainPriority.length)
            throw new IllegalStateException("priorities not distinct");
    }

    @Override
    public long calcRating(StatBlock totals) {
        long result = 0;
        long multiply = INITIAL;
        int breakpointValue = totals.get(firstAndLastStat);
        if (breakpointValue > breakpointTarget) {
            result += breakpointTarget * multiply;
            result += breakpointValue - breakpointTarget;
            multiply /= STEP;

            for (StatType stat : remainPriority) {
                result += totals.get(stat) * multiply;
                multiply /= STEP;
            }
        } else {
            result += breakpointValue * multiply;
            multiply /= STEP;

            for (StatType stat : remainPriority) {
                result += totals.get(stat) * multiply;
                multiply /= STEP;
            }
        }
        return result * OUTPUT_MULTIPLY;
    }

    @Override
    public long calcRating(StatBlock partA, StatBlock partB) {
        long result = 0;
        long multiply = INITIAL;
        int breakpointValue = partA.get(firstAndLastStat) + partB.get(firstAndLastStat);
        if (breakpointValue > breakpointTarget) {
            result += breakpointTarget * multiply;
            result += breakpointValue - breakpointTarget;
            multiply /= STEP;

            for (StatType stat : remainPriority) {
                result += (partA.get(stat) + partB.get(stat)) * multiply;
                multiply /= STEP;
            }
        } else {
            result += breakpointValue * multiply;
            multiply /= STEP;

            for (StatType stat : remainPriority) {
                result += (partA.get(stat) + partB.get(stat)) * multiply;
                multiply /= STEP;
            }
        }
        return result * OUTPUT_MULTIPLY;
    }

    @Override
    public long calcRating(StatType queryStat, int value) {
        if (queryStat == firstAndLastStat) {
            if (value > breakpointTarget) {
                return (breakpointTarget * INITIAL) + (value - breakpointTarget);
            } else {
                return value * INITIAL;
            }
        } else {
            long multiply = INITIAL;
            for (StatType stat : remainPriority) {
                if (stat == queryStat) {
                    return value * multiply * OUTPUT_MULTIPLY;
                }
                multiply /= STEP;
            }
            return 0;
        }
    }
}
