package au.nicholas.hardy.mopgear;

import java.util.Arrays;

public class StatRatingsPriority implements StatRatings {
    private final StatType[] priority;

//    public ModelPriority() {
//        priority = new StatType[]{StatType.Haste, StatType.Mastery, StatType.Crit};
//    }

    public StatRatingsPriority(StatType[] priority) {
        this.priority = priority;
    }

    public void validate() {
        if (priority.length > 3)
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
        for (StatType stat : priority) {
            value = (value << 16) | totals.get(stat);
        }
        return value;
    }
}
