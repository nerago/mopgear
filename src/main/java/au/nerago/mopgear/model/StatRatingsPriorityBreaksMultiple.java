package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StatRatingsPriorityBreaksMultiple extends StatRatings {
    public static final int STEP = 4;
    public static final long INITIAL = 0x100000;
    private final StatType breakpointStat;
    private final int[] breakpointsArray;
    private final StatType[] remainPriority;
    private final static int OUTPUT_MULTIPLY = 1; // scale to similar rates as weighting

    public StatRatingsPriorityBreaksMultiple(StatType breakpointStat, int[] breakpointsArray, StatType[] remainPriority) {
        this.breakpointStat = breakpointStat;
        this.breakpointsArray = breakpointsArray;
        this.remainPriority = remainPriority;
        chooseGems();
        chooseBestStats();
        validate();
    }

    public void validate() {
        if (remainPriority.length < 2)
            throw new IllegalArgumentException("need some more stats to consider");
        if (Arrays.stream(remainPriority).anyMatch(s -> s == breakpointStat))
            throw new IllegalArgumentException("breakpoint stat shouldn't be repeated as remaining");
        if (Arrays.stream(remainPriority).distinct().count() != remainPriority.length)
            throw new IllegalStateException("priorities not distinct");
        if (!Arrays.equals(breakpointsArray, Arrays.stream(breakpointsArray).sorted().toArray()))
            throw new IllegalStateException("breakpoints not ordered");
    }

    @Override
    public long calcRating(StatBlock totals) {
        final int hasteRating = totals.get(breakpointStat);
        int effectiveHaste = 0;
        for (int i = breakpointsArray.length - 1; i >= 0; i--) {
            int num = breakpointsArray[i];
            if (hasteRating >= num) {
                effectiveHaste = num;
                break;
            }
        }
        int excessHaste = hasteRating - effectiveHaste;

        long multiply = INITIAL;
        long result = effectiveHaste * multiply;
        result += excessHaste; // multiply 1

        for (StatType stat : remainPriority) {
            int value = totals.get(stat);
            multiply /= STEP;
            result += value * multiply;
        }

        return result * OUTPUT_MULTIPLY;
    }

    @Override
    public long calcRating(StatType queryStat, int queryValue) {
        if (queryStat == breakpointStat) {
            int effectiveHaste = 0;
            for (int i = breakpointsArray.length - 1; i >= 0; i--) {
                int num = breakpointsArray[i];
                if (queryValue >= num) {
                    effectiveHaste = num;
                    break;
                }
            }

            int excessHaste = queryValue - effectiveHaste;
            long result = (effectiveHaste * INITIAL) + excessHaste;
            return result * OUTPUT_MULTIPLY;
        } else {
            long multiply = INITIAL;
            for (StatType stat : remainPriority) {
                multiply /= STEP;
                if (stat == queryStat) {
                    return queryValue * multiply * OUTPUT_MULTIPLY;
                }
            }
            return 0;
        }
    }
}
