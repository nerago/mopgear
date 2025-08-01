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

    public static EnumMap<SlotEquip, List<ItemData>> standardItemsReforgedToMap(ReforgeRules rules, List<ItemData> items) {
        EnumMap<SlotEquip, List<ItemData>> map = new EnumMap<>(SlotEquip.class);
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

    public static Map<SlotEquip, List<ItemData>> limitedItemsReforgedToMap(ReforgeRules rules, List<ItemData> items,
                                                                           Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> presetForge) {
        Map<SlotEquip, List<ItemData>> map = new EnumMap<>(SlotEquip.class);
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
                map.put(slot, Reforger.reforgeItem(rules, item));
            }
        }
        return map;
    }
}
