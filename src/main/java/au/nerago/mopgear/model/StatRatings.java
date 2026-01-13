package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.SocketType;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.util.LowHighHolder;

import java.util.EnumMap;

import static au.nerago.mopgear.domain.StatType.*;

public abstract class StatRatings {
    protected EnumMap<SocketType, StatBlock> standardGems;
    protected StatType bestNonHit;
    protected StatType worstNonHit;

    public EnumMap<SocketType, StatBlock> getStandardGems() {
        return standardGems;
    }

    public abstract long calcRating(StatBlock totals);

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
        LowHighHolder<StatType> bestStat = new LowHighHolder<>();
        for (StatType stat : StatType.values()) {
            if (stat != Primary && stat != Hit && stat != Expertise && stat != Stam) { // TODO should we exclude stam this way or still consider for gems another way
                bestStat.add(stat, calcRating(stat, 1));
            }
        }
        bestNonHit = bestStat.getHigh();
        // TODO only include non-zero for worst?
        worstNonHit = bestStat.getLow();
    }

    public StatBlock gemChoice(SocketType socket) {
        return standardGems.get(socket);
    }

    public StatType bestNonHit() {
        return bestNonHit;
    }

    public StatType worstNonHit() {
        return worstNonHit;
    }
}
