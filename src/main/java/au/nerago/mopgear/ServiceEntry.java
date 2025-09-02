package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.Tuple;
import au.nerago.mopgear.io.DataLocation;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.io.SourcesOfItems;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class ServiceEntry {
    public static void main(String[] args) {
        if (args.length != 1 || args[0].isEmpty()) {
            System.out.println("MISSING FILENAME");
            return;
        }

        Path file = Path.of(args[0]);
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            TypeToken<ServiceParam> typeToken = new TypeToken<>() { };
            ServiceParam param = new Gson().fromJson(reader, typeToken);
            new ServiceEntry().run(param);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

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
                CostedItem[] extraItems = params.extraItems();
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
                                CostedItem[] extraItems,
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
