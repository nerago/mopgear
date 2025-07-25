package au.nicholas.hardy.mopgear;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public class EngineUtil {
    public static Stream<ItemSet> filterSets(Stream<ItemSet> sets) {
        return sets.filter(set -> inRange2(set.getTotals()));
    }

    public static boolean inRange2(StatBlock totals) {
        EnumMap<Secondary, Integer> targets = ModelParams.requiredAmounts;
        for (Map.Entry<Secondary, Integer> entry : targets.entrySet()) {
            int val = totals.get(entry.getKey()), cap = entry.getValue();
            if (val < cap || val > cap + ModelParams.RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        return true;
    }
}
