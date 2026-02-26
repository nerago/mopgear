package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.SpecType;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
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
    public static final Path basePath = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\wow-sim-mop\\gen-files");
    private static final Path INPUT_PROT_DPS_FILE = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\wow-sim-mop\\example-prot-dps.json");
    private static final Path INPUT_PROT_MITIGATE_FILE = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\wow-sim-mop\\example-prot-miti.json");
    private static final Path INPUT_RET_FILE = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\wow-sim-mop\\example-ret.json");
    public static final Path BASELINE_FILE = basePath.resolve("out-base.json");

    public static Path outName(StatType statType) {
        return basePath.resolve("out-" + statType + ".json");
    }
    public static Path outName(String tag) {
        return basePath.resolve("out-" + tag + ".json");
    }

    public static Path inputFileFor(SpecType spec) {
        switch (spec) {
            case PaladinProtDps -> {
                return INPUT_PROT_DPS_FILE;
            }
            case PaladinProtMitigation -> {
                return INPUT_PROT_MITIGATE_FILE;
            }
            case PaladinRet -> {
                return INPUT_RET_FILE;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public static Path makeWithBonusStat(SpecType spec, StatType statType, int add, SimSpeed simSpeed) {
        Path outFile = basePath.resolve("in-" + statType + ".json");
        modifyFiles(simSpeed, inputFileFor(spec), outFile, root -> modifyJsonBonusStat(root, statType, add));
        return outFile;
    }

    public static Path makeWithGear(SpecType spec, EquipMap map, String tag, SimSpeed simSpeed) {
        Path outFile = basePath.resolve("in-" + tag + ".json");
        modifyFiles(simSpeed, inputFileFor(spec), outFile, root -> modifyJsonItems(root, map));
        return outFile;
    }

    public static Path basic(SpecType spec, SimSpeed simSpeed) {
        Path outFile = basePath.resolve("in-basic.json");
        modifyFiles(simSpeed, inputFileFor(spec), outFile, _ -> {});
        return outFile;
    }

    private static void modifyFiles(SimSpeed simSpeed, Path inFile, Path outFile, Consumer<JsonObject> modifyJson) {
        try (BufferedReader reader = Files.newBufferedReader(inFile); BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            modifyJson.accept(root);
            changeIterations(root, simSpeed);

            try (JsonWriter jsonWriter = new JsonWriter(writer)) {
                jsonWriter.setFormattingStyle(FormattingStyle.PRETTY);
                Streams.write(root, jsonWriter);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public enum SimSpeed {
        QuickDirty,
        Medium,
        SlowAccurate
    }

    private static void changeIterations(JsonObject root, SimSpeed simSpeed) {
        JsonObject opts = root.getAsJsonObject("simOptions");
        int iterations = switch (simSpeed) {
            case QuickDirty -> 20000;
            case Medium -> 100000;
            case SlowAccurate -> 500000;
        };
        opts.addProperty("iterations", iterations);
    }

    private static void modifyJsonBonusStat(JsonObject root, StatType statType, int add) {
        JsonObject party = root.getAsJsonObject("raid").getAsJsonArray("parties").get(0).getAsJsonObject();
        JsonObject player = party.getAsJsonArray("players").get(0).getAsJsonObject();
        JsonArray stats = player.getAsJsonObject("bonusStats").getAsJsonArray("stats");
        stats.set(statType.simIndex, new JsonPrimitive(add));
    }

    private static void modifyJsonBonusStat(JsonObject root, StatBlock block) {
        JsonObject party = root.getAsJsonObject("raid").getAsJsonArray("parties").get(0).getAsJsonObject();
        JsonObject player = party.getAsJsonArray("players").get(0).getAsJsonObject();
        JsonArray stats = player.getAsJsonObject("bonusStats").getAsJsonArray("stats");
        for (StatType statType : StatType.values()) {
            int value = block.get(statType);
            stats.set(statType.simIndex, new JsonPrimitive(value));
        }
    }

    private static void modifyJsonItems(JsonObject root, EquipMap map) {
        JsonObject party = root.getAsJsonObject("raid").getAsJsonArray("parties").get(0).getAsJsonObject();
        JsonObject player = party.getAsJsonArray("players").get(0).getAsJsonObject();
        JsonArray itemArray = player.getAsJsonObject("equipment").getAsJsonArray("items");
        while (!itemArray.isEmpty())
            itemArray.remove(0);
        map.forEachValueIncludeNulls(item -> {
                if (item != null) {
                    itemArray.add(makeItemObject(item));
                }
            }
        );
    }
}
