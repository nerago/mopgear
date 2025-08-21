package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.SocketType;
import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.domain.StatType;

import java.util.Arrays;
import java.util.EnumMap;

public class StatRatingsPriority implements StatRatings {
    private final StatType[] priority;
    private final static int DEFAULT_MULTIPLY = 4; // scale to similar rates as weighting
    private EnumMap<SocketType,StatBlock> standardGems;


    public StatRatingsPriority(StatType[] priority) {
        this.priority = priority;
        chooseGems();
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

    private void chooseGems() {
        standardGems = new EnumMap<>(SocketType.class);
        GemData.chooseGem(standardGems, SocketType.Red, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.Blue, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.Yellow, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.General, this::calcRating);
    }

    @Override
    public StatBlock gemChoice(SocketType socket) {
        return standardGems.get(socket);
    }
}
