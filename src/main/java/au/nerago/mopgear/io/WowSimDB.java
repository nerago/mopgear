package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WowSimDB {
    private Map<ItemRef, ItemData> itemMap = new HashMap<>();

    // https://raw.githubusercontent.com/wowsims/mop/57251c327bbc745d1512b9c13e952f4bcf3deedb/assets/database/db.json

    public WowSimDB() {
        readInput(Objects.requireNonNull(getClass().getClassLoader().getResource("wowsimdb.json")));
    }

    public void readInput(URL url) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            convert(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void convert(JsonObject mainObject) {
        convertItems(mainObject.getAsJsonArray("items"));
//        mainObject.getAsJsonArray("enchants")
//        mainObject.getAsJsonArray("gems")
//        mainObject.getAsJsonArray("reforgeStats") // ids and stuff
    }

    private void convertItems(JsonArray itemsArray) {
        for (JsonElement element : itemsArray) {
            JsonObject object = element.getAsJsonObject();
            convertItem(object);
        }
    }

    private void convertItem(JsonObject object) {
        int id = object.get("id").getAsInt();
        String name = object.get("name").getAsString();
        int type = getIntOrDefault(object, "type", -1);
        if (type == -1)
            return;

        int weaponType = getIntOrDefault(object, "weaponType", 0);
        SlotItem slot = mapSlot(type, weaponType);
        JsonObject scalingOptions = object.getAsJsonObject("scalingOptions");
        for (Map.Entry<String, JsonElement> entry : scalingOptions.entrySet()) {
            JsonObject scaleEntry = entry.getValue().getAsJsonObject();
            int itemLevel = scaleEntry.get("ilvl").getAsInt();
            JsonObject stats = scaleEntry.getAsJsonObject("stats");
            StatBlock block = StatBlock.empty;
            if (stats != null) {
                for (Map.Entry<String, JsonElement> statEntry : stats.entrySet()) {
                    String statKey = statEntry.getKey();
                    int value = statEntry.getValue().getAsInt();
                    StatType statType = mapStat(statKey);
                    if (statType != null) {
                        block = block.withChange(statType, value);
                    }
                }
            }

            // TODO sockets
            ItemData item = ItemData.build(id, slot, name, block, new SocketType[0], 0, itemLevel);
            itemMap.put(item.ref, item);
        }
    }

    private static SlotItem mapSlot(int type, int weaponType) {
        switch (type) {
            case 1 -> { return SlotItem.Head; }
            case 2 -> { return SlotItem.Neck; }
            case 3 -> { return SlotItem.Offhand; }
            case 4 -> { return SlotItem.Back; }
            case 5 -> { return SlotItem.Chest; }
            case 6 -> { return SlotItem.Wrist; }
            case 7 -> { return SlotItem.Hand; }
            case 8 -> { return SlotItem.Belt; }
            case 9 -> { return SlotItem.Leg; }
            case 10 -> { return SlotItem.Foot; }
            case 11 -> { return SlotItem.Ring; }
            case 12 -> { return SlotItem.Trinket; }
            case 13 -> {
                switch (weaponType) {
                    case 1, 4, 6, 8, 9 -> { return SlotItem.Weapon2H; }
                    case 2, 3 -> { return SlotItem.Weapon1H; }
                    case 5, 7 -> { return SlotItem.Offhand; }
                    default -> throw new RuntimeException("unknown weapon " + weaponType);
                }
            }
            case 14 -> {
                switch (weaponType) {
                    case 0 -> { return SlotItem.Weapon2H; }
                    default -> throw new RuntimeException("unknown weapon " + weaponType);
                }
            }
            default -> throw new RuntimeException("unknown slot " + type);
        }
    }

    private static StatType mapStat(String statKey) {
        return switch (statKey) {
            case "0" -> StatType.Primary; // Strength
            case "1" -> StatType.Primary; // Agility
            case "2" -> StatType.Stam; // Stamina
            case "3" -> StatType.Primary; // Intellect
            case "4" -> StatType.Spirit; // Spirit
            case "5" -> StatType.Hit;
            case "6" -> StatType.Crit;
            case "7" -> StatType.Haste;
            case "8" -> StatType.Expertise;
            case "9" -> StatType.Dodge;
            case "10" -> StatType.Parry;
            case "11" -> StatType.Mastery;
            case "14" -> null; // Spell power
            case "15" -> null; // Pvp
            case "16" -> null; // Pvp
            case "17" -> null; // StatType.Armor;
            case "18" -> null; // bonus armor
            case "20" -> null; // Pvp + upgrade levels?
            default -> throw new RuntimeException("unknown stat " + statKey);
        };
    }

    private int getIntOrDefault(JsonObject object, String key, int defaultValue) {
        JsonElement entry = object.get(key);
        if (entry != null)
            return entry.getAsInt();
        else
            return defaultValue;
    }

    public ItemData lookup(ItemRef ref) {
        return itemMap.get(ref);
    }
}
