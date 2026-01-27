package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.SolvableEquipOptionsMap;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.process.Tweaker;
import au.nerago.mopgear.results.JobInput;
import au.nerago.mopgear.results.JobOutput;
import au.nerago.mopgear.util.BigStreamUtil;

import java.math.BigInteger;
import java.time.Instant;

import static au.nerago.mopgear.results.JobInput.SolveMethod.*;

public class Solver {
    private static final long DEFAULT_RANDOM_RUN_SIZE = 10_000_000;
    private static final long DEFAULT_INDEX_RUN_SIZE = 100_000;
    private static final long PHASED_COMBOS_GENERATE = 2_000_000;
    private static final long MAX_BASIC_FULL_SEARCH = 100_000;
    private static final long MAX_SKINNY_FULL_SEARCH = 100_000;
    private static final long MAX_SKINNY_PHASED_ANY = 10_000_000;
    private static final int TOP_HIT_COMBO_FILTER = 1000;

    public static JobOutput runJob(JobInput job) {
        ModelCombined model = job.model;
        SolvableEquipOptionsMap itemOptions = job.itemOptions;
        StatBlock adjustment = job.adjustment;
        Instant startTime = job.startTime;
        JobInput.SolveMethod method = job.forceMethod;
        BigInteger estimateFullCombos = BigStreamUtil.estimateSets(itemOptions);

        long runSizeMultiply = job.runSizeAdditionalMultiply;
        assert runSizeMultiply >= 1;
        switch (job.runSizeCategory) {
            case Medium -> runSizeMultiply *= 16;
            case Final -> runSizeMultiply *= 256;
        }

        SolverCapPhased phased = null;
        BigInteger estimatePhase1Combos = BigInteger.ZERO;
        if (SolverCapPhased.supportedModel(model)) {
            phased = new SolverCapPhased(model, adjustment, job.printRecorder);
            estimatePhase1Combos = phased.initAndCheckSizes(itemOptions);
            job.printf("COMBOS full=%,d skinny=%,d\n", estimateFullCombos, estimatePhase1Combos);
        } else {
            job.printf("COMBOS full=%,d no_skinny\n", estimateFullCombos);
        }

        if (method == null) {
            if (estimateFullCombos.compareTo(BigInteger.valueOf(MAX_BASIC_FULL_SEARCH * runSizeMultiply)) < 0) {
                method = Full;
            } else if (phased != null && job.phasedAcceptable) {
                if (estimatePhase1Combos.compareTo(BigInteger.valueOf(MAX_SKINNY_FULL_SEARCH * runSizeMultiply)) < 0) {
                    method = PhasedFull;
                } else if (estimatePhase1Combos.compareTo(BigInteger.valueOf(MAX_SKINNY_PHASED_ANY * runSizeMultiply)) < 0) {
                    method = PhasedTop;
                } else {
                    method = PhasedIndexedTop;
                }
            } else {
                method = SkipIndex;
            }
        }

        JobOutput output = new JobOutput(job);
        switch (method) {
            case SkipIndex -> {
                long runSize = DEFAULT_INDEX_RUN_SIZE * runSizeMultiply;
                job.printf("SOLVE skip index %d\n", runSize);
                output.resultSet = SolverIndexed.runSolverSkipping(model, itemOptions, adjustment, startTime, runSize, estimateFullCombos, job.specialFilter);
            }
            case Random -> {
                long runSize = DEFAULT_RANDOM_RUN_SIZE * runSizeMultiply;
                job.printf("SOLVE random %d (OBSOLETE)\n", runSize);
                output.resultSet = SolverRandom.runSolver(model, itemOptions, adjustment, startTime, runSize, !job.singleThread, job.specialFilter);
            }
            case Full -> {
                job.println("SOLVE full search");
                output.resultSet = SolverIndexed.runFullScan(model, itemOptions, adjustment, estimateFullCombos.longValueExact(), job.specialFilter);
            }
            case PhasedFull -> {
                job.println("SOLVE phased full");
                assert phased != null;
                output.resultSet = phased.runSolver(!job.singleThread, job.specialFilter, false, false, null, null, startTime);
            }
            case PhasedTop -> {
                assert phased != null;
                int topCombos = Math.toIntExact(TOP_HIT_COMBO_FILTER * runSizeMultiply);
                job.printf("SOLVE phased top only %d\n", topCombos);
                output.resultSet = phased.runSolver(!job.singleThread, job.specialFilter, false, true, null, topCombos, startTime);
            }
            case PhasedIndexedTop -> {
                assert phased != null;
                int targetCombos = Math.toIntExact(PHASED_COMBOS_GENERATE * runSizeMultiply);
                int topCombos = Math.toIntExact(TOP_HIT_COMBO_FILTER * job.runSizeAdditionalMultiply);
                job.printf("SOLVE phased top only %d -> %d\n", targetCombos, topCombos);
                output.resultSet = phased.runSolver(!job.singleThread, job.specialFilter, true, true, targetCombos, topCombos, startTime);
            }
        }

//        if (output.resultSet.isEmpty() && job.hackAllow) {
//            output.resultSet = FallbackCappedSetBuilder.fallbackLimits(model, itemOptions, adjustment, output);
//        }

        output.resultSet = output.resultSet.map(set -> Tweaker.tweak(set, model, itemOptions, job.specialFilter));

        output.resultSet.ifPresent(itemSet -> output.resultRating = model.calcRating(itemSet));
        return output;
    }
}
