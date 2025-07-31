package au.nicholas.hardy.mopgear;

import java.util.ArrayList;
import java.util.List;

public class Reforger {
    static List<ItemData> reforgeItem(ReforgeRules rules, ItemData baseItem) {
        List<ItemData> outputItems = new ArrayList<>();
        outputItems.add(baseItem);

        StatType[] target = rules.target();
        for (StatType originalStat : rules.source()) {
            int originalValue = baseItem.stat.get(originalStat);
            if (originalValue != 0) {
                int reforgeQuantity = (originalValue * 4) / 10;
                int remainQuantity = originalValue - reforgeQuantity;
                for (StatType targetStat : target) {
                    if (baseItem.stat.get(targetStat) == 0) {
                        String name = baseItem.name + " (" + originalStat + "->" + targetStat + ")";
                        StatBlock changedStats = baseItem.stat.withChange(originalStat, remainQuantity, targetStat, reforgeQuantity);
                        ItemData modified = new ItemData(baseItem.slot, name, changedStats, baseItem.statFixed, baseItem.id);
                        outputItems.add(modified);
                    }
                }
            }
        }

        return outputItems;
    }
}
