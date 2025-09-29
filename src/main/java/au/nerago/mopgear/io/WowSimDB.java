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
import java.util.stream.Stream;

public class WowSimDB {
    private final Map<ItemRef, ItemData> itemMap = new HashMap<>();
    private final Map<ReforgeRecipe, Integer> reforgeIds = new HashMap<>();

    // https://raw.githubusercontent.com/wowsims/mop/57251c327bbc745d1512b9c13e952f4bcf3deedb/assets/database/db.json

    public static WowSimDB instance = new WowSimDB();

    private WowSimDB() {
        readInput(Objects.requireNonNull(getClass().getClassLoader().getResource("wowsimdb.json")));
    }

    private void readInput(URL url) {
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
        convertReforge(mainObject.getAsJsonArray("reforgeStats"));
    }

    private void convertItems(JsonArray itemsArray) {
        for (JsonElement element : itemsArray) {
            JsonObject object = element.getAsJsonObject();
            convertItem(object);
        }
    }

    private void convertReforge(JsonArray reforgeStats) {
        for (JsonElement element : reforgeStats) {
            JsonObject object = element.getAsJsonObject();
            int id = object.get("id").getAsInt();
            String from = object.get("fromStat").getAsString();
            String to = object.get("toStat").getAsString();
            StatType fromStat = mapStat(from);
            StatType toStat = mapStat(to);
            ReforgeRecipe reforge = new ReforgeRecipe(fromStat, toStat);
            reforgeIds.put(reforge, id);
        }
    }

    private void convertItem(JsonObject object) {
        int id = object.get("id").getAsInt();
        String name = object.get("name").getAsString();
        int type = getIntOrDefault(object, "type", -1);
        if (type == -1)
            return;

        int weaponType = getIntOrDefault(object, "weaponType", 0);
        int handType = getIntOrDefault(object, "handType", 0);
        SlotItem slot = mapSlot(type, weaponType, handType);

        SocketType[] sockets = new SocketType[0];
        JsonArray gemSockets = object.getAsJsonArray("gemSockets");
        if (gemSockets != null) {
            sockets = gemSockets.asList().stream()
                    .map(e -> mapSocket(e.getAsInt()))
                    .toArray(SocketType[]::new);
        }

        JsonArray socketBonus = object.getAsJsonArray("socketBonus");
        StatBlock socketBonusBlock = null;
        if (socketBonus != null) {
            socketBonusBlock = convertBlock(socketBonus);
        }

        JsonObject scalingOptions = object.getAsJsonObject("scalingOptions");
        int baseItemLevel = scalingOptions.get("0").getAsJsonObject().get("ilvl").getAsInt();
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

            ItemRef ref = ItemRef.buildAdvanced(id, itemLevel, baseItemLevel);
            ItemData item = ItemData.buildFromWowSim(ref, slot, name, block, sockets, socketBonusBlock);
            itemMap.put(item.ref, item);
        }
    }

    private StatBlock convertBlock(JsonArray array) {
        StatType type = null;
        int value = 0;
        for (int index = 0; index < array.size(); ++index) {
            int slot = array.get(index).getAsInt();
            if (slot != 0) {
                if (type != null)
                    throw new RuntimeException("unexpected second value");
                type = blockIndexToStat(index);
                value = slot;
            }
        }
        if (type == null)
            return null;
        return StatBlock.of(type, value);
    }

    private StatType blockIndexToStat(int index) {
        switch (index) {
            case 0, 1, 3 -> { return StatType.Primary; }
            case 2 -> { return StatType.Stam; }
            case 4 -> {return StatType.Spirit;}
            case 5 -> {return StatType.Hit;}
            case 6 -> {return StatType.Crit;}
            case 7 -> { return StatType.Haste;}
            case 8 -> {return StatType.Expertise;}
            case 9 -> {return StatType.Dodge;}
            case 10 -> {return StatType.Parry;}
            case 11 -> {return StatType.Mastery;}
            case 15, 16 -> {return null; }//pvp
            default -> throw new RuntimeException("unknown stat index " + index);
        }
    }

    private SocketType mapSocket(int num) {
        switch (num) {
            case 1: return SocketType.Meta;
            case 2: return SocketType.Red;
            case 3: return SocketType.Blue;
            case 4: return SocketType.Yellow;
            case 8: return SocketType.General; // flagging for a possible belt socket
            case 9: return SocketType.Engineer;
            case 10: return SocketType.Sha;
            default: throw new RuntimeException("unknown socket " + num);
        }
    }

    private static SlotItem mapSlot(int type, int weaponType, int handType) {
        switch (type) {
            case 1 -> { return SlotItem.Head; }
            case 2 -> { return SlotItem.Neck; }
            case 3 -> { return SlotItem.Shoulder; }
            case 4 -> { return SlotItem.Back; }
            case 5 -> { return SlotItem.Chest; }
            case 6 -> { return SlotItem.Wrist; }
            case 7 -> { return SlotItem.Hand; }
            case 8 -> { return SlotItem.Belt; }
            case 9 -> { return SlotItem.Leg; }
            case 10 -> { return SlotItem.Foot; }
            case 11 -> { return SlotItem.Ring; }
            case 12 -> { return SlotItem.Trinket; }
            case 13, 14 -> {
                switch (handType) {
                    case 1, 2 -> { return SlotItem.Weapon1H; }
                    case 0, 4 -> { return SlotItem.Weapon2H; }
                    case 3 -> { return SlotItem.Offhand; }
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

    public ItemData lookupItem(ItemRef ref) {
        return itemMap.get(ref);
    }

    public Stream<ItemData> itemStream() {
        return itemMap.values().stream();
    }

    public int reforgeId(ReforgeRecipe reforge) {
        return reforgeIds.get(reforge);
    }
}
