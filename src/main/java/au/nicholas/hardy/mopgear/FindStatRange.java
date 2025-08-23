package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.results.JobInfo;
import au.nicholas.hardy.mopgear.util.Holder;
import au.nicholas.hardy.mopgear.util.LongHolder;
import au.nicholas.hardy.mopgear.util.LowHighHolder;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class FindStatRange {
    public static void checkSetReport(ModelCombined model, EquipOptionsMap items, JobInfo job) {
        for (StatType statType : StatType.values()) {
            Tuple.Tuple2<Integer, Integer> range = findRange(items, statType);
            report(statType, range, model, job);
        }
    }

    public static StatBlock checkSetAdjust(ModelCombined model, EquipOptionsMap items, JobInfo job) {
        for (StatType statType : StatType.values()) {
            Tuple.Tuple2<Integer, Integer> range = findRange(items, statType);
            StatBlock adjust = reportAndAdjust(statType, range, model, job);
            if (adjust != null)
                return adjust;
        }
        return null;
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

    @SuppressWarnings("StatementWithEmptyBody")
    private static StatBlock reportAndAdjust(StatType statType, Tuple.Tuple2<Integer, Integer> range, ModelCombined model, JobInfo job) {
        int lowAvailable = range.a(), highAvailable = range.b();
        if (statType == StatType.Hit) {
            int minTarget = model.statRequirements().getMinimumHit(), maxTarget = model.statRequirements().getMaximumHit();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                // ok
            } else if (highAvailable < minTarget) {
                StatType takeStat = model.statRatings().bestNonHit();
                int need = minTarget - highAvailable;
                job.printf("BAD Hit Low %d-%d NEED %d-%d STEALING %d %s\n", lowAvailable, highAvailable, minTarget, maxTarget, need, takeStat);
                return StatBlock.empty.withChange(StatType.Hit, need, takeStat, -need);
            } else {
                job.printf("BAD Hit %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
                throw new RuntimeException("not yet supported");
            }
        } else if (statType == StatType.Expertise) {
            int minTarget = model.statRequirements().getMinimumExpertise(), maxTarget = model.statRequirements().getMaximumExpertise();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                // ok
            } else if (highAvailable < minTarget) {
                StatType takeStat = model.statRatings().bestNonHit();
                int need = minTarget - highAvailable;
                job.printf("BAD Expertise Low %d-%d NEED %d-%d STEALING %d %s\n", lowAvailable, highAvailable, minTarget, maxTarget, need, takeStat);
                return StatBlock.empty.withChange(StatType.Expertise, need, takeStat, -need);
            } else {
                job.printf("BAD Expertise %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
                throw new RuntimeException("not yet supported");
            }
        }
        return null;
    }

    public static ItemSet adjustForCapsFinalSet(ItemSet set, ModelCombined model, JobInfo job) {
        StatBlock itemTotal = StatBlock.sum(set.items);
        StatBlock adjust = StatBlock.empty;
        StatType takeStat = model.statRatings().bestNonHit();

        int minHit = model.statRequirements().getMinimumHit(), maxHit = model.statRequirements().getMaximumHit();
        if (minHit != 0 && itemTotal.hit < minHit) {
            int need = minHit - itemTotal.hit;
            job.printf("ADJUST Hit Low %d NEED %d STEALING %d %s\n", itemTotal.hit, minHit, need, takeStat);
            adjust = adjust.withChange(StatType.Hit, need, takeStat, -need);
        } else if (maxHit != 0 && itemTotal.hit > maxHit) {
            int excess = itemTotal.hit - maxHit;
            job.printf("ADJUST Hit High %d LIMIT %d GIFTING %d %s\n", itemTotal.hit, maxHit, excess, takeStat);
            adjust = adjust.withChange(StatType.Hit, -excess, takeStat, excess);
        }

        int minExp = model.statRequirements().getMinimumExpertise(), maxExp = model.statRequirements().getMaximumExpertise();
        if (itemTotal.expertise < minExp) {
            int need = minExp - itemTotal.expertise;
            job.printf("ADJUST Expertise Low %d NEED %d STEALING %d %s\n", itemTotal.expertise, minExp, need, takeStat);
            adjust = adjust.withChange(StatType.Expertise, need, takeStat, -need);
        } else if (maxExp != 0 && itemTotal.expertise > maxExp) {
            int excess = itemTotal.expertise - maxExp;
            job.printf("ADJUST Hit High %d LIMIT %d GIFTING %d %s\n", itemTotal.expertise, maxExp, excess, takeStat);
            adjust = adjust.withChange(StatType.Expertise, -excess, takeStat, excess);
        }

        if (adjust == StatBlock.empty) {
            throw new IllegalStateException("expected to need adjust");
        }

        return ItemSet.manyItems(set.items, adjust);
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
}
