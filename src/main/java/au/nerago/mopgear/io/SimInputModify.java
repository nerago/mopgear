package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.EquippedItem;
import au.nerago.mopgear.domain.StatType;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimInputModify {
    private static final Path basePath = Path.of("D:\\prog\\wowsim\\gen-files");
    public static final Path INPUT_FILE = Path.of("D:\\prog\\wowsim\\test-cli.json");
    public static final Path BASELINE_FILE = basePath.resolve("out-base.json");

    public static Path outName(StatType statType) {
        return basePath.resolve("out-" + statType + ".json");
    }

    public static Path make(StatType statType, int add) {
        Path outFile = basePath.resolve("in-" + statType + ".json");
        modifyFiles(INPUT_FILE, outFile, statType, add);
        return outFile;
    }

    private static void modifyFiles(Path inFile, Path outFile, StatType statType, int add) {
        try (BufferedReader reader = Files.newBufferedReader(inFile); BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            modifyIO(reader, writer, statType, add);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void modifyIO(Reader reader, Writer writer, StatType statType, int add) throws IOException {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

        modifyJson(statType, add, root);

        try (JsonWriter jsonWriter = new JsonWriter(writer)) {
            Streams.write(root, jsonWriter);
        }
    }

    private static void modifyJson(StatType statType, int add, JsonObject root) {
        JsonObject party = root.getAsJsonObject("raid").getAsJsonArray("parties").get(0).getAsJsonObject();
        JsonObject player = party.getAsJsonArray("players").get(0).getAsJsonObject();
        JsonArray stats = player.getAsJsonObject("bonusStats").getAsJsonArray("stats");
        stats.set(statType.simIndex, new JsonPrimitive(add));
    }
}
