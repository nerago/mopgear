package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.EquippedItem;
import au.nerago.mopgear.results.OutputText;
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

public class SimOutputReader {
    public static void readInput(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            parseReader(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void parseReader(Reader reader) {
        List<EquippedItem> result = new ArrayList<>();
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonObject party = root.getAsJsonObject("raidMetrics").getAsJsonArray("parties").get(0).getAsJsonObject();
        JsonObject player = party.getAsJsonArray("players").get(0).getAsJsonObject();
        double dps = player.getAsJsonObject("dps").getAsJsonPrimitive("avg").getAsDouble();
        double tps = player.getAsJsonObject("threat").getAsJsonPrimitive("avg").getAsDouble();
        double dtps = player.getAsJsonObject("dtps").getAsJsonPrimitive("avg").getAsDouble();
        double tmi = player.getAsJsonObject("tmi").getAsJsonPrimitive("avg").getAsDouble();
        double hps = player.getAsJsonObject("hps").getAsJsonPrimitive("avg").getAsDouble();
        double death = player.getAsJsonPrimitive("chanceOfDeath").getAsDouble();

        OutputText.printf("%.2f\n", dps);
        OutputText.printf("%.2f\n", tps);
        OutputText.printf("%.2f\n", dtps);
        OutputText.printf("%.2f\n", hps);
        OutputText.printf("%.2f\n", tmi);
        OutputText.printf("%.2f\n", death * 100);
        OutputText.println();
    }
}
