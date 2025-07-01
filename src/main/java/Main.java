import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing\\src\\main\\java");

    public static void main(String[] arg) throws IOException {
        ItemData item = new ItemData();
        item.name = "abcd";
        item.str=200;
        item.mastery=443;

        Map<Integer, ItemData> cache = new HashMap<>();
        cache.put(123, item);

        try (Writer writer = Files.newBufferedWriter(directory.resolve("cache.json"))) {
            new Gson().toJson(cache, writer);
        }


//        int[] gear = new int[] {
//                78788, 70107, 78837, 248749, 78822, 77317, 78770, 77185
//        };
//
//        for (int id : gear) {
//            readItem(id);
//        }
    }

    public static JsonElement parseJson(String str) throws JsonIOException, JsonSyntaxException {
            StringReader reader = new StringReader(str);
            JsonReader jsonReader = new JsonReader(reader);
            JsonElement element = JsonParser.parseReader(jsonReader);
//            if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
//                throw new JsonSyntaxException("Did not consume the entire document.");
//            }
            return element;
    }

    private static ItemData readItem(int itemId) throws IOException {
        String url = "https://www.wowhead.com/cata/item="+itemId;
        String htmlContent = new String(new URL(url).openStream().readAllBytes());
        int startDataSection = htmlContent.indexOf("WH.Gatherer.addData");
        int startJson = htmlContent.indexOf('{', startDataSection);
        String jsonOnwards = htmlContent.substring(startJson);
//        System.out.println(val);


        JsonObject json = parseJson(jsonOnwards).getAsJsonObject();
        if (!json.has(String.valueOf(itemId))) {
            System.out.println("Failed "+itemId);
            return null;
        }
        JsonObject itemObject = json.get(String.valueOf(itemId)).getAsJsonObject();

        ItemData item = buildItem(itemObject);
        System.out.println(itemObject);
        System.out.println(item);
        return item;

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


    public static class ItemData {
        String name;
        Integer str;
        Integer mastery;
        Integer crit;
        Integer hit;
        Integer haste;
        Integer expertise;

        @Override
        public String toString() {
            return "ItemData{" +
                    "name='" + name + '\'' +
                    ", str=" + str +
                    ", mastery=" + mastery +
                    ", crit=" + crit +
                    ", hit=" + hit +
                    ", haste=" + haste +
                    ", expertise=" + expertise +
                    '}';
        }
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

    private static Integer objectGetInt(JsonObject object, String field) {
        if (object.has(field))
            return object.get(field).getAsInt();
        else
            return null;
    }
}
