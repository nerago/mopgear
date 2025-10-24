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
            OutputText.printf("{\"id\":%d,\"reforging\":%d},", item.itemId(), reforgeId);
        } else {
            OutputText.printf("{\"id\":%d},", item.itemId());
        }
    }
}
