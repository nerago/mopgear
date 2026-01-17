package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.DataLocation;
import au.nerago.mopgear.io.InputGearParser;
import au.nerago.mopgear.io.StandardModels;
import au.nerago.mopgear.io.WowSimDB;
import au.nerago.mopgear.model.DefaultEnchants;
import au.nerago.mopgear.model.GemData;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.Tuple;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.List;

public class AsWowSimJson {
    // only good for reforgelite, not wowsim
    // seems not to like that even that with newer versions
//    public static void writeToOutBasic(EquipMap map) {
//        OutputText.print("{\"player\":{\"equipment\":{\"items\":[");
//        map.forEachValue(AsWowSimJson::writeItemBasic);
//        OutputText.println("]}}}");
//    }
//
//    private static void writeItemBasic(FullItemData item) {
//        if (!item.reforge.isEmpty()) {
//            int reforgeId = WowSimDB.instance.reforgeId(item.reforge);
//            OutputText.printf("{\"id\":%d,\"reforging\":%d,\"upgrade_step\":%d},", item.itemId(), reforgeId, item.shared.ref().upgradeLevel());
//        } else {
//            OutputText.printf("{\"id\":%d,\"upgrade_step\":%d},", item.itemId(), item.shared.ref().upgradeLevel());
//        }
//    }

    public static JsonObject makeItemObject(FullItemData item) {
        JsonObject object = new JsonObject();
        object.add("id", new JsonPrimitive(item.itemId()));
        object.add("upgrade_step", new JsonPrimitive(item.shared.ref().upgradeLevel()));
        if (!item.reforge.isEmpty()) {
            int reforgeId = WowSimDB.instance.reforgeId(item.reforge);
            object.add("reforging", new JsonPrimitive(reforgeId));
        }

        if (item.gemChoice != null && !item.gemChoice.isEmpty()) {
            JsonArray gemArray = new JsonArray();
            for (GemInfo gemInfo : item.gemChoice) {
                gemArray.add(gemInfo.gemId());
            }
            object.add("gems", gemArray);
        }

        if (item.slot() != SlotItem.Trinket) {
            if (item.enchantChoice != null) {
                object.add("enchant", new JsonPrimitive(item.enchantChoice));
            }

            StatBlock expectedEnchants = GemData.process(item.gemChoice, item.enchantChoice, item.shared.socketSlots(), item.shared.socketBonus(), item.shared.name(), item.slot().possibleBlacksmith());
            if (!expectedEnchants.equalsStats(item.statEnchant)) {
                throw new RuntimeException("enchant details don't match");
            }
        }

        if (item.randomSuffix != null) {
            object.add("random_suffix", new JsonPrimitive(item.randomSuffix));
        }

        // NOTE tinker don't bother, just synapse springs of interest

        return object;
    }

    public static void writeFullToOut(EquipMap map, ModelCombined model) {
        Path gearFile;
        if (model.spec() == SpecType.PaladinRet) {
            gearFile = DataLocation.gearRetFile;
        } else if (model.spec() == SpecType.PaladinProtDps ) {
            gearFile = DataLocation.gearProtDpsFile;
        } else if (model.spec() == SpecType.PaladinProtMitigation) {
            gearFile = DataLocation.gearProtDefenceFile;
        } else {
            throw new RuntimeException("unknown gear file");
        }

        JsonObject sampleData = InputGearParser.asParsed(gearFile);

        // gear data, wowsim friendly
        JsonObject gearData = sampleData.getAsJsonObject("gear");
        JsonArray itemArray = gearData.getAsJsonArray("items");
        while (!itemArray.isEmpty()) {
            itemArray.remove(0);
        }
        map.forEachValue(item -> {
            itemArray.add(makeItemObject(item));
        });

        // equipment data, reforgelite friendly
        JsonObject playerData = new JsonObject();
        sampleData.add("player", playerData);
        JsonObject equipmentData = new JsonObject();
        playerData.add("equipment", equipmentData);
        equipmentData.add("items", itemArray); // reuse same item array

        OutputText.println(sampleData.toString());
    }
}
