package au.nicholas.hardy.mopgear;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InputParser {
    static List<EquippedItem> readInput(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return parseReader(reader);
        }
    }

    public static List<EquippedItem> readString(String jsonString) {
        StringReader reader = new StringReader(jsonString);
        return parseReader(reader);
    }

    private static List<EquippedItem> parseReader(Reader reader) {
        List<EquippedItem> result = new ArrayList<>();
        JsonObject inputObject = JsonParser.parseReader(reader).getAsJsonObject();
        JsonObject gear = inputObject.getAsJsonObject("gear");
        JsonArray items = gear.getAsJsonArray("items");
        for (JsonElement element : items) {
            if (element.isJsonObject()) {;
                int id = element.getAsJsonObject().get("id").getAsInt();
                String enchant;
                if (element.getAsJsonObject().has("enchant")) {
                    enchant = element.getAsJsonObject().get("enchant").getAsString();
                } else {
                    enchant = "MISSING ENCHANT";
                }
                result.add(new EquippedItem(id, enchant));
            }
        }
        return result;
    }
}
