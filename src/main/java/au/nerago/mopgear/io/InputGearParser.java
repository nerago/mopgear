package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.EquippedItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputGearParser {
    public static List<EquippedItem> readInput(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return parseReader(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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
            if (element.isJsonObject()) {
                JsonObject elementObject = element.getAsJsonObject();
                EquippedItem equippedItem = parseEquipped(elementObject);
                result.add(equippedItem);
            }
        }
        return result;
    }

    @NotNull
    public static EquippedItem parseEquipped(JsonObject elementObject) {
        int id = elementObject.get("id").getAsInt();

        int[] gems = null;
        if (elementObject.has("gems")) {
            JsonArray array = elementObject.get("gems").getAsJsonArray();
            gems = array.asList().stream().mapToInt(JsonElement::getAsInt).toArray();
        }

        Integer enchant = null;
        if (elementObject.has("enchant")) {
            enchant = elementObject.get("enchant").getAsInt();
        }

        int upgradeStep = 0;
        if (elementObject.has("upgrade_step")) {
            upgradeStep = elementObject.get("upgrade_step").getAsInt();
        }

        return new EquippedItem(id, gems, enchant, upgradeStep);
    }
}
