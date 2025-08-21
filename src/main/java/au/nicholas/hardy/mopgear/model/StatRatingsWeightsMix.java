package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.SocketType;
import au.nicholas.hardy.mopgear.domain.StatBlock;

import java.io.IOException;
import java.util.EnumMap;

public class StatRatingsWeightsMix implements StatRatings {
    private final StatRatings weightA;
    private final StatRatings weightB;
    private final int multiplyA;
    private final int multiplyB;
    private EnumMap<SocketType, StatBlock> standardGems;

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
