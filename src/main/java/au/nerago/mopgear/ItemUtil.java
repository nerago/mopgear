package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.InputGearParser;
import au.nerago.mopgear.model.*;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.io.WowHead;
import au.nerago.mopgear.util.ArrayUtil;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;

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
        set.add(SlotItem.WeaponOneHand);
        set.add(SlotItem.WeaponTwoHand);
        set.add(SlotItem.Offhand);
        return set;
    }

    public static EquipOptionsMap readAndLoad(ItemCache itemCache, boolean detailedOutput, Path file, ReforgeRules rules, Map<Integer, List<ReforgeRecipe>> presetForge) {
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
        int id = equippedItem.id(), upgrade = equippedItem.upgradeStep();
        ItemData item = loadItemBasic(itemCache, id, upgrade);

        if (equippedItem.gems().length > 0) {
            StatBlock gemStat = GemData.process(equippedItem.gems(), item.socketBonus, item.name);
            item = item.changeFixed(gemStat);
        }

        if (detailedOutput) {
            if (expectedEnchant.contains(item.slot)) {
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
            if (item.stat.isEmpty()) {
                OutputText.println("MISSING STATS MISSING STATS MISSING STATS MISSING STATS");
            }
        }
        return item;
    }

    public static ItemData loadItemBasic(ItemCache itemCache, int itemId, int upgradeLevel) {
        ItemData item = itemCache.get(itemId);
        if (item == null) {
            item = WowHead.fetchItem(itemId);
            if (item != null) {
                itemCache.put(itemId, item);
                itemCache.cacheSave();
            } else {
                throw new RuntimeException("missing item " + itemId);
            }
        }
        if (item.slot == SlotItem.Trinket) {
            item = Trinkets.updateTrinket(item);
        }
        item = ItemLevel.upgrade(item, upgradeLevel);
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
                                                            Map<Integer, List<ReforgeRecipe>> presetForge) {
        EquipOptionsMap map = EquipOptionsMap.empty();
        for (ItemData item : items) {
            SlotEquip slot = item.slot.toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.has(slot)) {
                slot = SlotEquip.Ring2;
            } else if (slot == SlotEquip.Trinket1 && map.has(slot)) {
                slot = SlotEquip.Trinket2;
            } else if (map.has(slot)) {
                throw new IllegalArgumentException("duplicate item");
            }

            if (presetForge.containsKey(item.ref.itemId())) {
                ItemData[] forged = presetForge.get(item.ref.itemId()).stream()
                        .map(preset -> Reforger.presetReforge(item, preset))
                        .toArray(ItemData[]::new);
                map.put(slot, forged);
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

    static void validateMultiSetAlignItemSlots(List<EquipOptionsMap> mapsParam) {
        Map<Integer, SlotEquip> seen = new HashMap<>();
        for (EquipOptionsMap map : mapsParam) {
            map.forEachPair((slot, array) -> {
                for (ItemData item : array) {
                    int itemId = item.ref.itemId();
                    SlotEquip val = seen.get(itemId);
                    if (val == null) {
                        seen.put(itemId, slot);
                    } else if (val != slot) {
                        throw new IllegalArgumentException("duplicate in non matching slot " + item);
                    }
                }
            });
        }
    }

    public static List<ItemData> onlyMatchingForge(List<ItemData> forgeList, ReforgeRecipe recipe) {
        if (recipe == null || recipe.isEmpty()) {
            for (ItemData item : forgeList) {
                if (item.reforge == null || item.reforge.isEmpty())
                    return List.of(item);
            }
        } else {
            for (ItemData item : forgeList) {
                if (recipe.equalsTyped(item.reforge))
                    return List.of(item);
            }
        }
        throw new IllegalArgumentException("specified forge not found " + forgeList.getFirst() + " " + recipe);
    }

    public static void defaultEnchants(EquipOptionsMap itemMap, ModelCombined model, boolean force) {
        itemMap.forEachValue(array -> ArrayUtil.mapInPlace(array, item -> defaultEnchants(item, model, force)));
    }

    public static ItemData defaultEnchants(ItemData item, ModelCombined model, boolean force) {
        if (force || item.statFixed.isEmpty()) {
            SocketType[] socketSlots = item.socketSlots;

            if (model.enchants().isBlacksmith() && (item.slot == SlotItem.Wrist || item.slot == SlotItem.Hand))
                socketSlots = socketSlots != null ? ArrayUtil.append(socketSlots, SocketType.General) : new SocketType[]{SocketType.General};
            else if (item.slot == SlotItem.Belt)
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

    static BigInteger estimateSets(EquipOptionsMap reforgedItems) {
        Optional<BigInteger> number = reforgedItems.entryStream().map(x -> BigInteger.valueOf(x.b().length)).reduce(BigInteger::multiply);
        if (number.isPresent()) {
            return number.get();
        } else {
            throw new RuntimeException("unable to determine item combination estimate");
        }
//        return reforgedItems.entryStream().mapToLong(x -> (long) x.b().length).reduce((a, b) -> a * b).orElse(0);
    }

    public static long estimateSets(List<SolverCapPhased.SkinnyItem[]> skinnyOptions) {
        return skinnyOptions.stream().mapToLong(x -> (long) x.length).reduce((a, b) -> a * b).orElse(0);
    }

    public static <X, T> long estimateSets(Map<X, List<T>> commonMap) {
        return commonMap.values().stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
    }
}
