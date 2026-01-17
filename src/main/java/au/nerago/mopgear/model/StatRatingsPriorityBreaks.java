package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StatRatingsPriorityBreaks extends StatRatings {
    public static final int STEP = 4;
    public static final long INITIAL = 0x100000;
    private final StatType firstAndLastStat;
    private final int breakpointTarget;
    private final StatType[][] remainPriority;

    public StatRatingsPriorityBreaks(StatType firstAndLastStat, int breakpointTarget, StatType[][] remainPriority) {
        this.firstAndLastStat = firstAndLastStat;
        this.breakpointTarget = breakpointTarget;
        this.remainPriority = remainPriority;
        chooseBestStats();
        validate();
    }

    public void validate() {
        if (remainPriority.length < 2)
            throw new IllegalArgumentException("need some more stats to consider");
        if (Arrays.stream(remainPriority).flatMap(Arrays::stream).anyMatch(s -> s == firstAndLastStat))
            throw new IllegalArgumentException("breakpoint stat shouldn't be repeated as remaining");

        Set<StatType> seen = new HashSet<>();
        for (StatType[] rank : remainPriority) {
            if (rank.length == 0)
                throw new IllegalArgumentException("empty rank");
            for (StatType stat : rank) {
                if (!seen.add(stat)) {
                    throw new IllegalStateException("repeated priority for " + stat);
                }
            }
        }
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

            for (StatType[] rank : remainPriority) {
                long value = 0;
                for (StatType stat : rank) {
                    value += totals.get(stat);
                }
                result += value * multiply;
                multiply /= STEP;
            }
        } else {
            result += breakpointValue * multiply;
            multiply /= STEP;

            for (StatType[] rank : remainPriority) {
                long value = 0;
                for (StatType stat : rank) {
                    value += totals.get(stat);
                }
                result += value * multiply;
                multiply /= STEP;
            }
        }
        return result;
    }

    @Override
    public long calcRating(StatType queryStat, int value) {
        if (queryStat == firstAndLastStat) {
            if (value > breakpointTarget) {
                return ((breakpointTarget * INITIAL) + (value - breakpointTarget));
            } else {
                return (long) value * INITIAL;
            }
        } else {
            long multiply = INITIAL / STEP;
            for (StatType[] rank : remainPriority) {
                for (StatType stat : rank) {
                    if (stat == queryStat) {
                        return value * multiply;
                    }
                }
                multiply /= STEP;
            }
            return 0;
        }
    }
}
