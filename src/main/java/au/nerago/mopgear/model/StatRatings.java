package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.util.LowHighHolder;
import au.nerago.mopgear.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static au.nerago.mopgear.domain.StatType.*;

public abstract class StatRatings {
    protected StatType bestNonHit;
    protected StatType worstNonHit;

    public abstract long calcRating(StatBlock totals);

    public abstract long calcRating(StatType stat, int value);

    protected void chooseBestStats() {
        LowHighHolder<StatType> bestStat = new LowHighHolder<>();
        for (StatType stat : StatType.values()) {
            if (stat != Primary && stat != Hit && stat != Expertise && stat != Stam) { // TODO should we exclude stam this way or still consider for gems another way
                long rating = calcRating(stat, 1);
                if (rating != 0) {
                    bestStat.add(stat, rating);
                }
            }
        }
        bestNonHit = bestStat.getHigh();
        worstNonHit = bestStat.getLow();
    }

    public StatType bestNonHit() {
        return bestNonHit;
    }

    public List<StatType> statOrder() {
        return Arrays.stream(values()).map(statType -> Tuple.create(statType, calcRating(statType, 1)))
                .sorted(Comparator.comparing(Tuple.Tuple2::b))
                .map(Tuple.Tuple2::a)
                .toList();
    }

    public StatType worstNonHit() {
        return worstNonHit;
    }
}
