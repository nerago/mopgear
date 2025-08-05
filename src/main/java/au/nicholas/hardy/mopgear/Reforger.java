package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class Reforger {
    static ItemData[] reforgeItem(ReforgeRules rules, ItemData baseItem) {
        List<ItemData> outputItems = new ArrayList<>();
        outputItems.add(baseItem);

        StatType[] target = rules.target();
        for (StatType sourceStat : rules.source()) {
            int originalValue = baseItem.stat.get(sourceStat);
            if (originalValue != 0) {
                int reforgeQuantity = (originalValue * 4) / 10;
                int remainQuantity = originalValue - reforgeQuantity;
                for (StatType targetStat : target) {
                    if (baseItem.stat.get(targetStat) == 0) {
                        ItemData modified = makeModified(baseItem, sourceStat, targetStat, remainQuantity, reforgeQuantity);
                        outputItems.add(modified);
                    }
                }
            }
        }

        return outputItems.toArray(ItemData[]::new);
    }

    public static ItemData presetReforge(ItemData baseItem, Tuple.Tuple2<StatType, StatType> statChange) {
        StatType sourceStat = statChange.a();
        StatType targetStat = statChange.b();
        if (sourceStat == null && targetStat == null) {
            return baseItem;
        } else if (sourceStat != null && targetStat != null)  {
            int originalValue = baseItem.stat.get(sourceStat);
            if (originalValue == 0 || baseItem.stat.get(targetStat) != 0)
                throw new RuntimeException("expected non-zero and zero");
            int reforgeQuantity = (originalValue * 4) / 10;
            int remainQuantity = originalValue - reforgeQuantity;
            return makeModified(baseItem, sourceStat, targetStat, remainQuantity, reforgeQuantity);
        } else {
            throw new IllegalStateException();
        }
    }

    private static ItemData makeModified(ItemData baseItem, StatType sourceStat, StatType targetStat, int remainQuantity, int reforgeQuantity) {
        String name = baseItem.name + " (" + sourceStat + "->" + targetStat + ")";
        StatBlock changedStats = baseItem.stat.withChange(sourceStat, remainQuantity, targetStat, reforgeQuantity);
        return new ItemData(baseItem.slot, name, changedStats, baseItem.statFixed, baseItem.id);
    }
}
