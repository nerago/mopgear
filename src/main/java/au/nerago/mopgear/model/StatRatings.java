package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.SocketType;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.util.BestHolder;

import java.util.EnumMap;

import static au.nerago.mopgear.domain.StatType.*;

public abstract class StatRatings {
    protected EnumMap<SocketType, StatBlock> standardGems;
    protected StatType bestNonHit;

    public abstract long calcRating(StatBlock totals);

    public abstract long calcRating(StatBlock partA, StatBlock partB);

    public abstract long calcRating(StatType stat, int value);

    protected void chooseGems() {
        standardGems = new EnumMap<>(SocketType.class);
        GemData.chooseGem(standardGems, SocketType.Red, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.Blue, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.Yellow, this::calcRating);
        GemData.chooseGem(standardGems, SocketType.General, this::calcRating);
        standardGems.put(SocketType.Meta, StatBlock.of(StatType.Primary, 216));
    }

    protected void chooseBestStats() {
        BestHolder<StatType> bestStat = new BestHolder<>();
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
