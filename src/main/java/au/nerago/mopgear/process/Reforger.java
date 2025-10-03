package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ReforgeRecipe;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.ReforgeRules;
import au.nerago.mopgear.util.BestHolder;

import java.util.ArrayList;
import java.util.List;

public class Reforger {
    public static ItemData[] reforgeItem(ReforgeRules rules, ItemData baseItem) {
        List<ItemData> outputItems = reforgeItemToList(rules, baseItem);
        return outputItems.toArray(ItemData[]::new);
    }

    private static List<ItemData> reforgeItemToList(ReforgeRules rules, ItemData baseItem) {
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
                        ItemData modified = makeModified(baseItem, new ReforgeRecipe(sourceStat, targetStat), remainQuantity, reforgeQuantity);
                        outputItems.add(modified);
                    }
                }
            }
        }
        return outputItems;
    }

    public static ItemData[] reforgeItemBest(ModelCombined model, ItemData baseItem) {
        List<ItemData> reforgedItems = reforgeItemToList(model.reforgeRules(), baseItem);
        BestHolder<ItemData> best = new BestHolder<>();
        for (ItemData item : reforgedItems) {
            best.add(item, model.calcRating(item));
        }
        return new ItemData[] { best.get() };
    }

    public static ItemData presetReforge(ItemData baseItem, ReforgeRecipe statChange) {
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
            int originalValue = baseItem.stat.get(sourceStat);
            if (originalValue == 0 || baseItem.stat.get(targetStat) != 0)
                throw new RuntimeException("expected non-zero and zero");
            int reforgeQuantity = (originalValue * 4) / 10;
            int remainQuantity = originalValue - reforgeQuantity;
            return makeModified(baseItem, new ReforgeRecipe(sourceStat, targetStat), remainQuantity, reforgeQuantity);
        } else {
            throw new IllegalStateException();
        }
    }

    private static ItemData makeModified(ItemData baseItem, ReforgeRecipe recipe, int remainQuantity, int reforgeQuantity) {
        String name = baseItem.name + " (" + recipe.source() + "->" + recipe.dest() + ")";
        StatBlock changedStats = baseItem.stat.withChange(recipe.source(), remainQuantity, recipe.dest(), reforgeQuantity);
        return baseItem.changeNameAndStats(name, changedStats, recipe);
    }
}
