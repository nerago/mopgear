package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.InputGearParser;
import au.nerago.mopgear.model.GemData;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.ReforgeRules;
import au.nerago.mopgear.model.Trinkets;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.io.WowHead;
import au.nerago.mopgear.util.ArrayUtil;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
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

    public static EquipOptionsMap readAndLoad(ItemCache itemCache, boolean detailedOutput, Path file, ReforgeRules rules, Map<Integer, ReforgeRecipe> presetForge) {
        List<EquippedItem> itemIds = InputGearParser.readInput(file);
        List<ItemData> items = loadItems(itemCache, itemIds, detailedOutput);
        EquipOptionsMap result = presetForge != null
                ? limitedItemsReforgedToMap(rules, items, presetForge)
                : standardItemsReforgedToMap(rules, items);
        itemCache.cacheSave();
        return result;
    }

    public static void forceReload(ItemCache itemCache, Path file) {
        List<EquippedItem> itemIds = InputGearParser.readInput(file);
        for (EquippedItem equippedItem : itemIds) {
            int id = equippedItem.id();
            ItemData item = WowHead.fetchItem(id);
            itemCache.put(id, item);
        }
        itemCache.cacheSave();
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
            StatBlock gemStat = GemData.process(equippedItem.gems(), item.socketBonus, item.name);
            item = item.changeFixed(gemStat);
        }

        if (detailedOutput) {
            if (expectedEnchant.contains(item.slot)) {
                if (equippedItem.enchant() != null) {
                    OutputText.println(id + ": " + item + " ENCHANT=" + equippedItem.enchant());
                } else {
                    OutputText.println(id + ": " + item + " MISSING EXPECTED ENCHANT");
                }
            } else {
                OutputText.println(id + ": " + item);
            }
            if (item.stat.isEmpty()) {
                OutputText.println("MISSING STATS MISSING STATS MISSING STATS MISSING STATS");
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
                itemCache.cacheSave();
            } else {
                throw new RuntimeException("missing item " + id);
            }
        }
        if (item.slot == SlotItem.Trinket) {
            item = Trinkets.updateTrinket(item);
        }
        return item;
    }

    public static EquipOptionsMap standardItemsReforgedToMap(ReforgeRules rules, List<ItemData> items) {
        EquipOptionsMap map = EquipOptionsMap.empty();
        for (ItemData item : items) {
            SlotEquip slot = item.slot.toSlotEquip();
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

    public static EquipOptionsMap limitedItemsReforgedToMap(ReforgeRules rules, List<ItemData> items,
                                                            Map<Integer, ReforgeRecipe> presetForge) {
        EquipOptionsMap map = EquipOptionsMap.empty();
        for (ItemData item : items) {
            SlotEquip slot = item.slot.toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.has(slot)) {
                slot = SlotEquip.Ring2;
            } else if (slot == SlotEquip.Trinket1 && map.has(slot)) {
                slot = SlotEquip.Trinket2;
            }
            if (presetForge.containsKey(item.id)) {
                ItemData forged = Reforger.presetReforge(item, presetForge.get(item.id));
                map.put(slot, new ItemData[]{forged});
            } else {
                map.put(slot, Reforger.reforgeItem(rules, item));
            }
        }
        return map;
    }

    public static EquipMap chosenItemsReforgedToMap(List<ItemData> items, Map<SlotEquip, ReforgeRecipe> presetForge) {
        EquipMap map = EquipMap.empty();
        for (ItemData item : items) {
            SlotEquip slot = item.slot.toSlotEquip();
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

    static void validateProt(EquipOptionsMap protMap) {
        if (protMap.get(SlotEquip.Offhand) == null || protMap.get(SlotEquip.Offhand).length == 0)
            throw new IllegalArgumentException("no shield");
    }

    static void validateRet(EquipOptionsMap retMap) {
        if (retMap.get(SlotEquip.Offhand) != null)
            throw new IllegalArgumentException("unexpected shield");
    }

    static void validateDualSets(List<EquipOptionsMap> mapsParam) {
        Map<Integer, SlotEquip> seen = new HashMap<>();
        for (EquipOptionsMap map : mapsParam) {
            map.forEachPair((slot, array) -> {
                for (ItemData item : array) {
                    int itemId = item.id;
                    SlotEquip val = seen.get(itemId);
                    if (val == null) {
                        seen.put(itemId, slot);
                    } else if (val != slot) {
                        throw new IllegalArgumentException("duplicate in non matching slot");
                    }
                }
            });
        }
    }

    public static void bestForgesOnly(EquipOptionsMap itemMap, ModelCombined model) {
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] items = itemMap.get(slot);
            if (items != null) {
                ItemData[] bestByItemId = Arrays.stream(items)
                        .collect(Collectors.groupingBy(it -> it.id,
                                Collectors.maxBy(Comparator.comparingLong(model::calcRating))))
                        .values().stream().map(Optional::orElseThrow)
                        .toArray(ItemData[]::new);
//                Optional<ItemData> best = Arrays.stream(items).max(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())));
                itemMap.put(slot, bestByItemId);
            }
        }
    }

    public static List<ItemData> onlyMatchingForge(List<ItemData> forgeList, ReforgeRecipe recipe) {
        if (recipe == null || recipe.isNull()) {
            for (ItemData item : forgeList) {
                if (item.reforge == null || item.reforge.isNull())
                    return List.of(item);
            }
        } else {
            for (ItemData item : forgeList) {
                if (recipe.equalsTyped(item.reforge))
                    return List.of(item);
            }
        }
        throw new IllegalArgumentException("specified forge not found " + forgeList.getFirst().id + " " + recipe);
    }

    public static void defaultEnchants(EquipOptionsMap itemMap, ModelCombined model, boolean force) {
        itemMap.forEachValue(array -> ArrayUtil.mapInPlace(array, item -> defaultEnchants(item, model, force)));
    }

    public static ItemData defaultEnchants(ItemData item, ModelCombined model, boolean force) {
        if (force || item.statFixed.isEmpty()) {
            SocketType[] socketSlots = item.socketSlots;

            // TODO blacksmith only
            if (item.slot == SlotItem.Wrist || item.slot == SlotItem.Hand)
                socketSlots = socketSlots != null ? ArrayUtil.append(socketSlots, SocketType.General) : new SocketType[]{SocketType.General};
            else if (item.slot == SlotItem.Belt)
                socketSlots = socketSlots != null ? ArrayUtil.append(socketSlots, SocketType.General) : new SocketType[]{SocketType.General};

            StatBlock total = StatBlock.empty;
            if (socketSlots != null) {
                for (SocketType type : socketSlots) {
                    total = total.plus(model.gemChoice(type));
                }
            }
            if (item.socketBonus != 0) {
                StatBlock bonus = GemData.getSocketBonus(item);
                total = total.plus(bonus);
            }
            StatBlock enchant = model.standardEnchant(item.slot);
            if (enchant != null) {
                total = total.plus(enchant);
            }

            return item.changeFixed(total);
        } else {
            return item;
        }
    }

    static long estimateSets(EquipOptionsMap reforgedItems) {
        return reforgedItems.entryStream().mapToLong(x -> (long) x.b().length).reduce((a, b) -> a * b).orElse(0);
    }

    public static long estimateSets(List<SolverCapPhased.SkinnyItem[]> skinnyOptions) {
        return skinnyOptions.stream().mapToLong(x -> (long) x.length).reduce((a, b) -> a * b).orElse(0);
    }

    public static long estimateSets(Map<Integer, List<ItemData>> commonMap) {
        return commonMap.values().stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
    }
}
