package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.FullItemData;
import au.nerago.mopgear.domain.ReforgeRecipe;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.ReforgeRules;
import au.nerago.mopgear.util.BestHolder;

import java.util.ArrayList;
import java.util.List;

public class Reforger {
    public static FullItemData[] reforgeItem(ReforgeRules rules, FullItemData baseItem) {
        List<FullItemData> outputItems = reforgeItemToList(rules, baseItem);
        return outputItems.toArray(FullItemData[]::new);
    }

    private static List<FullItemData> reforgeItemToList(ReforgeRules rules, FullItemData baseItem) {
        List<FullItemData> outputItems = new ArrayList<>();
        outputItems.add(baseItem);

        StatType[] target = rules.target();
        for (StatType sourceStat : rules.source()) {
            int originalValue = baseItem.statBase.get(sourceStat);
            if (originalValue != 0) {
                int reforgeQuantity = (originalValue * 4) / 10;
                int remainQuantity = originalValue - reforgeQuantity;
                for (StatType targetStat : target) {
                    if (baseItem.statBase.get(targetStat) == 0) {
                        FullItemData modified = makeModified(baseItem, new ReforgeRecipe(sourceStat, targetStat), remainQuantity, reforgeQuantity);
                        outputItems.add(modified);
                    }
                }
            }
        }
        return outputItems;
    }

    public static FullItemData[] reforgeItemBest(ModelCombined model, FullItemData baseItem) {
        List<FullItemData> reforgedItems = reforgeItemToList(model.reforgeRules(), baseItem);
        BestHolder<FullItemData> best = new BestHolder<>();
        for (FullItemData item : reforgedItems) {
            best.add(item, model.calcRating(item));
        }
        return new FullItemData[] { best.get() };
    }

    public static FullItemData presetReforge(FullItemData baseItem, ReforgeRecipe statChange) {
        if (statChange == null) {
            return baseItem;
        }

        StatType sourceStat = statChange.source();
        StatType targetStat = statChange.dest();
        if (sourceStat == null && targetStat == null) {
            return baseItem;
        } else if (sourceStat != null && targetStat != null)  {
            if (sourceStat == targetStat)
                throw new RuntimeException("expected different stats");
            int originalValue = baseItem.statBase.get(sourceStat);
            if (originalValue == 0 || baseItem.statBase.get(targetStat) != 0)
                throw new RuntimeException("expected non-zero and zero");
            int reforgeQuantity = (originalValue * 4) / 10;
            int remainQuantity = originalValue - reforgeQuantity;
            return makeModified(baseItem, new ReforgeRecipe(sourceStat, targetStat), remainQuantity, reforgeQuantity);
        } else {
            throw new IllegalStateException();
        }
    }

    private static FullItemData makeModified(FullItemData baseItem, ReforgeRecipe recipe, int remainQuantity, int reforgeQuantity) {
        StatBlock changedStats = baseItem.statBase.withChange(recipe.source(), remainQuantity, recipe.dest(), reforgeQuantity);
        return baseItem.changeForReforge(changedStats, recipe);
    }
}
