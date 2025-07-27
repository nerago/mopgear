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
            ItemData item = loadItem(itemCache, equippedItem);
            items.add(item);
        }
        return items;
    }

    public static ItemData loadItem(ItemCache itemCache, EquippedItem equippedItem) throws IOException {
        int id = equippedItem.id();
        ItemData item = loadItemBasic(itemCache, id);

        if (equippedItem.gems().length > 0) {
            StatBlock gemStat = GemData.process(equippedItem.gems(), item.slot);
            item = new ItemData(item.slot, item.name, item.stat, gemStat);
        }

        System.out.println(id + ": " + item + " with " + equippedItem.enchant());
        return item;
    }

    public static ItemData loadItemBasic(ItemCache itemCache, int id) throws IOException {
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
