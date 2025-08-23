package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.EquipOptionsMap;
import au.nicholas.hardy.mopgear.domain.ItemSet;
import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.BestHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class EngineUtil {
    public static Optional<ItemSet> chooseEngineAndRun(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, Long runSize, ItemSet otherSet, StatBlock adjustment) {
        boolean detailedOutput = startTime != null;
        long estimate = ItemUtil.estimateSets(itemOptions);
        Optional<ItemSet> proposed;
        if (runSize != null && estimate > runSize) {
            if (detailedOutput)
                System.out.printf("COMBINATIONS estimate=%,d RANDOM SAMPLE %,d\n", estimate, runSize);
            proposed = EngineRandom.runSolver(model, itemOptions, adjustment, startTime, otherSet, runSize);
        } else {
            if (detailedOutput)
                System.out.printf("COMBINATIONS estimate=%,d FULL RUN\n", estimate);
            proposed = EngineStream.runSolver(model, itemOptions, adjustment, startTime, otherSet, estimate);
        }
        if (proposed.isEmpty()) {
            proposed = fallbackLimits(model, itemOptions, adjustment, otherSet);
        }
        return proposed.map(itemSet -> Tweaker.tweak(itemSet, model, itemOptions));
    }

    private static Optional<ItemSet> fallbackLimits(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, ItemSet otherSet) {
        List<ItemSet> proposedList = FindStatRange.setsAtLimits(itemOptions, adjustment, otherSet);
        BestHolder<ItemSet> bestHolder = new BestHolder<>(null, 0);
        System.out.println("FALLBACK SET CHECKING");
        for (ItemSet set : proposedList) {
            if (model.statRequirements().filter(set)) {
                long rating = model.calcRating(set);
                bestHolder.add(set, rating);
            }
        }
        if (bestHolder.get() == null) {
            System.out.println("FALLBACK SET FAILED WITHIN CAPS");
            for (ItemSet set : proposedList) {
                long rating = model.calcRating(set);
                bestHolder.add(set, rating);
            }
        }
        if (bestHolder.get() != null)
            System.out.println("FALLBACK SET FOUND");
        return Optional.ofNullable(bestHolder.get());
    }
}
