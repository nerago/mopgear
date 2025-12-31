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
    @Deprecated
    public static void writeToOutBasic(EquipMap map) {
        OutputText.print("{\"player\":{\"equipment\":{\"items\":[");
        map.forEachValue(AsWowSimJson::writeItemBasic);
        OutputText.println("]}}}");
    }

    private static void writeItemBasic(FullItemData item) {
        if (!item.reforge.isEmpty()) {
            int reforgeId = WowSimDB.instance.reforgeId(item.reforge);
            OutputText.printf("{\"id\":%d,\"reforging\":%d,\"upgrade_step\":%d},", item.itemId(), reforgeId, item.shared.ref().upgradeLevel());
        } else {
            OutputText.printf("{\"id\":%d,\"upgrade_step\":%d},", item.itemId(), item.shared.ref().upgradeLevel());
        }
    }

    private static JsonObject makeItemObject(FullItemData item) {
        JsonObject object = new JsonObject();
        object.add("id", new JsonPrimitive(item.itemId()));
        object.add("upgrade_step", new JsonPrimitive(item.shared.ref().upgradeLevel()));
        if (!item.reforge.isEmpty()) {
            int reforgeId = WowSimDB.instance.reforgeId(item.reforge);
            object.add("reforging", new JsonPrimitive(reforgeId));
        }

        if (item.gemChoice != null && !item.gemChoice.isEmpty()) {
            JsonArray gemArray = new JsonArray();
            for (StatBlock gemStat : item.gemChoice) {
                int gemId = GemData.reverseLookup(gemStat, item.shared.primaryStatType());
                gemArray.add(gemId);
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
            writeToOutBasic(map);
            return;
        }

        JsonObject sampleData = InputGearParser.asParsed(gearFile);
        JsonObject gearData = sampleData.getAsJsonObject("gear");
        JsonArray itemArray = gearData.getAsJsonArray("items");
        while (!itemArray.isEmpty()) {
            itemArray.remove(0);
        }

        map.forEachValue(item -> {
            itemArray.add(makeItemObject(item));
        });

        OutputText.println(sampleData.toString());

//        OutputText.print("""
//                {
//                    "talents":"113213",
//                    "glyphs":{"minor":[{"spellID":57947},{"spellID":57954},{"spellID":57979}],"major":[{"spellID":54935},{"spellID":63222},{"spellID":54924}]},
//                    "id":"Player-4385-05E852E3",
//                    "class":"paladin",
//                    "unit":"player",
//                    "professions":[{"name":"Blacksmithing","level":600},{"name":"Engineering","level":600}],
//                    "race":"BloodElf",
//                    "name":"Neravi",
//                    "spec":"protection",
//                    "gear":{
//                    "items":[""");
//        map.forEachValue(AsWowSimJson::writeItem);
//        //{"id":87101,"reforging":139}
//        //{"id":87101,"reforging":139,"upgrade_step":2,"gems":[76886,76576]}
//        OutputText.println("""
//                ],
//                "version":"v3.1.5"},
//                "level":90,"version":"v3.1.5","realm":"Galakras"}""");
    }
}
