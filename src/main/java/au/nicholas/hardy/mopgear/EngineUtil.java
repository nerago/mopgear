package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.EquipOptionsMap;
import au.nicholas.hardy.mopgear.domain.ItemSet;
import au.nicholas.hardy.mopgear.model.ModelCombined;

import java.time.Instant;
import java.util.Optional;

public class EngineUtil {
    public static Optional<ItemSet> chooseEngineAndRun(ModelCombined model, EquipOptionsMap reforgedItems, Instant startTime, Long runSize, ItemSet otherSet) {
        long estimate = ItemUtil.estimateSets(reforgedItems);

        if (runSize != null && estimate > runSize) {
            if (startTime != null)
                System.out.printf("COMBINATIONS estimate=%,d RANDOM SAMPLE %,d\n", estimate, runSize);
            Optional<ItemSet> proposed = EngineRandom.runSolver(model, reforgedItems, startTime, otherSet, runSize);
            return proposed.map(itemSet -> Tweaker.tweak(itemSet, model, reforgedItems));
        } else {
            if (startTime != null)
                System.out.printf("COMBINATIONS estimate=%,d FULL RUN\n", estimate);
            return EngineStream.runSolver(model, reforgedItems, startTime, otherSet, estimate);
        }
    }
}
