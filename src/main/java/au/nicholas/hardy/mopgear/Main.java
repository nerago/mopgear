package au.nicholas.hardy.mopgear;

import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        Instant startTime = Instant.now();

        reforgeProcess(startTime);

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void reforgeProcess(Instant startTime) throws IOException {
        List<EquippedItem> itemIds = Main.readInput();
        List<ItemData> items = loadItems(itemIds);
        Collection<ItemSet> bestSets = Engine.runSolver(items, startTime);
        bestSets.forEach(s -> System.out.println(s.totals));
        bestSets.forEach(s -> {
            System.out.println("#######################################");
            System.out.println(s.totals);
            for (ItemData it : s.items.toArrayReverse(ItemData[]::new)) {
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
