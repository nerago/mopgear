package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.Tuple;

import java.io.IOException;
import java.util.*;

public class ItemUtil {
    public static List<ItemData> loadItems(ItemCache itemCache, List<EquippedItem> itemIds, boolean detailedOutput) throws IOException {
        List<ItemData> items = new ArrayList<>();
        for (EquippedItem equippedItem : itemIds) {
            ItemData item = loadItem(itemCache, equippedItem, detailedOutput);
            items.add(item);
        }
        return items;
    }

    public static ItemData loadItem(ItemCache itemCache, EquippedItem equippedItem, boolean detailedOutput) {
        int id = equippedItem.id();
        ItemData item = loadItemBasic(itemCache, id);

        if (equippedItem.gems().length > 0) {
            StatBlock gemStat = GemData.process(equippedItem.gems(), item.slot);
            item = new ItemData(item.slot, item.name, item.stat, gemStat, id);
        }

        System.out.println(id + ": " + item);
        return item;
    }

    public static ItemData loadItemBasic(ItemCache itemCache, int id) {
        ItemData item = itemCache.get(id);
        if (item == null) {
            item = WowHead.fetchItem(id);
            if (item != null) {
                itemCache.put(id, item);
            } else {
                throw new RuntimeException("missing item");
            }
        }
        return item;
    }

    public static EnumMap<SlotEquip, ItemData[]> standardItemsReforgedToMap(ReforgeRules rules, List<ItemData> items) {
        EnumMap<SlotEquip, ItemData[]> map = new EnumMap<>(SlotEquip.class);
        for (ItemData item : items) {
            SlotEquip slot = item.slot.toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.containsKey(slot)) {
                map.put(SlotEquip.Ring2, Reforger.reforgeItem(rules, item));
            } else if (slot == SlotEquip.Trinket1 && map.containsKey(slot)) {
                map.put(SlotEquip.Trinket2, Reforger.reforgeItem(rules, item));
            } else {
                map.put(slot, Reforger.reforgeItem(rules, item));
            }
        }
        return map;
    }

    public static EnumMap<SlotEquip, ItemData[]> limitedItemsReforgedToMap(ReforgeRules rules, List<ItemData> items,
                                                                       Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> presetForge) {
        EnumMap<SlotEquip, ItemData[]> map = new EnumMap<>(SlotEquip.class);
        for (ItemData item : items) {
            SlotEquip slot = item.slot.toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.containsKey(slot)) {
                slot = SlotEquip.Ring2;
            } else if (slot == SlotEquip.Trinket1 && map.containsKey(slot)) {
                slot = SlotEquip.Trinket2;
            }
            if (presetForge.containsKey(slot)) {
                ItemData forged = Reforger.presetReforge(item, presetForge.get(slot));
                map.put(slot, new ItemData[] { forged });
            } else {
                map.put(slot, Reforger.reforgeItem(rules, item));
            }
        }
        return map;
    }

    public static EnumMap<SlotEquip, ItemData> chosenItemsReforgedToMap(ModelCombined modelRet, List<ItemData> items, Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> presetForge) {
        EnumMap<SlotEquip, ItemData> map = new EnumMap<>(SlotEquip.class);
        for (ItemData item : items) {
            SlotEquip slot = item.slot.toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.containsKey(slot)) {
                slot = SlotEquip.Ring2;
            } else if (slot == SlotEquip.Trinket1 && map.containsKey(slot)) {
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

    static void buildJobWithCommonItemsFixed(EnumMap<SlotEquip, ItemData> chosenMap, EnumMap<SlotEquip, ItemData[]> submitMap) {
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData chosenItem = chosenMap.get(slot);
            ItemData[] slotOptions = submitMap.get(slot);
            if (chosenItem == null || slotOptions == null || slotOptions.length == 0)
                continue;

            ItemData example = slotOptions[0];
            if (example.id == chosenItem.id && example.statFixed.equalsStats(chosenItem.statFixed)) {
                submitMap.put(slot, new ItemData[] { chosenItem });
            }
        }
    }

    static void validateDualSets(Map<SlotEquip, ItemData[]> retMap, Map<SlotEquip, ItemData[]> protMap) {
        if (protMap.get(SlotEquip.Offhand) == null || protMap.get(SlotEquip.Offhand).length == 0)
            throw new IllegalArgumentException("no shield");
        if (protMap.get(SlotEquip.Ring1)[0].id == retMap.get(SlotEquip.Ring2)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Ring2)[0].id == retMap.get(SlotEquip.Ring1)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Trinket1)[0].id == retMap.get(SlotEquip.Trinket2)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Trinket2)[0].id == retMap.get(SlotEquip.Trinket1)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
    }
}
