package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.InputGearParser;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.io.WowHead;
import au.nerago.mopgear.io.WowSimDB;
import au.nerago.mopgear.model.*;
import au.nerago.mopgear.process.Reforger;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

import static au.nerago.mopgear.domain.SocketType.*;

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

    public static EquipOptionsMap readAndLoad(Path file, ModelCombined model, Map<Integer, List<ReforgeRecipe>> presetForge, boolean detailedOutput) {
        return readAndLoad(file, model.reforgeRules(), model.enchants(), presetForge, detailedOutput);
    }

    public static EquipOptionsMap readAndLoad(Path file, ReforgeRules reforge, DefaultEnchants enchants, Map<Integer, List<ReforgeRecipe>> presetForge, boolean detailedOutput) {
        List<EquippedItem> itemIds = InputGearParser.readInput(file);
        List<FullItemData> items = loadItems(itemIds, enchants, detailedOutput);
        EquipOptionsMap result = presetForge != null
                ? ItemMapUtil.limitedItemsReforgedToMap(reforge, items, presetForge)
                : ItemMapUtil.standardItemsReforgedToMap(reforge, items);
        ItemCache.instance.cacheSave();
        return result;
    }

    public static EquipMap readAndLoadExistingForge(Path file, DefaultEnchants enchants) {
        EquipMap map = EquipMap.empty();

        List<EquippedItem> itemIds = InputGearParser.readInput(file);
        for (EquippedItem equippedItem : itemIds) {
            FullItemData item = loadItem(equippedItem, enchants, false);

            ReforgeRecipe forge = null;
            if (equippedItem.reforging() != 0) {
                forge = WowSimDB.instance.reforgeId(equippedItem.reforging());
            }
            item = Reforger.presetReforge(item, forge);

            SlotEquip slot = item.slot().toSlotEquip();
            if (slot == SlotEquip.Ring1 && map.has(slot)) {
                slot = SlotEquip.Ring2;
            } else if (slot == SlotEquip.Trinket1 && map.has(slot)) {
                slot = SlotEquip.Trinket2;
            } else if (map.has(slot)) {
                throw new IllegalArgumentException("duplicate item");
            }

            map.put(slot, item);
        }

        return map;
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

    public static List<FullItemData> loadItems(List<EquippedItem> itemIds, DefaultEnchants enchants, boolean detailedOutput) {
        List<FullItemData> items = new ArrayList<>();
        for (EquippedItem equippedItem : itemIds) {
            FullItemData item = loadItem(equippedItem, enchants, detailedOutput);
            items.add(item);
        }
        return items;
    }

    public static FullItemData loadItem(EquippedItem equippedItem, DefaultEnchants enchants, boolean detailedOutput) {
        int id = equippedItem.itemId(), upgrade = equippedItem.upgradeStep();
        FullItemData item = loadItemBasic(id, upgrade);

        int[] equippedGems = equippedItem.gems();
        @NotNull SocketType[] socketSlots = item.shared.socketSlots();
        if (equippedGems.length > 0) {
            boolean possibleBlacksmith = item.slot().possibleBlacksmith();
            if (equippedGems.length != socketSlots.length && !possibleBlacksmith) {
                throw new IllegalArgumentException(id + ": " + item.toStringExtended() + " MISSING GEM MISSING GEM MISSING GEM");
//                OutputText.println(id + ": " + item.toStringExtended() + " MISSING GEM MISSING GEM MISSING GEM");
//                // TODO clean this up, was an empty socket on export
//                while (equippedGems.length < socketSlots.length) {
//                    equippedGems = ArrayUtil.append(equippedGems, 76699);
//                }
            }

            Tuple.Tuple2<StatBlock, List<StatBlock>> gemInfo = GemData.process(equippedGems, equippedItem.enchant(), socketSlots, item.shared.socketBonus(), item.shared.name(), possibleBlacksmith);
            item = item.changeEnchant(gemInfo.a(), gemInfo.b(), equippedItem.enchant());
        }

        if (id == 94820 && equippedItem.randomSuffix() == -336) {
            item = item.changeStatsBase(item.statBase.plus(StatBlock.of(StatType.Crit, 882)));
        }

        if (detailedOutput) {
            if (expectedEnchant.contains(item.slot())) {
                if (equippedItem.enchant() != null) {
                    StatBlock standardEnchant = enchants.standardEnchant(item.slot());
                    StatBlock actualEnchant = GemData.getEnchant(equippedItem.enchant());
                    if (standardEnchant == null || standardEnchant.equalsStats(actualEnchant)) {
                        OutputText.println(id + ": " + item.toStringExtended() + " ENCHANTED");
                    } else {
                        OutputText.println(id + ": " + item.toStringExtended() + " ENCHANT WRONG ENCHANT WRONG ENCHANT WRONG " + equippedItem.enchant());
                    }
                } else {
                    OutputText.println(id + ": " + item.toStringExtended() + " MISSING EXPECTED ENCHANT MISSING MISSING MISSING");
                }
            } else if (equippedItem.enchant() != null) {
                OutputText.println(id + ": " + item.toStringExtended() + " UNEXPECTED ENCHANT UNEXPECTED ENCHANT UNEXPECTED ENCHANT " + equippedItem.enchant());
            } else {
                OutputText.println(id + ": " + item.toStringExtended());
            }
            if (item.statBase.isEmpty()) {
                OutputText.println("MISSING STATS MISSING STATS MISSING STATS MISSING STATS");
            }
        }
        return item;
    }

    public static List<FullItemData> loadItemBasicWithRandomVariants(int itemId, int upgradeLevel) {
        FullItemData item = loadItemBasic(itemId, upgradeLevel);
        return switch (itemId) {
            // Caustic Spike Bracers
            case 95732 -> itemWithRandomVariants(item, 712);
            case 94820 -> itemWithRandomVariants(item, 858);
            case 96104 -> itemWithRandomVariants(item, 907);
            case 96476 -> itemWithRandomVariants(item, 968);
            case 96848 -> itemWithRandomVariants(item, 1020); // no value source (others random wowhead comment at least)
            default -> Collections.singletonList(item);
        };
    }

    private static List<FullItemData> itemWithRandomVariants(FullItemData item, int statValue) {
        List<FullItemData> result = new ArrayList<>();
        result.add(itemWithRandomVariants(item, StatType.Crit, statValue));
        result.add(itemWithRandomVariants(item, StatType.Haste, statValue));
        result.add(itemWithRandomVariants(item, StatType.Mastery, statValue));
        result.add(itemWithRandomVariants(item, StatType.Expertise, statValue));
        result.add(itemWithRandomVariants(item, StatType.Hit, statValue));
        return result;
    }

    private static FullItemData itemWithRandomVariants(FullItemData item, StatType statType, int statValue) {
        return item.changeStatsBase(item.statBase.withChange(statType, statValue)).changeName(item.shared.name() + " of " + statType);
    }

    public static FullItemData loadItemBasic(int itemId, int upgradeLevel) {
        ItemCache itemCache = ItemCache.instance;
        FullItemData item = itemCache.get(itemId, upgradeLevel);
        if (item == null) {
            if (upgradeLevel > 0) {
                item = itemCache.get(itemId, 0);
                if (item != null) {
                    OutputText.println("DOWNGRADE SUBSTITUTED BASE ITEM " + item.toStringExtended());
                } else {
                    throw new RuntimeException("item " + itemId + " not found in wowsim data, even downgraded");
                }
            } else {
                throw new RuntimeException("item " + itemId + " not found in wowsim data");
            }
//            throw new RuntimeException("dont use this please, prefer wowsim data");
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

    public static void defaultEnchants(EquipOptionsMap itemMap, ModelCombined model, boolean force, boolean alternateGem) {
        itemMap.forEachValue(array -> ArrayUtil.mapInPlace(array, item -> defaultEnchants(item, model, force, alternateGem)));
    }

    public static FullItemData defaultEnchants(FullItemData item, ModelCombined model, boolean force, boolean alternateGem) {
        int itemId = item.itemId();

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

        boolean socketBonusMet = true;
        StatBlock total = StatBlock.empty;
        List<StatBlock> gemChoice = new ArrayList<>();
        if (socketSlots != null) {
            int engineer = 0;
            for (SocketType type : socketSlots) {
                StatBlock value;
                if (type == SocketType.Engineer) {
                    if (engineer == 0)
                        value = StatBlock.of(StatType.Haste, 600);
                    else if (engineer == 1)
                        value = StatBlock.of(StatType.Mastery, 600);
                    else if (engineer == 2)
                        value = StatBlock.of(StatType.Crit, 600);
                    else
                        throw new IllegalArgumentException("don't know what engineer gem to add");
                    engineer++;
                } else if (alternateGem && (type == Red || type == Blue || type == Yellow)) {
                    value = model.gemChoiceBestAlternate();
                } else {
                    value = model.gemChoice(type);
                }

                gemChoice.add(value);
                if (!GemData.matchesSocket(type, value)) {
                    socketBonusMet = false;
                }

                total = total.plus(value);
            }
        }

        if (item.shared.socketBonus() != null && socketBonusMet) {
            total = total.plus(item.shared.socketBonus());
        }

        StatBlock enchant = model.standardEnchant(item.slot());
        Integer enchantId = null;
        if (enchant != null) {
            total = total.plus(enchant);
            enchantId = GemData.reverseLookup(enchant, item.shared.primaryStatType());
        }

        return item.changeEnchant(total, gemChoice, enchantId);
    }

    public static void duplicateAlternateEnchants(EquipOptionsMap items, ModelCombined model) {
        HashSet<FullItemData> resultingList = new HashSet<>();
        for (SlotEquip slot : SlotEquip.values()) {
            FullItemData[] options = items.get(slot);
            if (options != null) {
                HashSet<FullItemData> updated = new HashSet<>();
                for (FullItemData item : options) {
                    updated.add(item);
                    updated.add(defaultEnchants(item, model, true, false));
                    updated.add(defaultEnchants(item, model, true, true));
                }
                items.put(slot, updated.toArray(FullItemData[]::new));
            }
        }
    }
}
