package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.SocketType;
import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.domain.StatType;

import java.util.EnumMap;

public class StatRatingsWeightsMix extends StatRatings {
    private final StatRatings weightA;
    private final StatRatings weightB;
    private final int multiplyA;
    private final int multiplyB;

    public StatRatingsWeightsMix(StatRatings weightA, int multiplyA, StatRatings weightB, int multiplyB) {
        this.weightA = weightA;
        this.weightB = weightB;
        this.multiplyA = multiplyA;
        this.multiplyB = multiplyB;
        chooseGems();
        chooseBestStats();
    }

    public StatRatingsWeightsMix(StatRatings weightA, int multiplyA, StatRatings weightB, int multiplyB, EnumMap<SocketType, StatBlock> standardGems) {
        this.weightA = weightA;
        this.weightB = weightB;
        this.multiplyA = multiplyA;
        this.multiplyB = multiplyB;
        this.standardGems = standardGems;
        chooseBestStats();
    }

    @Override
    public long calcRating(StatBlock value) {
        long total = 0;
        if (multiplyA > 0)
            total += weightA.calcRating(value) * multiplyA;
        if (multiplyB > 0)
            total += weightB.calcRating(value) * multiplyB;
        return total;
    }

    @Override
    public long calcRating(StatBlock part1, StatBlock part2) {
        long total = 0;
        if (multiplyA > 0)
            total += weightA.calcRating(part1, part2) * multiplyA;
        if (multiplyB > 0)
            total += weightB.calcRating(part1, part2) * multiplyB;
        return total;
    }

    public long calcRating(StatType stat, int value) {
        long total = 0;
        if (multiplyA > 0)
            total += weightA.calcRating(stat, value) * multiplyA;
        if (multiplyB > 0)
            total += weightB.calcRating(stat, value) * multiplyB;
        return total;
    }
}
