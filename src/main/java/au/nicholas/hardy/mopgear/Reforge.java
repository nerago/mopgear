package au.nicholas.hardy.mopgear;

import java.util.ArrayList;
import java.util.List;

public class Reforge {
    static List<ItemData> reforgeItem(ItemData baseItem) {
        List<ItemData> outputItems = new ArrayList<>();
        outputItems.add(baseItem);

        for (StatType originalStat : ModelCommon.reforgeSource) {
            int originalValue = baseItem.stat.get(originalStat);
            if (originalValue != 0) {
                int reforgeQuantity = (originalValue * 4) / 10;
                int remainQuantity = originalValue - reforgeQuantity;
                for (StatType targetStat : ModelCommon.reforgeTargets) {
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
