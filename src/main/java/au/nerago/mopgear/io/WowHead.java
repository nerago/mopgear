package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.GemData;
import au.nerago.mopgear.results.OutputText;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static au.nerago.mopgear.domain.ArmorType.NotApplicable;

public class WowHead {
    public static FullItemData fetchItem(int itemId) {
        String url = "https://www.wowhead.com/mop-classic/item=" + itemId;
        String htmlContent = fetchHTML(url);

//        OutputText.println(htmlContent);

        int startIndex = 0;

        while (true) {
            int startDataSection = htmlContent.indexOf("WH.Gatherer.addData", startIndex);
            if (startDataSection == -1)
                break;
            int startJson = htmlContent.indexOf('{', startDataSection);
            String jsonOnwards = htmlContent.substring(startJson);

            JsonObject json = parseJson(jsonOnwards).getAsJsonObject();
            if (json.has(String.valueOf(itemId))) {
                JsonObject io = json.get(String.valueOf(itemId)).getAsJsonObject();
                JsonObject je = io.get("jsonequip").getAsJsonObject();
                if (je.has("reqlevel")) {
                    int level = je.get("reqlevel").getAsInt();
                    if (level > 1 && level < 85) {
                        OutputText.println("Skipping " + itemId + " version for level " + level);
                        startIndex = startDataSection + 1;
                        continue;
                    }
                }
            } else {
                startIndex = startDataSection + 1;
                continue;
            }
            OutputText.println("Fetched " + itemId);
            JsonObject itemObject = json.get(String.valueOf(itemId)).getAsJsonObject();
            OutputText.println(itemObject.toString());

            int itemLevel = readItemLevel(htmlContent);

            FullItemData item = buildItem(itemObject, itemId, itemLevel);
            OutputText.println(item.toString());
            if (item.statBase.isEmpty())
                OutputText.println("WARNWARNWARN item has no stats " + item);
            return item;
        }

        OutputText.println("Failed " + itemId);
        return null;
    }

    private static int readItemLevel(String htmlContent) {
        try {
            String search = "<!--ilvl-->";
            int index = htmlContent.indexOf(search);
            if (index == -1)
                return 0;
            index += search.length();
            String sub = htmlContent.substring(index, index + 3);
            return Integer.parseInt(sub);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String fetchHTML(String url) {
        String htmlContent;
        try (InputStream stream = URI.create(url).toURL().openStream()) {
            htmlContent = new String(stream.readAllBytes());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return htmlContent;
    }

//    {"name_enus":"Helmet of Radiant Glory","quality":4,"icon":"inv_helm_plate_raidpaladin_k_01","screenshot":{},
//    "jsonequip":{"appearances":{"0":[104069,""]},"armor":2995,"buyprice":946846,"classes":2,"displayid":104069,"dura":100,"exprtng":277,"hitrtng":237,"itemset":1064,"nsockets":2,"races":2099199,"reqlevel":85,"sellprice":189369,"slotbak":1,"socket1":1,"socket2":2,"socketbonus":4158,"sta":646,"str":371},
//    "attainable":0,"flags2":134242304,"displayName":"","qualityTier":0}

    private static FullItemData buildItem(JsonObject itemObject, int itemId, int itemLevel) {
        String name = objectGetString(itemObject, "name_enus");
        Objects.requireNonNull(name);

        JsonObject equipObject = itemObject.get("jsonequip").getAsJsonObject();
        SlotItem slot = SlotItem.withNum(objectGetInt(equipObject, "slotbak"));
        int socketBonus = objectGetInt(equipObject, "socketbonus");
        StatBlock socketBonusBlock = socketBonus != 0 ? GemData.getSocketBonus(name, socketBonus) : null;

        StatBlock statBlock = new StatBlock(
                objectGetIntOneOf(equipObject, "str", "int", "agi"),
                objectGetInt(equipObject, "sta"),
                objectGetInt(equipObject, "mastrtng"),
                objectGetInt(equipObject, "critstrkrtng"),
                objectGetInt(equipObject, "hitrtng"),
                objectGetInt(equipObject, "hastertng"),
                objectGetInt(equipObject, "exprtng"),
                objectGetInt(equipObject, "dodgertng"),
                objectGetInt(equipObject, "parryrtng"),
                objectGetInt(equipObject, "spi"));

        PrimaryStatType primaryStatType;
        int primaryCount = (equipObject.has("str") ? 1 : 0) + (equipObject.has("int") ? 1 : 0) + (equipObject.has("agi") ? 1 : 0);
        if (primaryCount > 1) {
            throw new IllegalArgumentException("primary stat conflict");
        } else if (primaryCount == 0) {
            primaryStatType = PrimaryStatType.NotApplicable;
        } else if (equipObject.has("str")) {
            primaryStatType = PrimaryStatType.Strength;
        } else if (equipObject.has("int")) {
            primaryStatType = PrimaryStatType.Intellect;
        } else if (equipObject.has("agi")) {
            primaryStatType = PrimaryStatType.Agility;
        } else {
            throw new IllegalArgumentException("primary stat unknown");
        }

        @NotNull ArmorType armorType = NotApplicable;
        throw new RuntimeException("don't know how to determine armor type");

//        List<SocketType> sockets = new ArrayList<>();
//        for (int i = 1; i <= 5; ++i) {
//            int socketTypeNum = objectGetInt(equipObject, "socket" + i);
//            if (socketTypeNum != 0) {
//                SocketType type = SocketType.withNum(socketTypeNum);
//                sockets.add(type);
//            }
//        }
//        SocketType[] socketArray = sockets.toArray(SocketType[]::new);
//
//        return FullItemData.buildFromWowHead(itemId, slot, name, statBlock, primaryStatType, armorType, socketArray, socketBonusBlock, itemLevel);
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

    private static int objectGetIntOneOf(JsonObject object, String... fields) {
        boolean found = false;
        int value = 0;
        for (String field : fields) {
            if (object.has(field)) {
                if (found)
                    throw new IllegalArgumentException("primary stat conflict");
                value = object.get(field).getAsInt();
                found = true;
            }
        }
        return value;
    }

    private static JsonElement parseJson(String str) throws JsonIOException, JsonSyntaxException {
        StringReader reader = new StringReader(str);
        JsonReader jsonReader = new JsonReader(reader);
        return JsonParser.parseReader(jsonReader);
    }
}
