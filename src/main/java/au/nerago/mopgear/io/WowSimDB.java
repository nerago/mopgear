package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.SetBonus;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.Tuple;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WowSimDB {
    private final Map<ItemRef, FullItemData> itemMap = new HashMap<>();
    private final Map<ReforgeRecipe, Integer> reforgeIds = new HashMap<>();

    // https://raw.githubusercontent.com/wowsims/mop/57251c327bbc745d1512b9c13e952f4bcf3deedb/assets/database/db.json

    private static final URL fileUrl = WowSimDB.class.getClassLoader().getResource("wowsimdb.json");
    public final static WowSimDB instance = new WowSimDB();

    private WowSimDB() {
        readInput(Objects.requireNonNull(fileUrl));
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

        int phase = getIntOrDefault(object, "phase", -1);
        ArmorType armorType = convertArmorType(getIntOrDefault(object, "armorType", -1));

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
            StatResult result = summarizeStats(stats);

            ItemRef ref = ItemRef.buildAdvanced(id, itemLevel, baseItemLevel);
            FullItemData item = FullItemData.buildFromWowSim(ref, slot, name, result.block(), result.primaryStatType(), armorType, sockets, socketBonusBlock, phase);
            itemMap.put(item.ref(), item);
        }
    }

    @NotNull
    private static StatResult summarizeStats(JsonObject stats) {
        boolean hasStr = false, hasInt = false, hasAgi = false;
        StatBlock block = StatBlock.empty;

        if (stats != null) {
            for (Map.Entry<String, JsonElement> statEntry : stats.entrySet()) {
                String statKey = statEntry.getKey();
                int value = statEntry.getValue().getAsInt();
                StatType statType = mapStat(statKey);
                if (statType != null) {
                    block = block.withChange(statType, value);
                }
                switch (statKey) {
                    case "0" -> hasStr = true;
                    case "1" -> hasAgi = true;
                    case "3" -> hasInt = true;
                }
            }
        }

        PrimaryStatType primaryStatType = selectPrimaryStat(hasStr, hasInt, hasAgi);
        return new StatResult(block, primaryStatType);
    }

    private record StatResult(StatBlock block, PrimaryStatType primaryStatType) {
    }

    private static PrimaryStatType selectPrimaryStat(boolean hasStr, boolean hasInt, boolean hasAgi) {
        int primaryCount = (hasStr ? 1 : 0) + (hasInt ? 1 : 0) + (hasAgi ? 1 : 0);
        if (primaryCount > 1) {
            throw new IllegalArgumentException("primary stat conflict");
        } else if (primaryCount == 0) {
            return PrimaryStatType.NotApplicable;
        } else if (hasStr) {
            return PrimaryStatType.Strength;
        } else if (hasInt) {
            return PrimaryStatType.Intellect;
        } else {
            return PrimaryStatType.Agility;
        }
    }

    private static ArmorType convertArmorType(int armorType) {
        switch (armorType) {
            case -1:
                return ArmorType.NotApplicable;
            case 1:
                return ArmorType.Cloth;
            case 2:
                return ArmorType.Leather;
            case 3:
                return ArmorType.Mail;
            case 4:
                return ArmorType.Plate;
            default:
                throw new RuntimeException("unexpected armor type " + armorType);
        }
    }

    private static StatBlock convertBlock(JsonArray array) {
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

    private static StatType blockIndexToStat(int index) {
        switch (index) {
            case 0, 1, 3 -> {
                return StatType.Primary;
            }
            case 2 -> {
                return StatType.Stam;
            }
            case 4 -> {
                return StatType.Spirit;
            }
            case 5 -> {
                return StatType.Hit;
            }
            case 6 -> {
                return StatType.Crit;
            }
            case 7 -> {
                return StatType.Haste;
            }
            case 8 -> {
                return StatType.Expertise;
            }
            case 9 -> {
                return StatType.Dodge;
            }
            case 10 -> {
                return StatType.Parry;
            }
            case 11 -> {
                return StatType.Mastery;
            }
            case 15, 16 -> {
                return null;
            }//pvp
            default -> throw new RuntimeException("unknown stat index " + index);
        }
    }

    private static SocketType mapSocket(int num) {
        return switch (num) {
            case 1 -> SocketType.Meta;
            case 2 -> SocketType.Red;
            case 3 -> SocketType.Blue;
            case 4 -> SocketType.Yellow;
            case 8 -> SocketType.General; // flagging for a possible belt socket
            case 9 -> SocketType.Engineer;
            case 10 -> SocketType.Sha;
            default -> throw new RuntimeException("unknown socket " + num);
        };
    }

    private static SlotItem mapSlot(int type, int weaponType, int handType) {
        switch (type) {
            case 1 -> {
                return SlotItem.Head;
            }
            case 2 -> {
                return SlotItem.Neck;
            }
            case 3 -> {
                return SlotItem.Shoulder;
            }
            case 4 -> {
                return SlotItem.Back;
            }
            case 5 -> {
                return SlotItem.Chest;
            }
            case 6 -> {
                return SlotItem.Wrist;
            }
            case 7 -> {
                return SlotItem.Hand;
            }
            case 8 -> {
                return SlotItem.Belt;
            }
            case 9 -> {
                return SlotItem.Leg;
            }
            case 10 -> {
                return SlotItem.Foot;
            }
            case 11 -> {
                return SlotItem.Ring;
            }
            case 12 -> {
                return SlotItem.Trinket;
            }
            case 13, 14 -> {
                switch (handType) {
                    case 1, 2 -> {
                        return SlotItem.Weapon1H;
                    }
                    case 0, 4 -> {
                        return SlotItem.Weapon2H;
                    }
                    case 3 -> {
                        return SlotItem.Offhand;
                    }
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

    private static int getIntOrDefault(JsonObject object, String key, int defaultValue) {
        JsonElement entry = object.get(key);
        if (entry != null)
            return entry.getAsInt();
        else
            return defaultValue;
    }

    private static int getFirstIntInArray(JsonObject object, String key, int defaultValue) {
        JsonElement entry = object.get(key);
        if (entry != null) {
            JsonArray array = entry.getAsJsonArray();
            if (!array.isEmpty()) {
                return array.get(0).getAsInt();
            }
        }
        return defaultValue;
    }

    private static String getStringOrDefault(JsonObject object, String key, String defaultValue) {
        JsonElement entry = object.get(key);
        if (entry != null)
            return entry.getAsString();
        else
            return defaultValue;
    }

    public FullItemData lookupItem(ItemRef ref) {
        return itemMap.get(ref);
    }

    public Stream<FullItemData> itemStream() {
        return itemMap.values().stream();
    }

    public int reforgeId(ReforgeRecipe reforge) {
        return reforgeIds.get(reforge);
    }

    public ReforgeRecipe reforgeId(int reforgeId) {
        for (Map.Entry<ReforgeRecipe, Integer> entry : reforgeIds.entrySet()) {
            if (entry.getValue() == reforgeId)
                return entry.getKey();
        }
        throw new RuntimeException("reforge id not found " + reforgeId);
    }

    public static void discoverSetBonuses() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileUrl.openStream()))) {
            JsonObject mainObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray itemsArray = mainObject.getAsJsonArray("items");

            Map<Tuple.Tuple2<WowClass, String>, Tuple.Tuple2<TreeSet<Integer>, List<Integer>>> itemsBySet = new HashMap<>();
            for (JsonElement element : itemsArray) {
                JsonObject object = element.getAsJsonObject();
                discoverSetBonuses(object, itemsBySet);
            }
            reportSetBonuses(itemsBySet);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void discoverSetBonuses(JsonObject object, Map<Tuple.Tuple2<WowClass, String>, Tuple.Tuple2<TreeSet<Integer>, List<Integer>>> itemsBySet) {
        int itemId = object.get("id").getAsInt();
        String name = object.get("name").getAsString();
        int type = getIntOrDefault(object, "type", -1);
        if (type == -1)
            return;

        JsonObject scalingOptions = object.getAsJsonObject("scalingOptions");
        JsonObject firstScaleEntry = scalingOptions.get("0").getAsJsonObject();
        int itemLevel = firstScaleEntry.get("ilvl").getAsInt();

        int classAllowId = getFirstIntInArray(object, "classAllowlist", -1);
        int setId = getIntOrDefault(object, "setId", -1);
        String setName = getStringOrDefault(object, "setName", "");
        int phase = getIntOrDefault(object, "phase", -1);

        if (setId != -1 && classAllowId != -1 && !name.contains("Gladiator") && itemLevel >= 463) {
            WowClass classType = WowClass.forId(classAllowId);
            if (classType == null)
                throw new RuntimeException("unknown class");

            Tuple.Tuple2<WowClass, String> key = Tuple.create(classType, setName);
            Tuple.Tuple2<TreeSet<Integer>, List<Integer>> entry = itemsBySet.computeIfAbsent(key, x -> Tuple.create(new TreeSet<>(), new ArrayList<>()));
            entry.a().add(phase);
            entry.a().add(itemLevel);
            entry.b().add(itemId);
        }
    }

    private static void reportSetBonuses(Map<Tuple.Tuple2<WowClass, String>, Tuple.Tuple2<TreeSet<Integer>, List<Integer>>> itemsBySet) {
//        Comparator<Tuple.Tuple3<WowClass, String, Integer>> cc1 = Comparator.comparing(Tuple.Tuple3::a);
////        Comparator<Tuple.Tuple3<WowClass, String, Integer>> cc2 = Comparator.comparing(Tuple.Tuple3::a).thenComparing( (a,b) -> 1);
//        Comparator<Tuple.Tuple3<WowClass, String, Integer>> cc3 =
//                Comparator.comparing((Function<Tuple.Tuple3<WowClass, String, Integer>, String>) x -> x.a().toString()).thenComparing(Tuple.Tuple3::c);
//        Comparator<Tuple.Tuple3<WowClass, String, Integer>> cc4 =
//                Comparator.comparing( (Tuple.Tuple3<WowClass, String, Integer> x) -> x.a()).thenComparing(Tuple.Tuple3::c);

        for (Tuple.Tuple2<WowClass, String> key : itemsBySet.keySet().stream()
                .sorted(Comparator.comparing((Tuple.Tuple2<WowClass, String> x) -> x.a()).thenComparing(Tuple.Tuple2::b))
                .toList()) {
            TreeSet<Integer> levels = itemsBySet.get(key).a();
            List<Integer> itemList = itemsBySet.get(key).b();

            // all meet this condition
//            if (levels.getFirst() == 483 || levels.getFirst() == 502 || levels.getFirst() == 528)
//                continue;

//            sets.add(new SetBonus.SetInfo(SpecType.PaladinProtMitigation, DEFAULT_BONUS, DEFAULT_BONUS, whiteTigerPlate));
            WowClass wowClass = key.a();
            String setName = key.b();
            OutputText.printf("sets.add(new SetInfo(SpecType.%s, \"%s\", DEFAULT_BONUS, DEFAULT_BONUS, new int[] {", wowClass, setName);
            OutputText.print(itemList.stream().map(Object::toString).collect(Collectors.joining(",")));
            OutputText.println("}));");
        }
    }
}
