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
            FullItemData[] items = itemMap.get(slot);
            if (items != null) {
                result.put(slot, ArrayUtil.mapAsNew(items, ItemLevel::scaleForChallengeMode));
            }
        }
        return result;
    }

    private static FullItemData scaleForChallengeMode(FullItemData item) {
        int level = item.itemLevel();
        if (level <= CHALLENGE_TARGET_LEVEL) {
            return item;
        }

        double factor = calcMultiplier(level, CHALLENGE_TARGET_LEVEL);
        return scaleAll(item, factor);
    }

    private static FullItemData scaleAll(FullItemData item, double factor) {
        StatBlock stats = scaleStatBlock(item.statBase, factor);
        if (item.slot() != SlotItem.Trinket) {
            OutputText.println("SCALED " + item.fullName() + " " + stats);
            return item.changeStatsBase(stats);
        } else {
            StatBlock statsFixed = scaleStatBlock(item.statEnchant, factor);
            OutputText.println("SCALED TRINKET " + item.fullName() + " " + stats + " " + statsFixed);
            return item.changeStatsBase(stats).changeEnchant(statsFixed, null, null);
        }
    }

    public static FullItemData upgrade(FullItemData item, int upgradeLevel) {
        if (upgradeLevel < 0 || upgradeLevel > 2)
            throw new IllegalArgumentException("invalid upgrade level");

        ItemRef ref = item.shared.ref();
        int currentLevel = ref.itemLevel();
        int baseItemLevel = ref.itemLevelBase();
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
