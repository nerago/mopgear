package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.SocketType;
import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.domain.StatType;
import au.nicholas.hardy.mopgear.util.BestHolder;

import java.util.EnumMap;

import static au.nicholas.hardy.mopgear.domain.StatType.*;

public abstract class StatRatings {
    private EnumMap<SocketType, StatBlock> standardGems;
    private StatType bestNonHit;

    public abstract long calcRating(StatBlock totals);

    public abstract long calcRating(StatType stat, int value);

    protected void chooseGems() {
        standardGems = new EnumMap<>(SocketType.class);
        GemData.chooseGem(standardGems, SocketType.Red, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.Blue, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.Yellow, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.General, this::calcRating);

        BestHolder<StatType> bestStat = new BestHolder<>(null, 0);
        for (StatType stat : StatType.values()) {
            if (stat != Primary && stat != Hit && stat != Expertise) {
                bestStat.add(stat, calcRating(stat, 1));
            }
        }
        bestNonHit = bestStat.get();
    }

    public StatBlock gemChoice(SocketType socket) {
        return standardGems.get(socket);
    }

    public StatType bestNonHit() {
        return bestNonHit;
    }
}
