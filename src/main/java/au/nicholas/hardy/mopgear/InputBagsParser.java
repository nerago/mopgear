package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.Tuple;
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
import java.util.Arrays;
import java.util.List;

public class InputBagsParser {
    static Tuple.Tuple2<Integer, Integer>[] readInput(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return parseReader(reader);
        }
    }

    @SuppressWarnings("unchecked")
    private static Tuple.Tuple2<Integer, Integer>[] parseReader(Reader reader) {
        List<Tuple.Tuple2<Integer, Integer>> result = new ArrayList<>();
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray items = root.getAsJsonArray("items");
        for (JsonElement element : items) {
            if (element.isJsonObject()) {
                JsonObject elementObject = element.getAsJsonObject();
                int id = elementObject.get("id").getAsInt();
                result.add(Tuple.create(id, 0));
            }
        }
        return (Tuple.Tuple2<Integer, Integer>[]) result.toArray(Tuple.Tuple2[]::new);
    }
}
