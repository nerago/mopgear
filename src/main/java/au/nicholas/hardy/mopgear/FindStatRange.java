package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.results.JobInfo;
import au.nicholas.hardy.mopgear.results.PrintRecorder;
import au.nicholas.hardy.mopgear.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FindStatRange {
    public static void checkSetReport(ModelCombined model, EquipOptionsMap items, JobInfo job) {
        for (StatType statType : StatType.values()) {
            Tuple.Tuple2<Integer, Integer> range = findRange(items, statType);
            report(statType, range, model, job);
        }
    }

    public static StatBlock checkSetAdjust(ModelCombined model, EquipOptionsMap items, JobInfo job) {
        Tuple.Tuple2<Integer, Integer> hitRange = findRange(items, StatType.Hit);
        StatBlock hitAdjust = reportAndAdjustHit(model, job, hitRange);

        Tuple.Tuple2<Integer, Integer> expRange = findRange(items, StatType.Expertise);
        StatBlock expAdjust = reportAndAdjustExpertise(model, job, expRange);

        return StatBlock.add(hitAdjust, expAdjust);
    }

    public static List<ItemSet> setsAtLimits(EquipOptionsMap itemOptions, StatBlock adjustment) {
        List<ItemSet> setList = new ArrayList<>();
        for (StatType statType : StatType.values()) {
            findSets(itemOptions, adjustment, statType, setList);
        }
        return setList;
    }

    private static void report(StatType statType, Tuple.Tuple2<Integer, Integer> range, ModelCombined model, JobInfo job) {
        int lowAvailable = range.a(), highAvailable = range.b();
        if (statType == StatType.Hit) {
            int minTarget = model.statRequirements().getMinimumHit(), maxTarget = model.statRequirements().getMaximumHit();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                job.printf("Hit %d-%d\n", lowAvailable, highAvailable);
            } else {
                job.printf("FAIL Hit %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
            }
        } else if (statType == StatType.Expertise) {
            int minTarget = model.statRequirements().getMinimumExpertise(), maxTarget = model.statRequirements().getMaximumExpertise();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                job.printf("Expertise %d-%d\n", lowAvailable, highAvailable);
            } else {
                job.printf("FAIL Expertise %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
            }
        } else {
            job.printf("%s %d-%d\n", statType, lowAvailable, highAvailable);
        }
    }

    private static StatBlock reportAndAdjustHit(ModelCombined model, JobInfo job, Tuple.Tuple2<Integer, Integer> range) {
        int lowAvailable = range.a(), highAvailable = range.b();
        int minTarget = model.statRequirements().getMinimumHit(), maxTarget = model.statRequirements().getMaximumHit();
        if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
            // ok
        } else if (highAvailable < minTarget) {
            StatType takeStat = model.statRatings().bestNonHit();
            int need = minTarget - highAvailable;
            job.printf("BAD Hit Low %d-%d NEED %d-%d STEALING %d %s\n", lowAvailable, highAvailable, minTarget, maxTarget, need, takeStat);
            return StatBlock.of(StatType.Hit, need, takeStat, -need);
        } else {
            job.printf("BAD Hit %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
            throw new RuntimeException("not yet supported");
        }
        return null;
    }

    private static StatBlock reportAndAdjustExpertise(ModelCombined model, JobInfo job, Tuple.Tuple2<Integer, Integer> range) {
        int lowAvailable = range.a(), highAvailable = range.b();
        int minTarget = model.statRequirements().getMinimumExpertise(), maxTarget = model.statRequirements().getMaximumExpertise();
        if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
            // ok
        } else if (highAvailable < minTarget) {
            StatType takeStat = model.statRatings().bestNonHit();
            int need = minTarget - highAvailable;
            job.printf("BAD Expertise Low %d-%d NEED %d-%d STEALING %d %s\n", lowAvailable, highAvailable, minTarget, maxTarget, need, takeStat);
            return StatBlock.of(StatType.Expertise, need, takeStat, -need);
        } else {
            job.printf("BAD Expertise %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
            throw new RuntimeException("not yet supported");
        }
        return null;
    }

    @SuppressWarnings("ConditionCoveredByFurtherCondition")
    public static ItemSet adjustForCapsFinalSet(EquipMap setItems, ModelCombined model, PrintRecorder print) {
        StatBlock itemTotal = StatBlock.sum(setItems);
        StatBlock adjust = StatBlock.empty;
        StatType takeStat = model.statRatings().bestNonHit();

        int minHit = model.statRequirements().getMinimumHit(), maxHit = model.statRequirements().getMaximumHit();
        int effectiveHit = model.statRequirements().effectiveHit(itemTotal);
        if (minHit != 0 && effectiveHit < minHit) {
            int need = minHit - effectiveHit;
            print.printf("ADJUST Hit Low %d NEED %d STEALING %d %s\n", effectiveHit, minHit, need, takeStat);
            adjust = adjust.withChange(StatType.Hit, need, takeStat, -need);
        } else if (minHit != 0 && maxHit != Integer.MAX_VALUE && effectiveHit > maxHit) {
            int excess = effectiveHit - maxHit + 1;
            print.printf("ADJUST Hit High %d LIMIT %d GIFTING %d %s\n", effectiveHit, maxHit, excess, takeStat);
            adjust = adjust.withChange(StatType.Hit, -excess, takeStat, excess);
        }

        int minExp = model.statRequirements().getMinimumExpertise(), maxExp = model.statRequirements().getMaximumExpertise();
        if (minExp != 0 && itemTotal.expertise < minExp) {
            int need = minExp - itemTotal.expertise;
            print.printf("ADJUST Expertise Low %d NEED %d STEALING %d %s\n", itemTotal.expertise, minExp, need, takeStat);
            adjust = adjust.withChange(StatType.Expertise, need, takeStat, -need);
        } else if (minExp != 0 && maxExp != Integer.MAX_VALUE && itemTotal.expertise > maxExp) {
            int excess = itemTotal.expertise - maxExp;
            print.printf("ADJUST Expertise High %d LIMIT %d GIFTING %d %s\n", itemTotal.expertise, maxExp, excess, takeStat);
            adjust = adjust.withChange(StatType.Expertise, -excess, takeStat, excess);
        }

        if (adjust == StatBlock.empty) {
            print.printf("ERROR expected to need adjust, none needed");
            //throw new IllegalStateException("expected to need adjust");
        }

        return ItemSet.manyItems(setItems, adjust);
    }

    private static Tuple.Tuple2<Integer, Integer> findRange(EquipOptionsMap itemOptions, StatType statType) {
        LongHolder low = new LongHolder(), high = new LongHolder();
        itemOptions.forEachValue(array -> {
            LowHighHolder<ItemData> statRange = findMinMax(array, statType);
            low.value += statRange.getLowRating();
            high.value += statRange.getHighRating();
        });
        return Tuple.create((int) low.value, (int) high.value);
    }

    private static void findSets(EquipOptionsMap itemOptions, StatBlock adjustment, StatType statType, List<ItemSet> setList) {
        Holder<ItemSet> lowSet = new Holder<>(), highSet = new Holder<>();
        itemOptions.forEachPair((slot, array) -> {
            LowHighHolder<ItemData> statRange = findMinMax(array, statType);
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

    private static LowHighHolder<ItemData> findMinMax(ItemData[] itemArray, StatType statType) {
        LowHighHolder<ItemData> holder = new LowHighHolder<>();
        for (ItemData item : itemArray) {
            int value = item.totalStatCopy().get(statType);
            holder.add(item, value);
        }
        return holder;
    }

    public static Optional<ItemSet> fallbackLimits(ModelCombined model, EquipOptionsMap itemOptions, StatBlock adjustment, JobInfo job) {
        job.println("FALLBACK SET SEARCHING");

        List<ItemSet> proposedList = setsAtLimits(itemOptions, adjustment);
        Optional<ItemSet> result = fallbackSimpleLimits(model, job, proposedList);
        if (result.isPresent()) {
            return result;
        } else {
            job.println("FALLBACK SET FAILED WITHIN HIT/EXP CAP");
            return fallbackLimitsWithAdjustment(model, job, proposedList);
        }
    }

    private static Optional<ItemSet> fallbackSimpleLimits(ModelCombined model, JobInfo job, List<ItemSet> proposedList) {
        BestHolder<ItemSet> bestHolder = new BestHolder<>(null, 0);

        for (ItemSet set : proposedList) {
            if (model.statRequirements().filter(set)) {
                long rating = model.calcRating(set);
                bestHolder.add(set, rating);
            }
        }

        if (bestHolder.get() != null) {
            job.println("FALLBACK SET FOUND USING MIN/MAX ONLY");
            job.hackCount++;
            return Optional.ofNullable(bestHolder.get());
        } else {
            return Optional.empty();
        }
    }

    private static Optional<ItemSet> fallbackLimitsWithAdjustment(ModelCombined model, JobInfo job, List<ItemSet> proposedList) {
        BestHolder<Tuple.Tuple2<ItemSet, PrintRecorder>> bestHolder = new BestHolder<>(null, 0);

        for (ItemSet set : proposedList) {
            PrintRecorder print = new PrintRecorder();

            ItemSet adjustedSet = adjustForCapsFinalSet(set.items.shallowClone(), model, print);
            if (!model.statRequirements().filter(adjustedSet)) {
//                throw new IllegalStateException("adjust didn't fix caps " + set + " -> " + adjustedSet);
                job.printf("ERROR adjust didn't fix caps " + set + " -> " + adjustedSet + " (or duplicate issues)");
                continue;
            }

            long rating = model.calcRating(adjustedSet);
            bestHolder.add(Tuple.create(adjustedSet, print), rating);
        }

        if (bestHolder.get() != null) {
            Tuple.Tuple2<ItemSet, PrintRecorder> result = bestHolder.get();
            job.printRecorder.append(result.b());
            job.println("FALLBACK SET FOUND FORCING CAPS");
            job.hackCount += 2;
            return Optional.ofNullable(result.a());
        } else {
//            throw new IllegalStateException("adjust didn't create any usable set somehow");
            job.printf("ERROR adjust didn't create any usable set somehow");
            return Optional.empty();
        }
    }
}
