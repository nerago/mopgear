package au.nicholas.hardy.mopgear;

import java.util.ArrayList;
import java.util.List;

public class Reforge {
    static List<ItemData> reforgeItem(ItemData baseItem) {
        List<ItemData> outputItems = new ArrayList<>();
        outputItems.add(baseItem);

        for (Secondary originalStat : Secondary.values()) {
            int originalValue = baseItem.get(originalStat);
            if (originalValue != 0) {
                int reforgeQuantity = (originalValue * 4) / 10;
                int remainQuantity = originalValue - reforgeQuantity;
                for (Secondary targetStat : ModelParams.reforgeTargets) {
                    if (baseItem.get(targetStat) == 0) {
                        ItemData modified = baseItem.copy();
                        modified.name += " (" + originalStat + "->" + targetStat + ")";
                        modified.set(originalStat, remainQuantity);
                        modified.set(targetStat, reforgeQuantity);
                        outputItems.add(modified);
                    }
                }
            }
        }

        return outputItems;
    }
}
