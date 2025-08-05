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
import java.util.Arrays;
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
                JsonObject elementObject = element.getAsJsonObject();
                int id = elementObject.get("id").getAsInt();

                int[] gems = null;
                if (elementObject.has("gems")) {
                    JsonArray array = elementObject.get("gems").getAsJsonArray();
                    gems = array.asList().stream().mapToInt(JsonElement::getAsInt).toArray();
                }

                Integer enchant = null;
                if (elementObject.has("enchant")) {
                    int enchantId = elementObject.get("enchant").getAsInt();
                    if (gems == null) {
                        gems = new int[]{enchantId};
                    } else {
                        gems = Arrays.copyOf(gems, gems.length + 1);
                        gems[gems.length - 1] = enchantId;
                    }
                    enchant = enchantId;
                }

                result.add(new EquippedItem(id, gems, enchant));
            }
        }
        return result;
    }
}
