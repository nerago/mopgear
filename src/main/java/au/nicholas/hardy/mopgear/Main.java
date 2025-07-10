package au.nicholas.hardy.mopgear;

import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    private static final Path cacheFile = directory.resolve("cache.json");
    private static final Path inputFile = directory.resolve("input.json");

    ItemCache itemCache;

    public static void main(String[] arg) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        itemCache = new ItemCache(cacheFile);
        ModelParams.validate();

        Instant startTime = Instant.now();

        reforgeProcess(startTime);

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void reforgeProcess(Instant startTime) throws IOException {
        List<EquippedItem> itemIds = Main.readInput();
        List<ItemData> items = loadItems(itemIds);
        Map<SlotEquip, List<ItemData>> reforgedItems = standardItemsToMap(items);
        Collection<ItemSet> bestSets = Engine.runSolver(reforgedItems, startTime);
        outputResult(bestSets);
    }

    private Map<SlotEquip, List<ItemData>> standardItemsToMap(List<ItemData> items) {
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

//    private void findUpgrade(Instant startTime) throws IOException {
//        List<EquippedItem> baseItemIds = Main.readInput();
//        List<ItemData> baseItems = loadItems(baseItemIds);
//        Collection<ItemSet> bestSets = Engine.runSolver(items, startTime);
//        bestSets.forEach(s -> System.out.println(s.totals));
//        bestSets.forEach(s -> {
//            System.out.println("#######################################");
//            System.out.println(s.totals);
//            for (ItemData it : s.items.toArrayReverse(ItemData[]::new)) {
//                System.out.println(it);
//            }
//        });
//    }

    private static void outputResult(Collection<ItemSet> bestSets) {
        bestSets.forEach(s -> System.out.println(s.getTotals()));
        bestSets.forEach(s -> {
            System.out.println("#######################################");
            System.out.println(s.getTotals());
            for (ItemData it : s.getItems().toArrayReverse(ItemData[]::new)) {
                System.out.println(it);
            }
        });
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }

    private List<ItemData> loadItems(List<EquippedItem> itemIds) throws IOException {
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

    private static List<EquippedItem> readInput() throws IOException {
        List<EquippedItem> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            JsonObject inputObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject gear = inputObject.getAsJsonObject("gear");
            JsonArray items = gear.getAsJsonArray("items");
            for (JsonElement element : items) {
                if (element.isJsonObject()) {
                    EquippedItem item = new EquippedItem();
                    item.id = element.getAsJsonObject().get("id").getAsInt();
                    if (element.getAsJsonObject().has("enchant")) {
                        item.enchant = element.getAsJsonObject().get("enchant").getAsString();
                    } else {
                        item.enchant = "MISSING ENCHANT";
                    }
                    result.add(item);
                }
            }
        }
        return result;
    }

}
