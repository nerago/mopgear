package au.nicholas.hardy.mopgear;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ItemUtil {
    public static List<ItemData> loadItems(ItemCache itemCache, List<EquippedItem> itemIds) throws IOException {
        List<ItemData> items = new ArrayList<>();
        for (EquippedItem equippedItem : itemIds) {
            int id = equippedItem.id;
            ItemData item = itemCache.get(id);
            if (item != null) {
                items.add(item);
                System.out.println(id + ": " + item + " with " + equippedItem.enchant);
            } else {
                item = WowHead.fetchItem(id);
                if (item != null) {
                    items.add(item);
                    itemCache.put(id, item);
                } else {
                    throw new RuntimeException("missing item");
                }
            }
        }
        return items;
    }

    public static Map<SlotEquip, List<ItemData>> standardItemsToMap(List<ItemData> items) {
        Map<SlotEquip, List<ItemData>> map = new EnumMap<>(SlotEquip.class);
        for (ItemData item : items) {
            SlotEquip slot = item.slot.toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.containsKey(slot)) {
                map.put(SlotEquip.Ring2, Reforge.reforgeItem(item));
            } else if (slot == SlotEquip.Trinket1 && map.containsKey(slot)) {
                map.put(SlotEquip.Trinket2, Reforge.reforgeItem(item));
            } else {
                map.put(slot, Reforge.reforgeItem(item));
            }
        }
        return map;
    }
}
