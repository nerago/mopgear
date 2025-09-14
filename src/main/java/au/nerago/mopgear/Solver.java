package au.nerago.mopgear;

import au.nerago.mopgear.domain.EquipOptionsMap;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.JobInfo;

import java.time.Instant;
import java.util.Optional;

public class Solver {
    private static final long MILLION = 1_000_000;
    private static final long DEFAULT_RANDOM_RUN_SIZE = 10 * MILLION;
    public static final long MAX_SKINNY_PHASED_ANY = 1000 * MILLION;
    public static final long MAX_SKINNY_FULL_SEARCH = 1000;
    public static final long MAX_BASIC_FULL_SEARCH = MILLION;

    public static JobInfo runJob(JobInfo job) {
        ModelCombined model = job.model;
        EquipOptionsMap itemOptions = job.itemOptions;
        Long runSize = job.runSize;
        StatBlock adjustment = job.adjustment;
        Instant startTime = job.startTime;

        if (runSize == null)
            runSize = DEFAULT_RANDOM_RUN_SIZE;

        SolverCapPhased phased = new SolverCapPhased(model, adjustment, job.printRecorder);
        long estimatePhase1Combos = phased.initAndCheckSizes(itemOptions);
        long estimateFullCombos = ItemUtil.estimateSets(itemOptions);

        job.printf("COMBOS full=%,d skinny=%,d\n", estimateFullCombos, estimatePhase1Combos);

        Optional<ItemSet> proposed;
        if (job.forceRandom) {
            job.printf("SOLVE random %d FORCED\n", runSize);
            proposed = SolverRandom.runSolver(model, itemOptions, adjustment, startTime, runSize, !job.singleThread);
        } else if (estimateFullCombos < MAX_BASIC_FULL_SEARCH) {
            job.println("SOLVE full search");
            proposed = SolverIndexed.runSolver(model, itemOptions, adjustment, startTime, estimateFullCombos);
        } else if (estimatePhase1Combos < MAX_SKINNY_FULL_SEARCH) {
            job.println("SOLVE phased full");
            proposed = phased.runSolver(!job.singleThread, false);
        } else if (estimatePhase1Combos < MAX_SKINNY_PHASED_ANY) {
            job.println("SOLVE phased top only");
            proposed = phased.runSolver(!job.singleThread, false);
        } else {
            job.printf("SOLVE random %d\n", runSize);
            proposed = SolverRandom.runSolver(model, itemOptions, adjustment, startTime, runSize, !job.singleThread);
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
