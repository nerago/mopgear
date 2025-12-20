package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.EquipOptionsMap;
import au.nerago.mopgear.domain.FullItemSet;
import au.nerago.mopgear.domain.SolvableEquipOptionsMap;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.process.FallbackCappedSetBuilder;
import au.nerago.mopgear.process.Tweaker;
import au.nerago.mopgear.results.JobInput;
import au.nerago.mopgear.results.JobOutput;
import au.nerago.mopgear.util.BigStreamUtil;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;

public class Solver {
    private static final long MILLION = 1_000_000;
    private static final long DEFAULT_RANDOM_RUN_SIZE = 10 * MILLION;
    public static final long MAX_SKINNY_PHASED_ANY = 100 * MILLION;
    public static final long MAX_SKINNY_FULL_SEARCH = 10000;
    public static final long MAX_BASIC_FULL_SEARCH = MILLION / 50;

    public static JobOutput runJob(JobInput job) {
        ModelCombined model = job.model;
        SolvableEquipOptionsMap itemOptions = job.itemOptions;
        long runSizeMultiply = job.runSizeMultiply;
        StatBlock adjustment = job.adjustment;
        Instant startTime = job.startTime;
        BigInteger estimateFullCombos = BigStreamUtil.estimateSets(itemOptions);

        SolverCapPhased phased = null;
        BigInteger estimatePhase1Combos = BigInteger.ZERO;
        if (SolverCapPhased.supportedModel(model)) {
            phased = new SolverCapPhased(model, adjustment, job.printRecorder, runSizeMultiply);
            estimatePhase1Combos = phased.initAndCheckSizes(itemOptions);
            job.printf("COMBOS full=%,d skinny=%,d\n", estimateFullCombos, estimatePhase1Combos);
        } else {
            job.printf("COMBOS full=%,d no_skinny\n", estimateFullCombos);
        }

        JobOutput output = new JobOutput(job);
        if (job.forceRandom) {
            long runSize = DEFAULT_RANDOM_RUN_SIZE * runSizeMultiply;
            job.printf("SOLVE random %d FORCED\n", runSize);
            output.resultSet = SolverRandom.runSolver(model, itemOptions, adjustment, startTime, runSize, !job.singleThread, job.specialFilter);
        } else if (job.forceSkipIndex) {
            job.println("SOLVE skip index");
            output.resultSet = SolverIndexed.runSolverSkipping(model, itemOptions, adjustment, startTime, job.forcedRunSized, estimateFullCombos, job.specialFilter);
        } else if (job.forcePhased) {
            job.println("SOLVE phased top only FORCED");
            assert phased != null;
            phased = new SolverCapPhasedIndexed(model, adjustment, job.printRecorder, runSizeMultiply);
            phased.initAndCheckSizes(itemOptions);
            output.resultSet = phased.runSolver(!job.singleThread, job.specialFilter);
        } else if (estimateFullCombos.compareTo(BigInteger.valueOf(MAX_BASIC_FULL_SEARCH * runSizeMultiply)) < 0) {
            job.println("SOLVE full search");
            output.resultSet = SolverIndexed.runSolver(model, itemOptions, adjustment, estimateFullCombos.longValueExact(), job.specialFilter);
        } else if (phased != null && estimatePhase1Combos.compareTo(BigInteger.valueOf(MAX_SKINNY_FULL_SEARCH * runSizeMultiply)) < 0) {
            job.println("SOLVE phased full");
            output.resultSet = phased.runSolver(!job.singleThread, job.specialFilter);
        } else if (phased != null && estimatePhase1Combos.compareTo(BigInteger.valueOf(MAX_SKINNY_PHASED_ANY * runSizeMultiply)) < 0) {
            job.println("SOLVE phased top only");
            output.resultSet = phased.runSolver(!job.singleThread, job.specialFilter);
        } else {
            long runSize = DEFAULT_RANDOM_RUN_SIZE * runSizeMultiply;
            job.printf("SOLVE random %d\n", runSize);
            output.resultSet = SolverRandom.runSolver(model, itemOptions, adjustment, startTime, runSize, !job.singleThread, job.specialFilter);
        }

//        if (output.resultSet.isEmpty() && job.hackAllow) {
//            output.resultSet = FallbackCappedSetBuilder.fallbackLimits(model, itemOptions, adjustment, output);
//        }

        output.resultSet = output.resultSet.map(set -> Tweaker.tweak(set, model, itemOptions, job.specialFilter));

        output.resultSet.ifPresent(itemSet -> output.resultRating = model.calcRating(itemSet));
        return output;
    }

    public static Optional<FullItemSet> chooseEngineAndRun(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, StatBlock adjustment) {
        JobInput job = new JobInput();
        job.model = model;
        job.setItemOptions(itemOptions);
        job.startTime = startTime;
        job.adjustment = adjustment;
        JobOutput output = runJob(job);
        job.printRecorder.outputNow();
        return output.getFinalResultSet();
    }
}
