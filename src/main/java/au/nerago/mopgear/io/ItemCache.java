package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.FullItemData;
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
    private final Map<ItemRef, FullItemData> itemRefLookup;
    private final Map<Integer, Set<FullItemData>> itemIdLookup;

    public static final ItemCache instance = new ItemCache();

    private ItemCache() {
        if (loadFromDisk()) {
            List<FullItemData> itemList = cacheLoad();
            this.itemRefLookup = itemList.stream().collect(Collectors.toMap(item -> item.shared.ref(), item -> item, (a, b) -> a, HashMap::new));
            this.itemIdLookup = itemList.stream().collect(Collectors.groupingBy(FullItemData::itemId, HashMap::new, Collectors.toSet()));
        } else {
            this.itemRefLookup = new HashMap<>();
            this.itemIdLookup = new HashMap<>();
            WowSimDB.instance.itemStream().forEach(this::put);
        }
    }

    private boolean loadFromDisk() {
        return false;
    }

    private static List<FullItemData> cacheLoad() {
        try (BufferedReader reader = Files.newBufferedReader(DataLocation.cacheFile)) {
            TypeToken<List<FullItemData>> typeToken = new TypeToken<>() {
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
                List<FullItemData> itemList = itemRefLookup.values().stream().toList();
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
                List<FullItemData> itemList = itemRefLookup.values().stream().toList();
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

    public FullItemData get(ItemRef ref) {
        return itemRefLookup.get(ref);
    }

    public FullItemData get(int itemId, int upgradeLevel) {
        Set<FullItemData> itemList = itemIdLookup.get(itemId);
        if (itemList != null) {
            for (FullItemData item : itemList) {
                if (item.ref().upgradeLevel() == upgradeLevel)
                    return item;
            }
        }
        return null;
    }

    public void put(FullItemData item) {
        itemRefLookup.put(item.ref(), item);
        itemIdLookup.computeIfAbsent(item.itemId(), k -> new HashSet<>()).add(item);
    }

    public void clear() {
        itemRefLookup.clear();
        itemIdLookup.clear();
    }
}
