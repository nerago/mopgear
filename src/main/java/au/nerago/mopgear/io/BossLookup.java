package au.nerago.mopgear.io;

import au.nerago.mopgear.util.MapUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BossLookup {
    private static final URL fileUrl = WowSimDB.class.getClassLoader().getResource("bosslookup.tsv");

    private static Map<Integer, String> itemMap = readInput();
    private static Map<String, String> nameMap = readInputName();
    private static Map<String, Integer> bossIdMap = readInputBosses();
    private static Map<Integer, String > bossIdMapInverse = MapUtil.inverse(bossIdMap);

    private static Map<String, Integer> readInputBosses() {
        int bossIdSequence = 1;
        HashSet<String> seen = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileUrl.toURI()))) {
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
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileUrl.toURI()))) {
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
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileUrl.toURI()))) {
            Map<Integer, String> map = new HashMap<>();
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
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
