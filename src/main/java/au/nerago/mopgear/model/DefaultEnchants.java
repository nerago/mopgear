package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.SlotItem;
import au.nerago.mopgear.domain.SpecType;
import au.nerago.mopgear.domain.StatBlock;

import java.util.EnumMap;
import java.util.Map;

public class DefaultEnchants {
    private final Map<SlotItem, StatBlock> map;

    public DefaultEnchants(SpecType spec) {
        this.map = known(spec);
    }

    public DefaultEnchants(Map<SlotItem, StatBlock> map) {
        this.map = map;
    }

    public StatBlock standardEnchant(SlotItem slot) {
        return map.get(slot);
    }

    public static EnumMap<SlotItem, StatBlock> known(SpecType spec) {
        EnumMap<SlotItem, StatBlock> map = new EnumMap<>(SlotItem.class);
        if (spec == SpecType.PaladinRet) {
            map.put(SlotItem.Shoulder, new StatBlock(200, 0, 0, 100, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Back, new StatBlock(0, 0, 0, 0, 180, 0, 0, 0, 0, 0));
            map.put(SlotItem.Chest, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Wrist, new StatBlock(0, 0, 170, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Hand, new StatBlock(170, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Leg, new StatBlock(285, 0, 0, 165, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Foot, new StatBlock(0, 0, 0, 0, 0, 175, 0, 0, 0, 0));
        } else if (spec == SpecType.PaladinProt) {
            map.put(SlotItem.Shoulder, new StatBlock(0, 300, 0, 0, 0, 0, 0, 100, 0, 0));
            map.put(SlotItem.Back, new StatBlock(0, 200, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Chest, new StatBlock(0, 300, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Wrist, new StatBlock(0, 0, 170, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Hand, new StatBlock(0, 0, 0, 0, 0, 0, 170, 0, 0, 0));
            map.put(SlotItem.Leg, new StatBlock(285, 0, 0, 165, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Foot, new StatBlock(0, 0, 140, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Offhand, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 175, 0));
        } else if (spec == SpecType.DruidBoom) {
            map.put(SlotItem.Shoulder, new StatBlock(120, 0, 0, 80, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Back, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Chest, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 80));
            map.put(SlotItem.Wrist, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Hand, new StatBlock(0, 0, 0, 0, 0, 170, 0, 0, 0, 0));
            map.put(SlotItem.Leg, new StatBlock(170, 0, 0, 0, 0, 0, 0, 0, 0, 100));
            map.put(SlotItem.Foot, new StatBlock(0, 0, 140, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Offhand, new StatBlock(165, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        } else {
            throw new IllegalArgumentException("need enchants");
        }

        return map;
    }
}
