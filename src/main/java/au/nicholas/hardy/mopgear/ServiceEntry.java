package au.nicholas.hardy.mopgear;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;

import static au.nicholas.hardy.mopgear.Main.cacheFile;

public class ServiceEntry {
    private final ItemCache itemCache;
    private final ModelCombined model;

    public ServiceEntry() throws IOException {
        itemCache = new ItemCache(cacheFile);
        StatRatingsWeights ratings = new StatRatingsWeights(null, true, 1, 1);
        StatRequirements requirements = new StatRequirements(false);
        model = new ModelCombined(ratings, requirements, ReforgeRules.ret());
    }

    public ItemSet run(String jsonString) {
        return reforgeProcess(jsonString);
    }

    private ItemSet reforgeProcess(String jsonString) {
        List<EquippedItem> itemIds = InputParser.readString(jsonString);
        List<ItemData> items;
        synchronized (itemCache) {
            items = ItemUtil.loadItems(itemCache, itemIds, false);
        }
        EnumMap<SlotEquip, ItemData[]> reforgedItems = ItemUtil.standardItemsReforgedToMap(model.reforgeRules(), items);
        return EngineStream.runSolver(model, reforgedItems, null, null);
    }
}
