package au.nerago.mopgear.io;

import au.nerago.mopgear.util.MapUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BossLookup {
    private static final URL fileUrl = WowSimDB.class.getClassLoader().getResource("bosslookup.tsv");

    private static final Map<Integer, String> itemMap = readInput();
    private static final Map<String, String> nameMap = readInputName();
    private static final Map<Integer, String > bossIdMapInverse = makeBossesMap();
    private static final Map<String, Integer> bossIdMap = MapUtil.inverse(bossIdMapInverse);

    private static Map<Integer, String> makeBossesMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(101,"Dogs MSV");
        map.put(102,"Feng MSV");
        map.put(103,"Gara'jal MSV");
        map.put(104,"Kings MSV");
        map.put(105,"Elegon MSV");
        map.put(106,"Will MSV");
        map.put(301,"Vizier HOF");
        map.put(302,"Blade Lord HOF");
        map.put(303,"Garalon HOF");
        map.put(304,"Wind Lord HOF");
        map.put(305,"Amber-Shaper HOF");
        map.put(306,"Empress HOF");
        map.put(501,"Protectors ToES");
        map.put(502,"Tsulong ToES");
        map.put(503,"Lei Shi ToES");
        map.put(504,"Sha ToES");
        map.put(701,"Jinrokh ToT");
        map.put(702,"Horridon ToT");
        map.put(703,"Council ToT");
        map.put(704,"Tortos ToT");
        map.put(705,"Megaera ToT");
        map.put(706,"Ji-Kun ToT");
        map.put(707,"Durumu ToT");
        map.put(708,"Primordius ToT");
        map.put(709,"Dark Animus ToT");
        map.put(710,"Iron Qon ToT");
        map.put(711,"Twin Consorts ToT");
        map.put(712,"Lei Shen ToT");
        map.put(713,"Raden ToT");
        return map;
    }

    private static @NotNull BufferedReader openFile() throws IOException, URISyntaxException {
        return Files.newBufferedReader(Path.of(fileUrl.toURI()), StandardCharsets.UTF_8);
    }

    private static Map<String, Integer> readInputBosses() {
        int bossIdSequence = 1;
        HashSet<String> seen = new HashSet<>();
        try (BufferedReader reader = openFile()) {
            Map<String, Integer> map = new HashMap<>();
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                String[] parts = line.split("\t");
                if (parts.length != 4)
                    break;
                String boss = parts[3];
                if (seen.add(boss)) {
                    map.put(boss, bossIdSequence++);
                }
            }
            return map;
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Map<String, String> readInputName() {
        try (BufferedReader reader = openFile()) {
            Map<String, String> map = new HashMap<>();
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                String[] parts = line.split("\t");
                if (parts.length != 4)
                    break;
    //            int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                String boss = parts[3];
                map.put(name, boss);
            }
            return map;
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    static Map<Integer, String> readInput() {
        try (BufferedReader reader = openFile()) {
            Map<Integer, String> map = new HashMap<>();
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                if (line.charAt(0) == '\uFEFF') // bloody bom
                    continue;
                String[] parts = line.split("\t");
                if (parts.length != 4)
                    break;
                int id = Integer.parseInt(parts[0]);
                String boss = parts[3];
                map.put(id, boss);
            }
            return map;
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String bossForItemId(int itemId) {
        return itemMap.getOrDefault(itemId, null);
    }

    public static String bossForItemName(String name) {
        return nameMap.getOrDefault(name, null);
    }

    public static int bossIdForItemName(@NotNull String name) {
        String bossName = nameMap.getOrDefault(name, null);
        if (bossName != null) {
            return bossIdMap.getOrDefault(bossName, 0);
        }
        return 0;
    }

    public static String bossNameForBossId(int bossId) {
        return bossIdMapInverse.getOrDefault(bossId, "Unknown");
    }
}
