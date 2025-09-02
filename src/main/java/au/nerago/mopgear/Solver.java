package au.nerago.mopgear;

import au.nerago.mopgear.domain.EquipOptionsMap;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.JobInfo;

import java.time.Instant;
import java.util.Optional;

public class Solver {

    public static final int THRESHOLD_PHASED = 7500;

    public static JobInfo runJob(JobInfo job) {
        ModelCombined model = job.model;
        EquipOptionsMap itemOptions = job.itemOptions;
        Long runSize = job.runSize;
        StatBlock adjustment = job.adjustment;
        Instant startTime = job.startTime;

        long estimate = ItemUtil.estimateSets(itemOptions);
        Optional<ItemSet> proposed;
        if (runSize != null && estimate / THRESHOLD_PHASED > runSize) {
            job.printf("COMBINATIONS estimate=%,d RANDOM SAMPLE %,d\n", estimate, runSize);
            if (job.singleThread)
                proposed = SolverRandom.runSolverSingleThread(model, itemOptions, adjustment, runSize);
            else
                proposed = SolverRandom.runSolver(model, itemOptions, adjustment, startTime, runSize);
        } else {
            job.printf("COMBINATIONS estimate=%,d FULL RUN (phased)\n", estimate);
            if (job.singleThread)
                proposed = new SolverCapPhased(model, adjustment).runSolverSingleThread(itemOptions);
            else
                proposed = new SolverCapPhased(model, adjustment).runSolver(itemOptions);
        }

        if (proposed.isEmpty() && job.hackAllow) {
            proposed = FindStatRange.fallbackLimits(model, itemOptions, adjustment, job);
        }

        job.resultSet = proposed.map(itemSet -> Tweaker.tweak(itemSet, model, itemOptions));
        return job;
    }

    public static Optional<ItemSet> chooseEngineAndRun(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, Long runSize, StatBlock adjustment) {
        JobInfo job = new JobInfo();
        job.config(model, itemOptions, startTime, runSize, adjustment);
        runJob(job);
        job.printRecorder.outputNow();
        return job.resultSet;
    }

    public static JobInfo chooseEngineAndRunAsJob(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, Long runSize, StatBlock adjustment) {
        JobInfo job = new JobInfo();
        job.config(model, itemOptions, startTime, runSize, adjustment);
        return runJob(job);
    }
}
