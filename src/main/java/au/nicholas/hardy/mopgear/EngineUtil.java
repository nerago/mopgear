package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.EquipOptionsMap;
import au.nicholas.hardy.mopgear.domain.ItemSet;
import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.results.JobInfo;
import au.nicholas.hardy.mopgear.util.BestHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class EngineUtil {
    public static JobInfo runJob(JobInfo job) {
        ModelCombined model = job.model;
        EquipOptionsMap itemOptions = job.itemOptions;
        Long runSize = job.runSize;
        StatBlock adjustment = job.adjustment;
        Instant startTime = job.startTime;

        long estimate = ItemUtil.estimateSets(itemOptions);
        Optional<ItemSet> proposed;
        if (runSize != null && estimate > runSize) {
            job.printf("COMBINATIONS estimate=%,d RANDOM SAMPLE %,d\n", estimate, runSize);
            proposed = EngineRandom.runSolver(model, itemOptions, adjustment, startTime, runSize);
        } else {
            job.printf("COMBINATIONS estimate=%,d FULL RUN\n", estimate);
            proposed = EngineStream.runSolver(model, itemOptions, adjustment, startTime, estimate);
        }

        if (proposed.isEmpty()) {
            proposed = fallbackLimits(model, itemOptions, adjustment, job);
        }

        job.resultSet = proposed.map(itemSet -> Tweaker.tweak(itemSet, model, itemOptions));
        return job;
    }

    public static Optional<ItemSet> chooseEngineAndRun(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, Long runSize, StatBlock adjustment) {
        JobInfo job = new JobInfo();
        job.config(model, itemOptions, startTime, runSize, adjustment);
        runJob(job);
        job.outputNow();
        return job.resultSet;
    }

    public static JobInfo chooseEngineAndRunAsJob(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, Long runSize, StatBlock adjustment) {
        JobInfo job = new JobInfo();
        job.config(model, itemOptions, startTime, runSize, adjustment);
        return runJob(job);
    }

    private static Optional<ItemSet> fallbackLimits(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, JobInfo result) {
        List<ItemSet> proposedList = FindStatRange.setsAtLimits(itemOptions, adjustment);
        BestHolder<ItemSet> bestHolder = new BestHolder<>(null, 0);
        result.println("FALLBACK SET CHECKING");
        for (ItemSet set : proposedList) {
            if (model.statRequirements().filter(set)) {
                long rating = model.calcRating(set);
                bestHolder.add(set, rating);
            }
        }
        if (bestHolder.get() == null) {
            result.println("FALLBACK SET FAILED WITHIN CAPS");
            for (ItemSet set : proposedList) {
                long rating = model.calcRating(set);
                bestHolder.add(set, rating);
            }
        }
        if (bestHolder.get() != null)
            result.println("FALLBACK SET FOUND");
        return Optional.ofNullable(bestHolder.get());
    }
}
