package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.results.AsWowSimJson;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static au.nerago.mopgear.results.AsWowSimJson.makeItemObject;

@SuppressWarnings("SameParameterValue")
public class SimInputModify {
    public static final Path basePath = Path.of("D:\\prog\\wowsim\\gen-files");
    public static final Path INPUT_FILE = Path.of("D:\\prog\\wowsim\\test-cli.json");
    public static final Path BASELINE_FILE = basePath.resolve("out-base.json");

    public static Path outName(StatType statType) {
        return basePath.resolve("out-" + statType + ".json");
    }

    public static Path makeWithBonusStat(StatType statType, int add) {
        Path outFile = basePath.resolve("in-" + statType + ".json");
        modifyFiles(INPUT_FILE, outFile, root -> modifyJsonBonusStat(root, statType, add));
        return outFile;
    }

    public static Path makeWithGear(EquipMap map, String tag) {
        Path outFile = basePath.resolve("in-" + tag + ".json");
        modifyFiles(INPUT_FILE, outFile, root -> modifyJsonItems(root, map));
        return outFile;
    }

    private static void modifyFiles(Path inFile, Path outFile, Consumer<JsonObject> modifyJson) {
        try (BufferedReader reader = Files.newBufferedReader(inFile); BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            modifyJson.accept(root);

            try (JsonWriter jsonWriter = new JsonWriter(writer)) {
                jsonWriter.setFormattingStyle(FormattingStyle.PRETTY);
                Streams.write(root, jsonWriter);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void modifyJsonBonusStat(JsonObject root, StatType statType, int add) {
        JsonObject party = root.getAsJsonObject("raid").getAsJsonArray("parties").get(0).getAsJsonObject();
        JsonObject player = party.getAsJsonArray("players").get(0).getAsJsonObject();
        JsonArray stats = player.getAsJsonObject("bonusStats").getAsJsonArray("stats");
        stats.set(statType.simIndex, new JsonPrimitive(add));
    }

    private static void modifyJsonItems(JsonObject root, EquipMap map) {
        JsonObject party = root.getAsJsonObject("raid").getAsJsonArray("parties").get(0).getAsJsonObject();
        JsonObject player = party.getAsJsonArray("players").get(0).getAsJsonObject();
        JsonArray itemArray = player.getAsJsonObject("equipment").getAsJsonArray("items");
        while (!itemArray.isEmpty())
            itemArray.remove(0);
        map.forEachValue(item -> {
            itemArray.add(makeItemObject(item));
        });
    }
}
