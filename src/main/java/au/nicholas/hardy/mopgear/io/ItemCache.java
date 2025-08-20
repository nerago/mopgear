package au.nicholas.hardy.mopgear.io;

import au.nicholas.hardy.mopgear.domain.ItemData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ItemCache {
    private static final Object writeSync = new Object();
    private final Map<Integer, ItemData> itemCache;
    private final Path file;

    public ItemCache(Path file) throws IOException {
        this.file = file;
        this.itemCache = cacheLoad(file);
    }

    private static Map<Integer, ItemData> cacheLoad(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            TypeToken<Map<Integer, ItemData>> typeToken = new TypeToken<>() {
            };
            return new Gson().fromJson(reader, typeToken);
        }
    }


    public void cacheSave() {
        synchronized (writeSync) {
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                new Gson().toJson(itemCache, writer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public ItemData get(int id) {
        return itemCache.get(id);
    }

    public void put(int id, ItemData item) {
        itemCache.put(id, item);
    }
}
