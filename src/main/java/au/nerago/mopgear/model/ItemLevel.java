package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.ArrayUtil;

public class ItemLevel {
    private static final int CHALLENGE_TARGET_LEVEL = 463;
    private static final double FORMULA_POWER = 5.48;
    public static final int ITEM_LEVELS_PER_UPGRADE_LEVEL = 4;
    public static final int MAX_UPGRADE_LEVEL = 2;

    private static double calcMultiplier(int levelFrom, int levelTo) {
        return Math.pow((double) levelTo / (double) levelFrom, FORMULA_POWER);
    }

    public static EquipOptionsMap scaleForChallengeMode(EquipOptionsMap itemMap) {
        EquipOptionsMap result = EquipOptionsMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] items = itemMap.get(slot);
            if (items != null) {
                result.put(slot, ArrayUtil.mapAsNew(items, ItemLevel::scaleForChallengeMode));
            }
        }
        return result;
    }

    private static ItemData scaleForChallengeMode(ItemData item) {
        int level = item.ref.itemLevel();
        if (level <= CHALLENGE_TARGET_LEVEL) {
            return item;
        }

        double factor = calcMultiplier(level, CHALLENGE_TARGET_LEVEL);
        return scaleAll(item, factor);
    }

    private static ItemData scaleAll(ItemData item, double factor) {
        StatBlock stats = scaleStatBlock(item.stat, factor);
        if (item.slot != SlotItem.Trinket) {
            OutputText.println("SCALED " + item.name + " " + stats);
            return item.changeStats(stats);
        } else {
            StatBlock statsFixed = scaleStatBlock(item.statFixed, factor);
            OutputText.println("SCALED TRINKET " + item.name + " " + stats + " " + statsFixed);
            return item.changeStats(stats).changeFixed(statsFixed);
        }
    }

    public static ItemData upgrade(ItemData item, int upgradeLevel) {
        if (upgradeLevel < 0 || upgradeLevel > 2)
            throw new IllegalArgumentException("invalid upgrade level");

        int currentLevel = item.ref.itemLevel();
        int baseItemLevel = item.ref.itemLevelBase();
        int targetLevel = baseItemLevel + upgradeLevel * ITEM_LEVELS_PER_UPGRADE_LEVEL;

        if (currentLevel != targetLevel) {
            double factor = calcMultiplier(currentLevel, targetLevel);
            return scaleAll(item, factor).changeItemLevel(targetLevel);
        } else {
            return item;
        }
    }

    private static StatBlock scaleStatBlock(StatBlock stats, double factor) {
        for (StatType type : StatType.values()) {
            double val = stats.get(type);
            if (val != 0) {
                val *= factor;
                stats = stats.withChange(type, (int) Math.round(val));
            }
        }
        return stats;
    }

    // https://github.com/wowsims/mop/blob/master/tools/database/dbc/item.go#L173
//    func (item *Item) GetScaledStat(index int, itemLevel int) float64 {
//
//        if itemLevel == item.ItemLevel {
//            // Maybe just return it?
//            return item.BonusAmountCalculated[index]
//        }
//
//        slotType := item.GetRandomSuffixType()
//        itemBudget := 0.0
//
//        if slotType != -1 && item.OverallQuality > 0 {
//
//            randomProperty := GetDBC().RandomPropertiesByIlvl[itemLevel]
//            itemBudget = float64(randomProperty[item.OverallQuality.ToProto()][slotType])
//
//            if item.StatAlloc[index] > 0 && itemBudget > 0 {
//                rawValue := math.Round(item.StatAlloc[index] * itemBudget * 0.0001)
//
//                // Figure out if this does anything in MoP
//                //Not used right now in Cata
//                //socket_penalty := math.RoundNearby item.StatPercentageOfSocket[index] * SocketCost(itemLevel)
//                return rawValue - item.SocketModifier[index] // Could this be a calculated socket penalty?
//            } else {
//                return math.Floor(item.BonusAmountCalculated[index] * item.ApproximateScaleCoeff(item.ItemLevel, itemLevel))
//            }
//        }
//        return 0
//    }
}
