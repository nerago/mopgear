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
    public static Optional<ItemSet> fallbackLimits(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, JobOutput job) {
        job.println("NO SET FOUND USING NORMAL PROCESS");

        @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements =
                model.statRequirements() instanceof StatRequirements.StatRequirementsWithHitExpertise
                        ? (StatRequirements.StatRequirementsWithHitExpertise) model.statRequirements() : null;

        List<ItemSet> proposedList = setsAtLimits(itemOptions, adjustment, requirements);
        Optional<ItemSet> result = fallbackSimpleLimits(model, job, proposedList);
        if (result.isEmpty() && requirements != null) {
            // NOTE only works for some StatRequirements
            result = fallbackLimitsWithAdjustment(job, proposedList, model, requirements, model.statRatings());
        }
        return result;
    }

    private static List<ItemSet> setsAtLimits(EquipOptionsMap itemOptions, StatBlock adjustment, @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements) {
        List<ItemSet> setList = new ArrayList<>();
        for (StatType statType : StatType.values()) {
            findSets(itemOptions, adjustment, statType, setList, requirements);
        }
        return setList;
    }

    @SuppressWarnings("ConditionCoveredByFurtherCondition")
    private static ItemSet adjustForCapsFinalSet(EquipMap setItems, PrintRecorder print, StatRatings ratings, @NotNull StatRequirements.StatRequirementsWithHitExpertise requirements) {
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

        return ItemSet.manyItems(setItems, adjust);
    }

    private static void findSets(EquipOptionsMap itemOptions, StatBlock adjustment, StatType statType, List<ItemSet> setList, @Nullable StatRequirements.StatRequirementsWithHitExpertise requirements) {
        Holder<ItemSet> lowSet = new Holder<>(), highSet = new Holder<>();
        itemOptions.forEachPair((slot, array) -> {
            LowHighHolder<ItemData> statRange = StatUtil.findMinMax(requirements, array, statType);
            if (lowSet.value == null) {
                lowSet.value = ItemSet.singleItem(slot, statRange.getLow(), adjustment);
                highSet.value = ItemSet.singleItem(slot, statRange.getHigh(), adjustment);
            } else {
                lowSet.value = lowSet.value.copyWithAddedItem(slot, statRange.getLow());
                highSet.value = highSet.value.copyWithAddedItem(slot, statRange.getHigh());
            }
        });
        setList.add(lowSet.value);
        setList.add(highSet.value);
    }

    private static Optional<ItemSet> fallbackSimpleLimits(ModelCombined model, JobOutput job, List<ItemSet> proposedList) {
        BestHolder<ItemSet> bestHolder = new BestHolder<>();

        for (ItemSet set : proposedList) {
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

    private static Optional<ItemSet> fallbackLimitsWithAdjustment(JobOutput job, List<ItemSet> proposedList, ModelCombined model, @NotNull StatRequirements.StatRequirementsWithHitExpertise requirements, StatRatings ratings) {
        BestHolder<Tuple.Tuple2<ItemSet, PrintRecorder>> bestHolder = new BestHolder<>();

        for (ItemSet set : proposedList) {
            PrintRecorder print = new PrintRecorder();

            //noinspection deprecation
            ItemSet adjustedSet = adjustForCapsFinalSet(set.items().shallowClone(), print, ratings, requirements);
            if (!model.filterOneSet(adjustedSet)) {
                job.printf("ERROR adjust didn't fix caps " + set + " -> " + adjustedSet + " (or duplicate issues)");
                continue;
            }

            long rating = ratings.calcRating(adjustedSet.totalForRating());
            bestHolder.add(Tuple.create(adjustedSet, print), rating);
        }

        if (bestHolder.get() != null) {
            Tuple.Tuple2<ItemSet, PrintRecorder> result = bestHolder.get();
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
