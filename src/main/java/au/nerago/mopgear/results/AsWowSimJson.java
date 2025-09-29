package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.io.WowSimDB;

public class AsWowSimJson {
    public static void writeToOut(EquipMap map) {
        OutputText.print("{\"player\":{\"equipment\":{\"items\":[");
        map.forEachValue(AsWowSimJson::writeItem);
        OutputText.println("]}}}");
    }

    private static void writeItem(ItemData item) {
        OutputText.printf("{\"id\":%d,", item.ref.itemId());
        if (!item.reforge.isEmpty()) {
            int reforgeId = WowSimDB.instance.reforgeId(item.reforge);
            OutputText.printf("\"reforging\":%d", reforgeId);
        }
        OutputText.print("},");
    }
}
