package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.FullItemData;
import au.nerago.mopgear.io.WowSimDB;

public class AsWowSimJson {
    public static void writeToOut(EquipMap map) {
        OutputText.print("{\"player\":{\"equipment\":{\"items\":[");
        map.forEachValue(AsWowSimJson::writeItem);
        OutputText.println("]}}}");
    }

    private static void writeItem(FullItemData item) {
        if (!item.reforge.isEmpty()) {
            int reforgeId = WowSimDB.instance.reforgeId(item.reforge);
            OutputText.printf("{\"id\":%d,\"reforging\":%d,\"upgrade_step\":%d},", item.itemId(), reforgeId, item.shared.ref().upgradeLevel());
        } else {
            OutputText.printf("{\"id\":%d},", item.itemId());
        }
    }

    public static void writeFullToOut(EquipMap map) {
        OutputText.print("""
                {
                    "talents":"113213",
                    "glyphs":{"minor":[{"spellID":57947},{"spellID":57954},{"spellID":57979}],"major":[{"spellID":54935},{"spellID":63222},{"spellID":54924}]},
                    "id":"Player-4385-05E852E3",
                    "class":"paladin",
                    "unit":"player",
                    "professions":[{"name":"Blacksmithing","level":600},{"name":"Engineering","level":600}],
                    "race":"BloodElf",
                    "name":"Neravi",
                    "spec":"protection",
                    "gear":{
                    "items":[""");
        map.forEachValue(AsWowSimJson::writeItem);
        //{"id":87101,"reforging":139}
        //{"id":87101,"reforging":139,"upgrade_step":2,"gems":[76886,76576]}
        OutputText.println("""
                ],
                "version":"v3.1.5"},
                "level":90,"version":"v3.1.5","realm":"Galakras"}""");
    }
}
