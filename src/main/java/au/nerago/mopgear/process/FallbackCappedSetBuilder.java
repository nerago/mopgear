package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRatings;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.results.JobOutput;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.Holder;
import au.nerago.mopgear.util.LowHighHolder;
import au.nerago.mopgear.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FallbackCappedSetBuilder {
    public static Optional<SolvableItemSet> fallbackLimits(ModelCombined model, SolvableEquipOptionsMap itemOptions, StatBlock adjustment, JobOutput job) {
        job.println("NO SET FOUND USING NORMAL PROCESS");

        @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements =
                model.statRequirements() instanceof StatRequirements.StatRequirementsWithHitExpertise
                        ? (StatRequirements.StatRequirementsWithHitExpertise) model.statRequirements() : null;

        List<SolvableItemSet> proposedList = setsAtLimits(itemOptions, adjustment, requirements);
        Optional<SolvableItemSet> result = fallbackSimpleLimits(model, job, proposedList);
        if (result.isEmpty() && requirements != null) {
            // NOTE only works for some StatRequirements
            result = fallbackLimitsWithAdjustment(job, proposedList, model, requirements, model.statRatings());
        }
        return result;
    }

    private static List<SolvableItemSet> setsAtLimits(SolvableEquipOptionsMap itemOptions, StatBlock adjustment, @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements) {
        List<SolvableItemSet> setList = new ArrayList<>();
        for (StatType statType : StatType.values()) {
            findSets(itemOptions, adjustment, statType, setList, requirements);
        }
        return setList;
    }

    @SuppressWarnings("ConditionCoveredByFurtherCondition")
    private static SolvableItemSet adjustForCapsFinalSet(SolvableEquipMap setItems, PrintRecorder print, StatRatings ratings, @NotNull StatRequirements.StatRequirementsWithHitExpertise requirements) {
        StatBlock itemTotal = StatBlock.sumForCaps(setItems);
        StatBlock adjust = StatBlock.empty;
        StatType takeStat = ratings.bestNonHit(), giveStat = ratings.worstNonHit();

        int minHit = requirements.getMinimumHit(), maxHit = requirements.getMaximumHit();
        int effectiveHit = requirements.effectiveHit(itemTotal);
        if (minHit != 0 && effectiveHit < minHit) {
            int need = minHit - effectiveHit;
            print.printf("ADJUST Hit Low %d NEED %d STEALING %d %s\n", effectiveHit, minHit, need, takeStat);
            adjust = adjust.withChange(StatType.Hit, need, takeStat, -need);
        } else if (minHit != 0 && maxHit != Integer.MAX_VALUE && effectiveHit > maxHit) {
            int excess = effectiveHit - maxHit + 1;
            print.printf("ADJUST Hit High %d LIMIT %d GIFTING %d %s\n", effectiveHit, maxHit, excess, giveStat);
            adjust = adjust.withChange(StatType.Hit, -excess, giveStat, excess);
        }

        int minExp = requirements.getMinimumExpertise(), maxExp = requirements.getMaximumExpertise();
        if (minExp != 0 && itemTotal.expertise() < minExp) {
            int need = minExp - itemTotal.expertise();
            print.printf("ADJUST Expertise Low %d NEED %d STEALING %d %s\n", itemTotal.expertise(), minExp, need, takeStat);
            adjust = adjust.withChange(StatType.Expertise, need, takeStat, -need);
        } else if (minExp != 0 && maxExp != Integer.MAX_VALUE && itemTotal.expertise() > maxExp) {
            int excess = itemTotal.expertise() - maxExp;
            print.printf("ADJUST Expertise High %d LIMIT %d GIFTING %d %s\n", itemTotal.expertise(), maxExp, excess, giveStat);
            adjust = adjust.withChange(StatType.Expertise, -excess, giveStat, excess);
        }

        if (adjust == StatBlock.empty) {
            print.printf("ERROR expected to need adjust, none needed");
            //throw new IllegalStateException("expected to need adjust");
        }

        return SolvableItemSet.manyItems(setItems, adjust);
    }

    private static void findSets(SolvableEquipOptionsMap itemOptions, StatBlock adjustment, StatType statType, List<SolvableItemSet> setList, @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements) {
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

    private static Optional<SolvableItemSet> fallbackSimpleLimits(ModelCombined model, JobOutput job, List<SolvableItemSet> proposedList) {
        BestHolder<SolvableItemSet> bestHolder = new BestHolder<>();

        for (SolvableItemSet set : proposedList) {
            if (model.filterOneSet(set)) {
                long rating = model.calcRating(set);
                bestHolder.add(set, rating);
            }
        }

        if (bestHolder.get() != null) {
            job.println("FALLBACK SET FOUND USING MIN/MAX ONLY");
            job.hackCount++;
            return Optional.ofNullable(bestHolder.get());
        } else {
            job.println("FALLBACK SET FAILED WITHIN BASIC MIN/MAX");
            return Optional.empty();
        }
    }

    private static Optional<SolvableItemSet> fallbackLimitsWithAdjustment(JobOutput job, List<SolvableItemSet> proposedList, ModelCombined model, @NotNull StatRequirements.StatRequirementsWithHitExpertise requirements, StatRatings ratings) {
        BestHolder<Tuple.Tuple2<SolvableItemSet, PrintRecorder>> bestHolder = new BestHolder<>();

        for (SolvableItemSet set : proposedList) {
            PrintRecorder print = new PrintRecorder();

            SolvableItemSet adjustedSet = adjustForCapsFinalSet(set.items(), print, ratings, requirements);
            if (!model.filterOneSet(adjustedSet)) {
                job.printf("ERROR adjust didn't fix caps " + set + " -> " + adjustedSet + " (or duplicate issues)");
                continue;
            }

            long rating = ratings.calcRating(adjustedSet.totalForRating());
            bestHolder.add(Tuple.create(adjustedSet, print), rating);
        }

        if (bestHolder.get() != null) {
            Tuple.Tuple2<SolvableItemSet, PrintRecorder> result = bestHolder.get();
            job.input.printRecorder.append(result.b());
            job.println("FALLBACK SET FUDGED TOGETHER WITH HACKED STATS");
            job.hackCount += 2;
            return Optional.ofNullable(result.a());
        } else {
            job.printf("ERROR adjust didn't create any usable set somehow");
            return Optional.empty();
        }
    }
}
