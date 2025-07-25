package au.nicholas.hardy.mopgear;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static au.nicholas.hardy.mopgear.Main.cacheFile;

public class ServiceEntry {
    private final ItemCache itemCache;

    public ServiceEntry() throws IOException {
        itemCache = new ItemCache(cacheFile);
    }

    public Collection<ItemSet> run(String jsonString) throws IOException {
        return reforgeProcess(jsonString);
    }

    private Collection<ItemSet> reforgeProcess(String jsonString) throws IOException {
        List<EquippedItem> itemIds = InputParser.readString(jsonString);
        List<ItemData> items;
        synchronized (itemCache) {
            items = ItemUtil.loadItems(itemCache, itemIds);
        }
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);
        return EngineStream.runSolver(reforgedItems, null);
    }
}
