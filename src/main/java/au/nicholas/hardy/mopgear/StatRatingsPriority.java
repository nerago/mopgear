package au.nicholas.hardy.mopgear;

import java.util.Arrays;

public class StatRatingsPriority implements StatRatings {
    private final StatType[] priority;
    private final static int DEFAULT_MULTIPLY = 4; // scale to similar rates as weighting

    public StatRatingsPriority(StatType[] priority) {
        this.priority = priority;
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
        long value = 0;
        long multiply = 1000;
        for (StatType stat : priority) {
            value += totals.get(stat) * multiply;
            multiply /= 10;
        }
        return value * DEFAULT_MULTIPLY;
    }
}
