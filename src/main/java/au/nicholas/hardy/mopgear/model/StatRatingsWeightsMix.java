package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.domain.StatType;

import java.io.IOException;

public class StatRatingsWeightsMix extends StatRatings {
    private final StatRatings weightA;
    private final StatRatings weightB;
    private final int multiplyA;
    private final int multiplyB;

    public StatRatingsWeightsMix(StatRatings weightA, int multiplyA, StatRatings weightB, int multiplyB) throws IOException {
        this.weightA = weightA;
        this.weightB = weightB;
        this.multiplyA = multiplyA;
        this.multiplyB = multiplyB;
        chooseGems();
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

    public long calcRating(StatType stat, int value) {
        long total = 0;
        if (multiplyA > 0)
            total += weightA.calcRating(stat, value) * multiplyA;
        if (multiplyB > 0)
            total += weightB.calcRating(stat, value) * multiplyB;
        return total;
    }

}
