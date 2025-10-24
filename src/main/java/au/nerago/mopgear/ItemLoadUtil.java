package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.InputGearParser;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.io.WowHead;
import au.nerago.mopgear.model.GemData;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.ReforgeRules;
import au.nerago.mopgear.model.Trinkets;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.ArrayUtil;

import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("unused")
public class ItemLoadUtil {
    private static final Set<SlotItem> expectedEnchant = buildExpectedEnchant();

    private static Set<SlotItem> buildExpectedEnchant() {
        EnumSet<SlotItem> set = EnumSet.noneOf(SlotItem.class);
        set.add(SlotItem.Shoulder);
        set.add(SlotItem.Back);
        set.add(SlotItem.Chest);
        set.add(SlotItem.Wrist);
        set.add(SlotItem.Hand);
//        set.add(SlotItem.Belt);
        set.add(SlotItem.Leg);
        set.add(SlotItem.Foot);
        set.add(SlotItem.Weapon1H);
        set.add(SlotItem.Weapon2H);
        set.add(SlotItem.Offhand);
        return set;
    }

    public static EquipOptionsMap readAndLoad(boolean detailedOutput, Path file, ReforgeRules rules, Map<Integer, List<ReforgeRecipe>> presetForge) {
        List<EquippedItem> itemIds = InputGearParser.readInput(file);
        List<FullItemData> items = loadItems(itemIds, detailedOutput);
        EquipOptionsMap result = presetForge != null
                ? ItemMapUtil.limitedItemsReforgedToMap(rules, items, presetForge)
                : ItemMapUtil.standardItemsReforgedToMap(rules, items);
        ItemCache.instance.cacheSave();
        return result;
    }

    public static void forceReload(ItemCache itemCache, Path file) {
        List<EquippedItem> itemIds = InputGearParser.readInput(file);
        for (EquippedItem equippedItem : itemIds) {
            int id = equippedItem.itemId();
            FullItemData item = WowHead.fetchItem(id);
            itemCache.put(item);
        }
        itemCache.cacheSave();
    }

    public static List<FullItemData> loadItems(List<EquippedItem> itemIds, boolean detailedOutput) {
        List<FullItemData> items = new ArrayList<>();
        for (EquippedItem equippedItem : itemIds) {
            FullItemData item = loadItem(equippedItem, detailedOutput);
            items.add(item);
        }
        return items;
    }

    public static FullItemData loadItem(EquippedItem equippedItem, boolean detailedOutput) {
        int id = equippedItem.itemId(), upgrade = equippedItem.upgradeStep();
        FullItemData item = loadItemBasic(id, upgrade);

        if (equippedItem.gems().length > 0) {
            StatBlock gemStat = GemData.process(equippedItem.gems(), item.shared.socketBonus(), item.shared.name());
            item = item.changeEnchant(gemStat);
        }

        if (detailedOutput) {
            if (expectedEnchant.contains(item.slot())) {
                if (equippedItem.enchant() != null) {
                    OutputText.println(id + ": " + item.toStringExtended() + " ENCHANT=" + equippedItem.enchant());
                } else {
                    OutputText.println(id + ": " + item.toStringExtended() + " MISSING EXPECTED ENCHANT");
                }
            } else if (equippedItem.enchant() != null) {
                OutputText.println(id + ": " + item.toStringExtended() + " UNEXPECTED ENCHANT UNEXPECTED ENCHANT=" + equippedItem.enchant());
            } else {
                OutputText.println(id + ": " + item.toStringExtended());
            }
            if (item.statBase.isEmpty()) {
                OutputText.println("MISSING STATS MISSING STATS MISSING STATS MISSING STATS");
            }
        }
        return item;
    }

    public static FullItemData loadItemBasic(int itemId, int upgradeLevel) {
        ItemCache itemCache = ItemCache.instance;
        FullItemData item = itemCache.get(itemId, upgradeLevel);
        if (item == null) {
            throw new RuntimeException("dont use this please, prefer wowsim data");
//            item = WowHead.fetchItem(itemId);
//            if (item != null) {
//                itemCache.put(item);
//                itemCache.cacheSave();
//            } else {
//                throw new RuntimeException("missing item " + itemId);
//            }

//            if (upgradeLevel != 0) {
//                if (item.isUpgradable()) {
//                    OutputText.println("INACCURATE-UPGRADE " + item.toStringExtended());
//                    item = ItemLevel.upgrade(item, upgradeLevel);
//                    itemCache.put(item);
//                } else {
//                    OutputText.println("CANNOT-UPGRADE " + item.toStringExtended());
//                }
//            }
        }
        if (item.slot() == SlotItem.Trinket) {
            item = Trinkets.updateTrinket(item);
        }
        // TODO trinket vs upgrade ordering?
        return item;
    }

    public static void defaultEnchants(EquipOptionsMap itemMap, ModelCombined model, boolean force) {
        itemMap.forEachValue(array -> ArrayUtil.mapInPlace(array, item -> defaultEnchants(item, model, force)));
    }

    public static FullItemData defaultEnchants(FullItemData item, ModelCombined model, boolean force) {
        if (item.slot() == SlotItem.Trinket) {
            return item;
        }

        if (!item.statEnchant.isEmpty() && !force) {
            return item;
        }

        SocketType[] socketSlots = item.shared.socketSlots();

        if (model.enchants().isBlacksmith() && (item.slot() == SlotItem.Wrist || item.slot() == SlotItem.Hand))
            socketSlots = socketSlots != null ? ArrayUtil.append(socketSlots, SocketType.General) : new SocketType[]{SocketType.General};
        else if (item.slot() == SlotItem.Belt)
            socketSlots = socketSlots != null ? ArrayUtil.append(socketSlots, SocketType.General) : new SocketType[]{SocketType.General};

        StatBlock total = StatBlock.empty;
        if (socketSlots != null) {
            int engineer = 0;
            for (SocketType type : socketSlots) {
                if (type == SocketType.Engineer) {
                    StatBlock value;
                    if (engineer == 0)
                        value = StatBlock.of(StatType.Haste, 600);
                    else if (engineer == 1)
                        value = StatBlock.of(StatType.Mastery, 600);
                    else if (engineer == 2)
                        value = StatBlock.of(StatType.Crit, 600);
                    else
                        throw new IllegalArgumentException("don't know what engineer gem to add");
                    total = total.plus(value);
                    engineer++;
                } else {
                    total = total.plus(model.gemChoice(type));
                }
            }
        }
        if (item.shared.socketBonus() != null) {
            total = total.plus(item.shared.socketBonus());
        }
        StatBlock enchant = model.standardEnchant(item.slot());
        if (enchant != null) {
            total = total.plus(enchant);
        }

        return item.changeEnchant(total);
    }
}
