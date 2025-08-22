package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.EquipOptionsMap;
import au.nicholas.hardy.mopgear.domain.ItemData;
import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.domain.StatType;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.LongHolder;
import au.nicholas.hardy.mopgear.util.LowHighHolder;
import au.nicholas.hardy.mopgear.util.Tuple;

public class FindStatRange {
    public static void checkSetReport(ModelCombined model, EquipOptionsMap items) {
        for (StatType statType : StatType.values()) {
            Tuple.Tuple2<Integer, Integer> range = findRange(items, statType);
            report(statType, range, model);
        }
    }

    public static StatBlock checkSetAdjust(ModelCombined model, EquipOptionsMap items) {
        for (StatType statType : StatType.values()) {
            Tuple.Tuple2<Integer, Integer> range = findRange(items, statType);
            StatBlock adjust = reportAndAdjust(statType, range, model);
            if (adjust != null)
                return adjust;
        }
        return null;
    }

    private static void report(StatType statType, Tuple.Tuple2<Integer, Integer> range, ModelCombined model) {
        int lowAvailable = range.a(), highAvailable = range.b();
        if (statType == StatType.Hit) {
            int minTarget = model.statRequirements().getMinimumHit(), maxTarget = model.statRequirements().getMaximumHit();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                System.out.printf("Hit %d-%d\n", lowAvailable, highAvailable);
            } else {
                System.out.printf("FAIL Hit %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
            }
        } else if (statType == StatType.Expertise) {
            int minTarget = model.statRequirements().getMinimumExpertise(), maxTarget = model.statRequirements().getMaximumExpertise();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                System.out.printf("Expertise %d-%d\n", lowAvailable, highAvailable);
            } else {
                System.out.printf("FAIL Expertise %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
            }
        } else {
            System.out.printf("%s %d-%d\n", statType, lowAvailable, highAvailable);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static StatBlock reportAndAdjust(StatType statType, Tuple.Tuple2<Integer, Integer> range, ModelCombined model) {
        int lowAvailable = range.a(), highAvailable = range.b();
        if (statType == StatType.Hit) {
            int minTarget = model.statRequirements().getMinimumHit(), maxTarget = model.statRequirements().getMaximumHit();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                // ok
            } else if (highAvailable < minTarget) {
                StatType takeStat = model.statRatings().bestNonHit();
                int need = minTarget - highAvailable;
                System.out.printf("FAIL Hit Low %d-%d NEED %d-%d STEALING %d %s\n", lowAvailable, highAvailable, minTarget, maxTarget, need, takeStat);
                return StatBlock.empty.withChange(StatType.Hit, need, takeStat, -need);
            } else {
                System.out.printf("FAIL Hit %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
                throw new RuntimeException("not yet supported");
            }
        } else if (statType == StatType.Expertise) {
            int minTarget = model.statRequirements().getMinimumExpertise(), maxTarget = model.statRequirements().getMaximumExpertise();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                // ok
            } else if (highAvailable < minTarget) {
                StatType takeStat = model.statRatings().bestNonHit();
                int need = minTarget - highAvailable;
                System.out.printf("FAIL Expertise Low %d-%d NEED %d-%d STEALING %d %s\n", lowAvailable, highAvailable, minTarget, maxTarget, need, takeStat);
                return StatBlock.empty.withChange(StatType.Expertise, need, takeStat, -need);
            } else {
                System.out.printf("FAIL Expertise %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
                throw new RuntimeException("not yet supported");
            }
        }
        return null;
    }

    private static Tuple.Tuple2<Integer, Integer> findRange(EquipOptionsMap items, StatType statType) {
        LongHolder low = new LongHolder(), high = new LongHolder();
        items.forEachValue(array -> {
            LowHighHolder<Object> statRange = findMinMax(array, statType);
            low.value += statRange.getLowRating();
            high.value += statRange.getHighRating();
        });
        return Tuple.create((int) low.value, (int) high.value);
    }

    private static LowHighHolder<Object> findMinMax(ItemData[] itemArray, StatType statType) {
        LowHighHolder<Object> holder = new LowHighHolder<>();
        for (ItemData item : itemArray) {
            int value = item.totalStatCopy().get(statType);
            holder.add(item, value);
        }
        return holder;
    }
}
