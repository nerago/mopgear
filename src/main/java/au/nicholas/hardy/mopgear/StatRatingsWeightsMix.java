package au.nicholas.hardy.mopgear;

import java.io.IOException;

public class StatRatingsWeightsMix implements StatRatings {
    private final StatRatings weightA;
    private final StatRatings weightB;
    private final int multiplyA;
    private final int multiplyB;
    private final StatBlock standardGem;

    public StatRatingsWeightsMix(StatRatings weightA, int multiplyA, StatRatings weightB, int multiplyB, int defaultGem) throws IOException {
        this.weightA = weightA;
        this.weightB = weightB;
        this.multiplyA = multiplyA;
        this.multiplyB = multiplyB;
        this.standardGem = GemData.known.get(defaultGem);
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
    public StatBlock standardGem() {
        return standardGem;
    }
}
