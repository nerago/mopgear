package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.CostedItem;
import au.nerago.mopgear.util.Tuple;
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
    static CostedItem[] readInput(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return parseReader(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static CostedItem[] parseReader(Reader reader) {
        List<CostedItem> result = new ArrayList<>();
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray items = root.getAsJsonArray("items");
        for (JsonElement element : items) {
            if (element.isJsonObject()) {
                JsonObject elementObject = element.getAsJsonObject();
                int id = elementObject.get("id").getAsInt();
                result.add(new CostedItem(id, 0));
            }
        }
        return result.toArray(CostedItem[]::new);
    }
}
