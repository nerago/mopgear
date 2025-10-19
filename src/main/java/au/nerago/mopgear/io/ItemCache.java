package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemRef;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ItemCache {
    private static final Object writeSync = new Object();
    private final Map<ItemRef, ItemData> itemRefLookup;
    private final Map<Integer, Set<ItemData>> itemIdLookup;

    public static final ItemCache instance = new ItemCache();

    private ItemCache() {
        List<ItemData> itemList = cacheLoad();

        this.itemRefLookup = itemList.stream().collect(Collectors.toMap(item -> item.ref, item -> item, (a, b) -> a, HashMap::new));

        this.itemIdLookup = itemList.stream().collect(Collectors.groupingBy(item -> item.ref.itemId(), HashMap::new, Collectors.toSet()));
    }

    private static List<ItemData> cacheLoad() {
        try (BufferedReader reader = Files.newBufferedReader(DataLocation.cacheFile)) {
            TypeToken<List<ItemData>> typeToken = new TypeToken<>() {
            };
            return new Gson().fromJson(reader, typeToken);
        } catch (NoSuchFileException ex) {
            return new ArrayList<>();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void cacheSave() {
        synchronized (writeSync) {
            Path destFile = DataLocation.cacheFile;
            try (BufferedWriter writer = Files.newBufferedWriter(destFile)) {
                List<ItemData> itemList = itemRefLookup.values().stream().toList();
                new Gson().newBuilder().setPrettyPrinting().create().toJson(itemList, writer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void cacheSaveMoveReplace() {
        synchronized (writeSync) {
            Path destFile = DataLocation.cacheFile;
            Path tempFile = destFile.resolveSibling(destFile.getFileName().toString() + ".temp");

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                List<ItemData> itemList = itemRefLookup.values().stream().toList();
                new Gson().newBuilder().setPrettyPrinting().create().toJson(itemList, writer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            try {
                Files.move(tempFile, destFile, REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ItemData get(ItemRef ref) {
        return itemRefLookup.get(ref);
    }

    public ItemData get(int itemId, int upgradeLevel) {
        Set<ItemData> itemList = itemIdLookup.get(itemId);
        if (itemList != null) {
            for (ItemData item : itemList) {
                if (item.ref.upgradeLevel() == upgradeLevel)
                    return item;
            }
        }
        return null;
    }

    public void put(ItemData item) {
        itemRefLookup.put(item.ref, item);
        itemIdLookup.computeIfAbsent(item.ref.itemId(), k -> new HashSet<>()).add(item);
    }

    public void clear() {
        itemRefLookup.clear();
        itemIdLookup.clear();
    }
}
