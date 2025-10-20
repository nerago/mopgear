package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.EquipOptionsMap;
import au.nerago.mopgear.domain.SolvableItem;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.LongHolder;
import au.nerago.mopgear.util.LowHighHolder;
import au.nerago.mopgear.util.Tuple;

public class FindStatRange2 {
    public static void checkSetReportOnly(ModelCombined model, EquipOptionsMap items, PrintRecorder job) {
        if (model.statRequirements() instanceof StatRequirements.StatRequirementsWithHitExpertise requirements) {
            for (StatType statType : StatType.values()) {
                Tuple.Tuple2<Integer, Integer> range = findRange(requirements, items, statType);
                report(statType, range, requirements, job);
            }
        } else {
            job.printf("UNKNOWN FAILURE STAT RANGES\n");
        }
    }

    private static Tuple.Tuple2<Integer, Integer> findRange(StatRequirements.StatRequirementsWithHitExpertise requirements, EquipOptionsMap itemOptions, StatType statType) {
        LongHolder low = new LongHolder(), high = new LongHolder();
        itemOptions.forEachValue(array -> {
            SolvableItem[] solvableArray = ArrayUtil.mapAsNew(array, SolvableItem::of, SolvableItem[]::new);
            LowHighHolder<SolvableItem> statRange = StatUtil.findMinMax(requirements, solvableArray, statType);
            low.value += statRange.getLowRating();
            high.value += statRange.getHighRating();
        });
        return Tuple.create((int) low.value, (int) high.value);
    }

    private static void report(StatType statType, Tuple.Tuple2<Integer, Integer> range, StatRequirements.StatRequirementsWithHitExpertise requirements, PrintRecorder job) {
        int lowAvailable = range.a(), highAvailable = range.b();
        if (statType == StatType.Hit) {
            int minTarget = requirements.getMinimumHit(), maxTarget = requirements.getMaximumHit();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                job.printf("Hit %d-%d\n", lowAvailable, highAvailable);
            } else {
                job.printf("FAIL Hit %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
            }
        } else if (statType == StatType.Expertise) {
            int minTarget = requirements.getMinimumExpertise(), maxTarget = requirements.getMaximumExpertise();
            if (highAvailable >= minTarget && lowAvailable <= maxTarget) {
                job.printf("Expertise %d-%d\n", lowAvailable, highAvailable);
            } else {
                job.printf("FAIL Expertise %d-%d NEED %d-%d\n", lowAvailable, highAvailable, minTarget, maxTarget);
            }
        } else {
            job.printf("%s %d-%d\n", statType, lowAvailable, highAvailable);
        }
    }
}
