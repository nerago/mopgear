package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.EquippedItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InputBagsParser {
    static List<EquippedItem> readInput(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return parseReader(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<EquippedItem> parseReader(Reader reader) {
        List<EquippedItem> result = new ArrayList<>();
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray items = root.getAsJsonArray("items");
        for (JsonElement element : items) {
            if (element.isJsonObject()) {
                JsonObject elementObject = element.getAsJsonObject();
                EquippedItem item = InputGearParser.parseEquipped(elementObject);
                result.add(item);
            }
        }
        return result;
    }
}
