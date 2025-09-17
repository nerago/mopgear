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
    public static final long MAX_SKINNY_PHASED_ANY = 100 * MILLION;
    public static final long MAX_SKINNY_FULL_SEARCH = 10000;
    public static final long MAX_BASIC_FULL_SEARCH = MILLION;

    public static JobInfo runJob(JobInfo job) {
        ModelCombined model = job.model;
        EquipOptionsMap itemOptions = job.itemOptions;
        long runSizeMultiply = job.runSizeMultiply;
        StatBlock adjustment = job.adjustment;
        Instant startTime = job.startTime;

        SolverCapPhased phased = new SolverCapPhased(model, adjustment, job.printRecorder);
        long estimatePhase1Combos = phased.initAndCheckSizes(itemOptions);
        long estimateFullCombos = ItemUtil.estimateSets(itemOptions);

        job.printf("COMBOS full=%,d skinny=%,d\n", estimateFullCombos, estimatePhase1Combos);

        Optional<ItemSet> proposed;
        if (job.forceRandom) {
            long runSize = DEFAULT_RANDOM_RUN_SIZE * runSizeMultiply;
            job.printf("SOLVE random %d FORCED\n", runSize);
            proposed = SolverRandom.runSolver(model, itemOptions, adjustment, startTime, runSize, !job.singleThread, job.specialFilter);
        } else if (estimateFullCombos < MAX_BASIC_FULL_SEARCH * runSizeMultiply) {
            job.println("SOLVE full search");
            proposed = SolverIndexed.runSolver(model, itemOptions, adjustment, startTime, estimateFullCombos, job.specialFilter);
        } else if (estimatePhase1Combos < MAX_SKINNY_FULL_SEARCH * runSizeMultiply) {
            job.println("SOLVE phased full");
            proposed = phased.runSolver(!job.singleThread, job.specialFilter, false);
        } else if (estimatePhase1Combos < MAX_SKINNY_PHASED_ANY * runSizeMultiply) {
            job.println("SOLVE phased top only");
            proposed = phased.runSolver(!job.singleThread, job.specialFilter, false);
        } else {
            long runSize = DEFAULT_RANDOM_RUN_SIZE * runSizeMultiply;
            job.printf("SOLVE random %d\n", runSize);
            proposed = SolverRandom.runSolver(model, itemOptions, adjustment, startTime, runSize, !job.singleThread, job.specialFilter);
        }

        if (proposed.isEmpty() && job.hackAllow) {
            proposed = FindStatRange.fallbackLimits(model, itemOptions, adjustment, job);
        }

        job.resultSet = proposed.map(itemSet -> Tweaker.tweak(itemSet, model, itemOptions));
        return job;
    }

    public static Optional<ItemSet> chooseEngineAndRun(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, StatBlock adjustment) {
        JobInfo job = new JobInfo();
        job.config(model, itemOptions, startTime, adjustment);
        runJob(job);
        job.printRecorder.outputNow();
        return job.resultSet;
    }
}
