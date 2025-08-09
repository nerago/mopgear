package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class ItemUtil {
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
        set.add(SlotItem.Weapon);
        set.add(SlotItem.Offhand);
        return set;
    }

    public static List<ItemData> loadItems(ItemCache itemCache, List<EquippedItem> itemIds, boolean detailedOutput) {
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
            StatBlock gemStat = GemData.process(equippedItem.gems());
            item = new ItemData(item.slot, item.name, item.stat, gemStat, id);
        }

        if (detailedOutput) {
            if (expectedEnchant.contains(item.slot) && equippedItem.enchant() == null) {
                System.out.println(id + ": " + item + " MISSING EXPECTED ENCHANT");
            } else {
                System.out.println(id + ": " + item);
            }
        }
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

    public static EnumMap<SlotEquip, ItemData> chosenItemsReforgedToMap(List<ItemData> items, Map<SlotEquip, Tuple.Tuple2<StatType, StatType>> presetForge) {
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
            if (ItemData.isSameEquippedItem(example, chosenItem)) {
                submitMap.put(slot, new ItemData[] { chosenItem });
            }
        }
    }

    static void buildJobWithSpecifiedItemsFixed(EnumMap<SlotEquip, ItemData> chosenMap, EnumMap<SlotEquip, ItemData[]> submitMap) {
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData chosenItem = chosenMap.get(slot);
            if (chosenItem != null) {
                submitMap.put(slot, new ItemData[]{chosenItem});
            }
        }
    }

    static void validateDualSets(Map<SlotEquip, ItemData[]> retMap, Map<SlotEquip, ItemData[]> protMap) {
        if (protMap.get(SlotEquip.Offhand) == null || protMap.get(SlotEquip.Offhand).length == 0)
            throw new IllegalArgumentException("no shield");
        if (retMap.get(SlotEquip.Offhand) != null)
            throw new IllegalArgumentException("unexpected shield");
        if (protMap.get(SlotEquip.Ring1)[0].id == retMap.get(SlotEquip.Ring2)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Ring2)[0].id == retMap.get(SlotEquip.Ring1)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Trinket1)[0].id == retMap.get(SlotEquip.Trinket2)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
        if (protMap.get(SlotEquip.Trinket2)[0].id == retMap.get(SlotEquip.Trinket1)[0].id)
            throw new IllegalArgumentException("duplicate in non matching slot");
    }

    static EnumMap<SlotEquip, ItemData[]> commonInDualSet(Map<SlotEquip, ItemData[]> retMap, Map<SlotEquip, ItemData[]> protMap) {
        EnumMap<SlotEquip, ItemData[]> common = new EnumMap<>(SlotEquip.class);
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] aaa = retMap.get(slot);
            ItemData[] bbb = protMap.get(slot);
            if (aaa == null || bbb == null || aaa.length == 0 || bbb.length == 0)
                continue;

            if (ItemData.isSameEquippedItem(aaa[0], bbb[0])) {
                System.out.println("COMMON " + aaa[0].name);

                ArrayList<ItemData> commonForges = new ArrayList<>();
                for (ItemData a : aaa) {
                    for (ItemData b : bbb) {
                        if (ItemData.isIdenticalItem(a, b))
                            commonForges.add(a);
                    }
                }
                common.put(slot, commonForges.toArray(ItemData[]::new));
            }
        }
        return common;
    }

    private static boolean hasNoDuplicate(CurryQueue<ItemData> items) {
        ItemData ring = null, trink = null;
        do {
            ItemData item = items.item();
            switch (item.slot) {
                case Ring -> {
                    if (ring == null)
                        ring = item;
                    else if (ring.id == item.id)
                        return false;
                }
                case Trinket -> {
                    if (trink == null)
                        trink = item;
                    else if (trink.id == item.id)
                        return false;
                }
            }
            items = items.tail();
        } while (items != null);
        return true;
    }

    public static void bestForgesOnly(EnumMap<SlotEquip, ItemData[]> itemMap, ModelCombined model) {
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] items = itemMap.get(slot);
            if (items != null) {
                ItemData[] bestByItemId = Arrays.stream(items)
                        .collect(Collectors.groupingBy(x -> x.id,
                                Collectors.maxBy(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())))))
                        .values().stream().map(Optional::orElseThrow)
                        .toArray(ItemData[]::new);
//                Optional<ItemData> best = Arrays.stream(items).max(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())));
                itemMap.put(slot, bestByItemId);
            }
        }
    }
}
