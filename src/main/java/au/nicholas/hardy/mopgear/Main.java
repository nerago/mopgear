package au.nicholas.hardy.mopgear;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Main {

    static final Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    static final Path cacheFile = directory.resolve("cache.json");
    static final Path inputFile = directory.resolve("input.json");
    static Map<Integer, ItemData> itemCache;

    public static void main(String[] arg) throws IOException {
        cacheLoad();

        Instant startTime = Instant.now();

        List<Integer> itemIds = Main.readInput();
        List<ItemData> items = Main.loadItems(itemIds);
        Collection<ItemSet> bestSets = Engine.runSolver(items, startTime);
        bestSets.forEach(s -> System.out.println(s.totals));
        bestSets.forEach(s -> {
            System.out.println("#######################################");
            System.out.println(s.totals);
            for (ItemData it : s.items.toArrayReverse(ItemData[]::new)) {
                System.out.println(it);
            }
        });

        printElapsed(startTime);

        cacheSave();
    }

    private static void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }

    private static List<ItemData> loadItems(List<Integer> itemIds) throws IOException {
        List<ItemData> items = new ArrayList<>();
        for (int id : itemIds) {
            ItemData item = itemCache.get(id);
            if (item != null) {
                items.add(item);
            } else {
                item = fetchItem(id);
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

    private static List<Integer> readInput() throws IOException {
        List<Integer> itemIds = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            JsonObject inputObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject gear = inputObject.getAsJsonObject("gear");
            JsonArray items = gear.getAsJsonArray("items");
            for (JsonElement element : items) {
                if (element.isJsonObject()) {
                    int id = element.getAsJsonObject().get("id").getAsInt();
                    itemIds.add(id);
                }
            }
        }
        return itemIds;
    }

    private static void cacheSave() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(cacheFile)) {
            new Gson().toJson(itemCache, writer);
        }
    }

    private static void cacheLoad() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(cacheFile)) {
            TypeToken<Map<Integer, ItemData>> typeToken = new TypeToken<>() {
            };
            itemCache = new Gson().fromJson(reader, typeToken);
        }
    }

    public static JsonElement parseJson(String str) throws JsonIOException, JsonSyntaxException {
        StringReader reader = new StringReader(str);
        JsonReader jsonReader = new JsonReader(reader);
        return JsonParser.parseReader(jsonReader);
    }

    private static ItemData fetchItem(int itemId) throws IOException {
        String url = "https://www.wowhead.com/cata/item=" + itemId;
        String htmlContent = fetchHTML(url);

        System.out.println(htmlContent);

        int startIndex = 0;

        while (true) {
            int startDataSection = htmlContent.indexOf("WH.Gatherer.addData", startIndex);
            if (startDataSection == -1)
                break;
            int startJson = htmlContent.indexOf('{', startDataSection);
            String jsonOnwards = htmlContent.substring(startJson);

            JsonObject json = parseJson(jsonOnwards).getAsJsonObject();
            if (json.has(String.valueOf(itemId))) {
                System.out.println("Fetched " + itemId);
            } else {
                startIndex = startDataSection + 1;
                continue;
            }
            JsonObject itemObject = json.get(String.valueOf(itemId)).getAsJsonObject();

            ItemData item = buildItem(itemObject);
            System.out.println(itemObject);
            System.out.println(item);
            return item;
        }

        System.out.println("Failed " + itemId);
        return null;
    }

    private static String fetchHTML(String url) throws IOException {
        String htmlContent;
        try (InputStream stream = URI.create(url).toURL().openStream()) {
            htmlContent = new String(stream.readAllBytes());
        }
        return htmlContent;
    }

    private static ItemData buildItem(JsonObject itemObject) {
        ItemData item = new ItemData();
        item.name = objectGetString(itemObject, "name_enus");

        JsonObject equipObject = itemObject.get("jsonequip").getAsJsonObject();
        item.str = objectGetInt(equipObject, "str");
        item.mastery = objectGetInt(equipObject, "mastrtng");
        item.crit = objectGetInt(equipObject, "critstrkrtng");
        item.expertise = objectGetInt(equipObject, "exprtng");
        item.hit = objectGetInt(equipObject, "hitrtng");
        item.haste = objectGetInt(equipObject, "hastertng");

        return item;
    }

    private static String objectGetString(JsonObject object, String field) {
        if (object.has(field))
            return object.get(field).getAsString();
        else
            return null;
    }

    private static int objectGetInt(JsonObject object, String field) {
        if (object.has(field))
            return object.get(field).getAsInt();
        else
            return 0;
    }
}
