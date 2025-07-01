package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Main {
    static Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    static Path cacheFile = directory.resolve("cache.json");
    static Path inputFile = directory.resolve("input.json");
    static Map<Integer, ItemData> itemCache;

    public static void main(String[] arg) throws IOException {
        cacheLoad();

        Instant startTime = Instant.now();

        runSolver();

        // 15 items
        // 4,747,561,509,943 total combinations

        printElapsed(startTime);

        cacheSave();
    }

    private static void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }

    private static void runSolver() throws IOException {
        List<Integer> itemIds = readInput();
        List<ItemData> items = loadItems(itemIds);

        List<List<ItemData>> reforgedItems = items.stream().map(Main::reforgeItem).toList();

        System.out.println("total items " + reforgedItems.size());
        reforgedItems = new ArrayList<>(reforgedItems);
        while (reforgedItems.size() >= 13) reforgedItems.removeLast();

        //       initial     summary   no-nullable  avoid-lowest
        // 11 == 0.969s      14s
        // 12 == 8.05s       1M30      27s          4.9s
        // 13 == 1M2                                28s

        Stream<CurryQueue<ItemData>> initialSets = generateItemCombinations(reforgedItems);
//        System.out.println(initialSets.count());
        Stream<ItemSet> summarySets = makeSummarySets(initialSets);
        System.out.println(summarySets.count());
    }

    private static Stream<ItemSet> makeSummarySets(Stream<CurryQueue<ItemData>> initialSets) {
        return initialSets.map(ItemSet::new);
    }

    private static Stream<CurryQueue<ItemData>> generateItemCombinations(List<List<ItemData>> itemsBySlot) {
        Stream<CurryQueue<ItemData>> stream = null;
        for (List<ItemData> slotItems : itemsBySlot) {
            if (stream == null) {
                stream = newCombinationStream(slotItems);
            } else {
                stream = applyItemsToCombination(stream, slotItems);
            }
        }
        return stream;
    }

    private static Stream<CurryQueue<ItemData>> newCombinationStream(List<ItemData> slotItems) {
        return slotItems.parallelStream().unordered().map(CurryQueue::single);
    }

    private static Stream<CurryQueue<ItemData>> applyItemsToCombination(Stream<CurryQueue<ItemData>> stream, List<ItemData> slotItems) {
        return stream.mapMulti((set, sink) -> {
            for (ItemData add : slotItems) {
                sink.accept(set.prepend(add));
            }
        });
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

        /*
        WH.Gatherer.addData(3, 11, {"77185":
        {"name_enus":"Demonbone Waistguard","quality":4,"icon":"inv_belt_plate_raiddeathknight_k_01","screenshot":{},"jsonequip":{"appearances":{"0":[105638,""]},"armor":2154,"buyprice":3000000,
        "critstrkrtng":221,"displayid":105638,"dura":60,"mastrtng":221,"nsockets":2,"reqlevel":85,"sellprice":750000,"slotbak":6,"socket1":2,"socket2":4,"socketbonus":4136,"sta":542,"str":321},"attainable":0,"flags2":134242304,"displayName":"","qualityTier":0},
        "77250":{"name_enus":"Runescriven Demon Collar","quality":4,"icon":"inv_belt_plate_raiddeathknight_k_01","screenshot":{},

        $.extend(g_items[77185], {"appearances":{"0":[105638,""]},"armor":2154,"classs":4,"displayid":105638,"flags2":134242304,"id":77185,"level":397,"name":"Demonbone Waistguard","quality":4,"reqlevel":85,"slot":6,"slotbak":6,"source":[5],"subclass":4,"jsonequip":{"quality":4,"appearances":{"0":[105638,""]},"armor":2154,"classs":4,"displayid":105638,"flags2":134242304,"id":77185,"level":397,"name":"Demonbone Waistguard","quality":4,"reqlevel":85,"slot":6,"slotbak":6,"source":[5],"subclass":4,"appearances":{"0":[105638,""]},"armor":2154,"buyprice":3000000,"critstrkrtng":221,"displayid":105638,"dura":60,"mastrtng":221,"nsockets":2,"reqlevel":85,"sellprice":750000,"slotbak":6,"socket1":2,"socket2":4,"socketbonus":4136,"sta":542,"str":321,"statsInfo":{"4":{"qty":321,"alloc":40,"socketMult":1},"7":{"qty":542,"alloc":0,"socketMult":0},"32":{"qty":221,"alloc":20,"socketMult":0.5},"49":{"qty":221,"alloc":20,"socketMult":0.5}}}});

        <table><tr><td><!--nstart--><!--nend--><!--ndstart--><!--ndend--><span class="q"><br>Item Level <!--ilvl-->397</span><!--bo--><br>Binds when picked up<!--ue--><table width="100%"><tr><td>Waist</td><th><!--scstart4:4--><span class="q1">Plate</span><!--scend--></th></tr></table><!--rf--><span><!--amr-->2154 Armor</span><br><span><!--stat4-->+321 Strength</span><br><span><!--stat7-->+542 Stamina</span><!--ebstats--><br><span class="q2">+<!--rtg49-->221 Mastery</span><!--egstats--><!--eistats--><!--nameDescStats--><!--rs--><!--e--><br /><br><a href="/cata/items/gems?filter=81;2;0" class="socket-red q0">Red Socket</a><br><a href="/cata/items/gems?filter=81;4;0" class="socket-blue q0">Blue Socket</a><!--ps--><br><!--sb--><span class="q0">Socket Bonus: +20 Strength</span><br /><br />Durability 60 / 60</td></tr></table><table><tr><td>Requires Level <!--rlvl-->85<br><!--rr--><span class="q2">Equip: Improves critical strike rating by <!--rtg32-->221.</span><!--itemEffects:1--><br><!--pvpEquip--><!--pvpEquip--><div class="whtt-sellprice">Sell Price: <span class="moneygold">75</span></div></td></tr></table><!--i?77185:1:85:85--></noscript>

        Correct stats are 221 Mastery, 221 Crit
         */
    }

    private static String fetchHTML(String url) throws IOException {
        String htmlContent;
        try (InputStream stream = URI.create(url).toURL().openStream()) {
            htmlContent = new String(stream.readAllBytes());
        }
        return htmlContent;
    }

    enum Secondary {
        Mastery,
        Crit,
        Hit,
        Haste,
        Expertise
    }

    static final Secondary[] priority = new Secondary[]{Secondary.Hit, Secondary.Expertise, Secondary.Mastery, Secondary.Crit, Secondary.Haste};
    static final Secondary priorityLowest = priority[priority.length - 1];

    static final int TARGET_HIT = 961, TARGET_EXPERTISE = 481, PERMITTED_EXCEED = 80;

    static List<ItemData> reforgeItem(ItemData baseItem) {
        List<ItemData> outputItems = new ArrayList<>();
        outputItems.add(baseItem);

        for (Secondary originalStat : Secondary.values()) {
            int originalValue = baseItem.get(originalStat);
            if (originalValue != 0) {
                int reforgeQuantity = (originalValue * 4) / 10;
                int remainQuantity = originalValue - reforgeQuantity;
                for (Secondary targetStat : Secondary.values()) {
                    if (targetStat != priorityLowest && baseItem.get(targetStat) == 0) {
                        ItemData modified = baseItem.copy();
                        modified.set(originalStat, remainQuantity);
                        modified.set(targetStat, reforgeQuantity);
                        outputItems.add(modified);
                    }
                }
            }
        }

        return outputItems;
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
