package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.SourcesOfItems;
import au.nerago.mopgear.model.ModelCombined;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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

    @SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
    public void run(ServiceParam params) {
        Path gearFile = Path.of(params.gearFile);
        ModelCombined model = ModelCombined.load(params.model);
        Map<Integer, List<ReforgeRecipe>> fixedForges = new HashMap<>(params.fixedForges);
        EquipOptionsMap items = ItemLoadUtil.readAndLoad(gearFile, model, fixedForges, false);

        switch (params.taskType) {
            case REFORGE -> {
                Tasks.reforgeProcess(items, model, null);
            }
            case EXTRA_ITEMS -> {
                Tasks.reforgeProcessPlusMany(items, model, null, params.extraItems, params.upgradeLevel, false, null);
            }
            case EXTRA_BAGS -> {
                Path bagsFile = Path.of(params.bagFile);
                Tasks.reforgeProcessPlusMany(items, model, null, SourcesOfItems.bagItemsArray(bagsFile, SourcesOfItems.ignoredItems));
            }
            case FIND_UPGRADE -> {
                CostedItem[] extraItems = params.extraItems();
                if (extraItems == null && params.sourceOfItems != null) {
                    extraItems = SourcesOfItems.get(params.sourceOfItems());
                } else {
                    throw new IllegalArgumentException("no upgrade items specified");
                }
                Tasks.findUpgrade(items, extraItems, model, false, null, params.upgradeLevel);
            }
        }
    }

    public record ServiceParam(String gearFile,
                                String bagFile,
                                ServiceModel model,
                                Map<Integer, List<ReforgeRecipe>> fixedForges,
                                boolean challengeModeScaling,
                                ServiceTaskType taskType,
                                CostedItem[] extraItems,
                                String sourceOfItems,
                                Integer upgradeLevel
                               ) {
    }

    public record ServiceModel(SpecType spec,
                               List<ServiceWeightStats> weight,
                                ServiceRequiredStats required,
                                List<StatType> reforgeTargets,
                                Map<SlotItem, StatBlock> defaultEnchants,
                                boolean blacksmith) {
    }

    public enum ServiceTaskType {
        REFORGE, EXTRA_ITEMS, EXTRA_BAGS, FIND_UPGRADE
    }

    public record ServiceWeightStats(String file, int scale) {
    }

    public record ServiceRequiredStats(int hit, int expertise, int allowedExceed, boolean combinedHit) {
    }
}
