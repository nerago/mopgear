package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.io.DataLocation;
import au.nicholas.hardy.mopgear.io.ItemCache;
import au.nicholas.hardy.mopgear.io.SourcesOfItems;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class ServiceEntry {
//    private final ItemCache itemCache;
//    private final ModelCombined model;
//
//    public ServiceEntry() throws IOException {
//        itemCache = new ItemCache(cacheFile);
//        StatRatingsWeights ratings = new StatRatingsWeights(null, true, 1, 1);
//        StatRequirements requirements = StatRequirements.ret();
//        model = new ModelCombined(ratings, requirements, ReforgeRules.ret());
//    }
//
//    public ItemSet run(String jsonString) {
//        return reforgeProcess(jsonString).orElse(null);
//    }
//
//    private Optional<ItemSet> reforgeProcess(String jsonString) {
//        List<EquippedItem> itemIds = InputParser.readString(jsonString);
//        List<ItemData> items;
//        synchronized (itemCache) {
//            items = ItemUtil.loadItems(itemCache, itemIds, false);
//        }
//        EnumMap<SlotEquip, ItemData[]> reforgedItems = ItemUtil.standardItemsReforgedToMap(model.reforgeRules(), items);
//        return EngineStream.runSolver(model, reforgedItems, null, null);
//    }

    public void run(ServiceParam params) {
        ItemCache itemCache = new ItemCache(DataLocation.cacheFile);
        Path gearFile = Path.of(params.gearFile);
        ModelCombined model = ModelCombined.load(params.model);
        EnumMap<SlotEquip, ReforgeRecipe> fixedForges = new EnumMap<>(params.fixedForges);
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, false, gearFile, model.reforgeRules(), fixedForges);

        switch (params.jobType) {
            case REFORGE -> {
                Jobs.reforgeProcess(items, model, null);
            }
            case EXTRA_ITEMS -> {
                Jobs.reforgeProcessPlusMany(items, model, null, params.extraItems);
            }
            case EXTRA_BAGS -> {
                Path bagsFile = Path.of(params.bagFile);
                Jobs.reforgeProcessPlusMany(items, model, null, SourcesOfItems.bagItemsArray(model, bagsFile, SourcesOfItems.ignoredItems));
            }
            case FIND_UPGRADE -> {
                Tuple.Tuple2<Integer, Integer>[] extraItems = params.extraItems();
                if (extraItems == null && params.sourceOfItems != null) {
                    extraItems = SourcesOfItems.get(params.sourceOfItems());
                } else {
                    throw new IllegalArgumentException("no upgrade items specified");
                }
                Jobs.findUpgradeSetup(items, extraItems, model, false, null);
            }
        }
    }

    public record ServiceParam(String gearFile,
                                String bagFile,
                                ServiceModel model,
                                Map<SlotEquip, ReforgeRecipe> fixedForges,
                                boolean challengeModeScaling,
                                ServiceJobType jobType,
                                Tuple.Tuple2<Integer, Integer>[] extraItems,
                                String sourceOfItems
                               ) {
    }

    public record ServiceModel(List<ServiceWeightStats> weight,
                                ServiceRequiredStats required,
                                List<StatType> reforgeTargets,
                                Map<SlotItem, StatBlock> defaultEnchants) {
    }

    public enum ServiceJobType {
        REFORGE, EXTRA_ITEMS, EXTRA_BAGS, FIND_UPGRADE
    }

    public record ServiceWeightStats(String file, int scale) {
    }

    public record ServiceRequiredStats(int hit, int expertise, int allowedExceed, boolean combinedHit) {
    }
}
