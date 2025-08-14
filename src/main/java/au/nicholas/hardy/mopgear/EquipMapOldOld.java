package au.nicholas.hardy.mopgear;

import java.util.EnumMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EquipMapOldOld {
    private EnumMap<SlotEquip, ItemData> map;

    public EquipMapOldOld() {
        this.map = new EnumMap<>(SlotEquip.class);
    }

    public EquipMapOldOld(EnumMap<SlotEquip, ItemData> map) {
        this.map = map;
    }

    public ItemData get(SlotEquip slot) {
        return map.get(slot);
    }

    public void put(SlotEquip slot, ItemData value) {
        map.put(slot, value);
    }

    public EquipMapOldOld clone() {
        return new EquipMapOldOld(map.clone());
    }

    public void forEachValue(Consumer<ItemData> func) {
        map.values().forEach(func);
    }

    public void forEachPair(BiConsumer<SlotEquip, ItemData> func) {
        map.forEach(func);
    }

    public boolean has(SlotEquip slot) {
        return map.get(slot) != null;
    }
}
