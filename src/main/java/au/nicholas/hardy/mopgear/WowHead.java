package au.nicholas.hardy.mopgear;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;

public class WowHead {
    static ItemData fetchItem(int itemId) throws IOException {
        String url = "https://www.wowhead.com/cata/item=" + itemId;
        String htmlContent = fetchHTML(url);

//        System.out.println(htmlContent);

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
            System.out.println(itemObject);

            ItemData item = buildItem(itemObject);
            System.out.println(item);
//            throw  new RuntimeException("todo");
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

//    {"name_enus":"Helmet of Radiant Glory","quality":4,"icon":"inv_helm_plate_raidpaladin_k_01","screenshot":{},
//    "jsonequip":{"appearances":{"0":[104069,""]},"armor":2995,"buyprice":946846,"classes":2,"displayid":104069,"dura":100,"exprtng":277,"hitrtng":237,"itemset":1064,"nsockets":2,"races":2099199,"reqlevel":85,"sellprice":189369,"slotbak":1,"socket1":1,"socket2":2,"socketbonus":4158,"sta":646,"str":371},
//    "attainable":0,"flags2":134242304,"displayName":"","qualityTier":0}

    private static ItemData buildItem(JsonObject itemObject) {
        ItemData item = new ItemData();
        item.name = objectGetString(itemObject, "name_enus");

        JsonObject equipObject = itemObject.get("jsonequip").getAsJsonObject();
        item.slot = SlotItem.withNum(objectGetInt(equipObject, "slotbak"));
        item.str = objectGetInt(equipObject, "str");
        item.mastery = objectGetInt(equipObject, "mastrtng");
        item.crit = objectGetInt(equipObject, "critstrkrtng");
        item.expertise = objectGetInt(equipObject, "exprtng");
        item.hit = objectGetInt(equipObject, "hitrtng");
        item.haste = objectGetInt(equipObject, "hastertng");

        return item;
    }

    @SuppressWarnings("SameParameterValue")
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

    private static JsonElement parseJson(String str) throws JsonIOException, JsonSyntaxException {
        StringReader reader = new StringReader(str);
        JsonReader jsonReader = new JsonReader(reader);
        return JsonParser.parseReader(jsonReader);
    }
}
