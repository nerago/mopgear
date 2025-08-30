package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.domain.StatType;

import java.util.Arrays;

public class StatRatingsPriority extends StatRatings {
    private final StatType[] priority;
    private final static int DEFAULT_MULTIPLY = 4; // scale to similar rates as weighting

    public StatRatingsPriority(StatType[] priority) {
        this.priority = priority;
        chooseGems();
        chooseBestStats();
        validate();
    }

    public void validate() {
        if (priority.length != 4)
            throw new IllegalStateException("can't use current number ranking");
        if (Arrays.stream(priority).distinct().count() != priority.length)
            throw new IllegalStateException("priorities not distinct");
    }


    /***
     * @see #priority
     * Secondary.Haste, Secondary.Mastery, Secondary.Crit
     * Maxes on armor around 1349, weapon 1021
     */
    @Override
    public long calcRating(StatBlock totals) {
        long result = 0;
        long multiply = 1000;
        for (StatType stat : priority) {
            result += totals.get(stat) * multiply;
            multiply /= 10;
        }
        return result * DEFAULT_MULTIPLY;
    }

    @Override
    public long calcRating(StatBlock partA, StatBlock partB) {
        long result = 0;
        long multiply = 1000;
        for (StatType stat : priority) {
            result += (partA.get(stat) + partB.get(stat)) * multiply;
            multiply /= 10;
        }
        return result * DEFAULT_MULTIPLY;
    }

    @Override
    public long calcRating(StatType queryStat, int value) {
        long multiply = 1000;
        for (StatType stat : priority) {
            if (stat == queryStat) {
                return value * multiply * DEFAULT_MULTIPLY;
            }
            multiply /= 10;
        }
        return 0;
    }
}
