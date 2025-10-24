package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ItemLevel;
import au.nerago.mopgear.model.ReforgeRules;
import au.nerago.mopgear.process.Reforger;
import au.nerago.mopgear.util.ArrayUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ItemMapUtil {
    public static EquipOptionsMap standardItemsReforgedToMap(ReforgeRules rules, List<FullItemData> items) {
        EquipOptionsMap map = EquipOptionsMap.empty();
        for (FullItemData item : items) {
            SlotEquip slot = item.slot().toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.has(slot)) {
                map.put(SlotEquip.Ring2, Reforger.reforgeItem(rules, item));
            } else if (slot == SlotEquip.Trinket1 && map.has(slot)) {
                map.put(SlotEquip.Trinket2, Reforger.reforgeItem(rules, item));
            } else {
                map.put(slot, Reforger.reforgeItem(rules, item));
            }
        }
        return map;
    }

    public static EquipOptionsMap limitedItemsReforgedToMap(ReforgeRules rules, List<FullItemData> items,
                                                            Map<Integer, List<ReforgeRecipe>> presetForge) {
        EquipOptionsMap map = EquipOptionsMap.empty();
        for (FullItemData item : items) {
            SlotEquip slot = item.slot().toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.has(slot)) {
                slot = SlotEquip.Ring2;
            } else if (slot == SlotEquip.Trinket1 && map.has(slot)) {
                slot = SlotEquip.Trinket2;
            } else if (map.has(slot)) {
                throw new IllegalArgumentException("duplicate item");
            }

            if (presetForge.containsKey(item.itemId())) {
                FullItemData[] forged = presetForge.get(item.itemId()).stream()
                        .map(preset -> Reforger.presetReforge(item, preset))
                        .toArray(FullItemData[]::new);
                map.put(slot, forged);
            } else {
                map.put(slot, Reforger.reforgeItem(rules, item));
            }
        }
        return map;
    }

    public static EquipMap chosenItemsReforgedToMap(List<FullItemData> items, Map<SlotEquip, ReforgeRecipe> presetForge) {
        EquipMap map = EquipMap.empty();
        for (FullItemData item : items) {
            SlotEquip slot = item.slot().toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.has(slot)) {
                slot = SlotEquip.Ring2;
            } else if (slot == SlotEquip.Trinket1 && map.has(slot)) {
                slot = SlotEquip.Trinket2;
            }
            if (presetForge.containsKey(slot)) {
                map.put(slot, Reforger.presetReforge(item, presetForge.get(slot)));
            } else {
                throw new IllegalArgumentException("not specified reforge for " + slot);
            }
        }
        return map;
    }

    public static EquipOptionsMap upgradeAllTo2(EquipOptionsMap baseItems) {
        EquipOptionsMap result = EquipOptionsMap.empty();
        baseItems.forEachPair((slot, itemArray) ->
                result.put(slot, ArrayUtil.mapAsNew(itemArray, ItemMapUtil::upgradeItemTo2, FullItemData[]::new)));
        return result;
    }

    private static FullItemData upgradeItemTo2(FullItemData oldItem) {
        if (!oldItem.isUpgradable() || oldItem.ref().upgradeLevel() == ItemLevel.MAX_UPGRADE_LEVEL) {
            return oldItem;
        }

        FullItemData loaded = ItemLoadUtil.loadItemBasic(oldItem.itemId(), ItemLevel.MAX_UPGRADE_LEVEL);
        loaded = Reforger.presetReforge(loaded, oldItem.reforge);
        return loaded.changeEnchant(oldItem.statEnchant);
    }

    public static Function<SolvableItem, FullItemData> mapperToFullItems(EquipOptionsMap map) {
        return solvableItem -> {
            if (solvableItem != null) {
                ItemRef ref = new ItemRef(solvableItem.itemId(), solvableItem.itemLevel(), solvableItem.itemLevelBase(), solvableItem.duplicateNum());
                Optional<FullItemData> itemData = map.itemStream()
                        .filter(it -> it.isIdenticalItem(ref, solvableItem.totalCap(), solvableItem.totalRated()))
                        .findFirst();
                return itemData.orElseThrow();
            } else {
                return null;
            }
        };
    }
}
