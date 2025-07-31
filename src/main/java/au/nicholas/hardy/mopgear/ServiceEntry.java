package au.nicholas.hardy.mopgear;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static au.nicholas.hardy.mopgear.Main.cacheFile;

public class ServiceEntry {
    private final ItemCache itemCache;
    private final ModelCombined model;

    public ServiceEntry() throws IOException {
        itemCache = new ItemCache(cacheFile);
        StatRatingsWeights ratings = new StatRatingsWeights(null, true);
        StatRequirements requirements = new StatRequirements(true, false);
        model = new ModelCombined(ratings, requirements, new ReforgeRules());
    }

    public Collection<ItemSet> run(String jsonString) throws IOException {
        return reforgeProcess(jsonString);
    }

    private Collection<ItemSet> reforgeProcess(String jsonString) throws IOException {
        List<EquippedItem> itemIds = InputParser.readString(jsonString);
        List<ItemData> items;
        synchronized (itemCache) {
            items = ItemUtil.loadItems(itemCache, itemIds, false);
        }
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsReforgedToMap(model.getReforgeRules(), items);
        return EngineStream.runSolver(model, reforgedItems, null);
    }
}
