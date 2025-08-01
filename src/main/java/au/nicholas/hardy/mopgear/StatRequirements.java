package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public class StatRequirements {
    public StatRequirements(boolean blacksmith, boolean parryExpertise) {
        this.blacksmith = blacksmith;
        this.parryExpertise = parryExpertise;
        this.requiredAmounts = buildRequired(parryExpertise);
    }

    private static final double RATING_PER_PERCENT = 339.9534;
        static final double TARGET_PERCENT = 7.5; // for bosses
//    private static final double TARGET_PERCENT = 6; // for heroics
    private static final int TARGET_RATING = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT); // 2040 / 2550

    private static final int RATING_CAP_ALLOW_EXCEED = 100;

    public final EnumMap<StatType, Integer> requiredAmounts;
    public final boolean blacksmith;
    public final boolean parryExpertise;

    private static EnumMap<StatType, Integer> buildRequired(boolean parryExpertise) {
        EnumMap<StatType, Integer> map = new EnumMap<>(StatType.class);
        map.put(StatType.Hit, TARGET_RATING);
//        if (parryExpertise)
//            map.put(StatType.Expertise, TARGET_RATING * 2);
//        else
//            map.put(StatType.Expertise, TARGET_RATING);
        if (!parryExpertise)
            map.put(StatType.Expertise, TARGET_RATING);
        return map;
    }

//    public void validate() {
//        if (Arrays.stream(reforgeTargets).distinct().count() != reforgeTargets.length)
//            throw new IllegalStateException("reforgeTargets not distinct");
//        if (!Arrays.asList(reforgeTargets).containsAll(requiredAmounts.keySet()))
//            throw new IllegalStateException("todo");
//    }

    public Stream<ItemSet> filterSets(Stream<ItemSet> sets) {
//        return sets.filter(set -> hasNoDuplicate(set.items) && inRange2(set.getTotals()));
        return sets.filter(set -> inRange(set.getTotals()));
    }

    private static boolean hasNoDuplicate(CurryQueue<ItemData> items) {
        ItemData ring = null, trink = null;
        do {
            ItemData item = items.item();
            switch (item.slot) {
                case Ring -> {
                    if (ring == null)
                        ring = item;
                    else if (ring.id == item.id)
                        return false;
                }
                case Trinket -> {
                    if (trink == null)
                        trink = item;
                    else if (trink.id == item.id)
                        return false;
                }
            }
            items = items.tail();
        } while (items != null);
        return true;
    }

    public boolean inRange(StatBlock totals) {
        for (Map.Entry<StatType, Integer> entry : requiredAmounts.entrySet()) {
            int val = totals.get(entry.getKey()), cap = entry.getValue();
            if (val < cap || val > cap + RATING_CAP_ALLOW_EXCEED)
                return false;
        }
        return true;
    }
}
