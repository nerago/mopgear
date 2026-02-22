package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRatings;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.results.JobOutput;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class FallbackCappedSetReport {
    public static String reportIfSetShouldExist(ModelCombined model, SolvableEquipOptionsMap itemOptions, StatBlock adjustment, JobOutput job) {
        job.println("NO SET FOUND USING NORMAL PROCESS");

        @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements =
                model.statRequirements() instanceof StatRequirements.StatRequirementsWithHitExpertise
                        ? (StatRequirements.StatRequirementsWithHitExpertise) model.statRequirements() : null;

        List<SolvableItemSet> proposedList = setsAtLimits(itemOptions, adjustment, requirements);
        if (areAnyOfTheseAcceptable(model, job, proposedList)) {
            job.println("FALLBACK SET FOUND USING MIN/MAX ONLY");
            return "FALLBACK AVAILABLE";
        } else if (requirements != null) {
            return discoverCommonProblems(job, proposedList, model, requirements, model.statRatings());
        } else {
            return "UNKNOWN";
        }
    }

    private static List<SolvableItemSet> setsAtLimits(SolvableEquipOptionsMap itemOptions, StatBlock adjustment, @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements) {
        List<SolvableItemSet> setList = new ArrayList<>();
        for (StatType statType : StatType.values()) {
            setsAtLimitsOfStat(itemOptions, adjustment, statType, setList, requirements);
        }
        return setList;
    }

    private static void setsAtLimitsOfStat(SolvableEquipOptionsMap itemOptions, StatBlock adjustment, StatType statType, List<SolvableItemSet> setList, @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements) {
        Holder<SolvableItemSet> lowSet = new Holder<>(), highSet = new Holder<>();
        itemOptions.forEachPair((slot, array) -> {
            LowHighHolder<SolvableItem> statRange = StatUtil.findMinMax(requirements, array, statType);
            if (lowSet.value == null) {
                lowSet.value = SolvableItemSet.singleItem(slot, statRange.getLow(), adjustment);
                highSet.value = SolvableItemSet.singleItem(slot, statRange.getHigh(), adjustment);
            } else {
                lowSet.value = lowSet.value.copyWithAddedItem(slot, statRange.getLow());
                highSet.value = highSet.value.copyWithAddedItem(slot, statRange.getHigh());
            }
        });
        setList.add(lowSet.value);
        setList.add(highSet.value);
    }

    enum ProblemType {
        HitLow, HitHigh, ExpLow, ExpHigh
    }

    private static ArrayList<ProblemType> discoverSetProblems(SolvableEquipMap setItems, PrintRecorder print, StatRatings ratings, @NotNull StatRequirements.StatRequirementsWithHitExpertise requirements) {
        ArrayList<ProblemType> problemList = new ArrayList<>();

        StatBlock itemTotal = StatBlock.sumForCaps(setItems);

        int minHit = requirements.getMinimumHit(), maxHit = requirements.getMaximumHit();
        int effectiveHit = requirements.effectiveHit(itemTotal);
        if (minHit != 0 && effectiveHit < minHit) {
            problemList.add(ProblemType.HitLow);
        } else if (minHit != 0 && effectiveHit > maxHit) {
            problemList.add(ProblemType.HitHigh);
        }

        int minExp = requirements.getMinimumExpertise(), maxExp = requirements.getMaximumExpertise();
        if (minExp != 0 && itemTotal.expertise() < minExp) {
            problemList.add(ProblemType.ExpLow);
        } else if (minExp != 0 && maxExp != Integer.MAX_VALUE && itemTotal.expertise() > maxExp) {
            problemList.add(ProblemType.ExpHigh);
        }

        return problemList;
    }

    private static boolean areAnyOfTheseAcceptable(ModelCombined model, JobOutput job, List<SolvableItemSet> proposedList) {
        for (SolvableItemSet set : proposedList) {
            if (model.filterOneSet(set)) {
                return true;
            }
        }
        return false;
    }

    private static String discoverCommonProblems(JobOutput job, List<SolvableItemSet> proposedList, ModelCombined model, @NotNull StatRequirements.StatRequirementsWithHitExpertise requirements, StatRatings ratings) {
        HashSet<ProblemType> commonProblems = new HashSet<>();

        for (SolvableItemSet set : proposedList) {
            PrintRecorder print = new PrintRecorder();

            ArrayList<ProblemType> setProblems = discoverSetProblems(set.items(), print, ratings, requirements);
            commonProblems.addAll(setProblems);
        }

        if (commonProblems.isEmpty()) {
            throw new RuntimeException("NO SOLUTION FOR UNKNOWN REASON");
        } else {
            String problemSummary = commonProblems.stream().map(Objects::toString).collect(Collectors.joining(", "));
            job.println("NO SOLUTION PROBLEMS = " + problemSummary);
            return problemSummary;
        }
    }
}
