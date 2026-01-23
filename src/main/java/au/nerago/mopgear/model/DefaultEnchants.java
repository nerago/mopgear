package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.SlotItem;
import au.nerago.mopgear.domain.SpecType;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static au.nerago.mopgear.domain.StatType.*;

public class DefaultEnchants {
    private final Map<SlotItem, StatBlock> map;
    private final boolean blacksmith;

    public DefaultEnchants(SpecType spec, boolean blacksmith) {
        this.map = known(spec);
        this.blacksmith = blacksmith;
    }

    public DefaultEnchants(Map<SlotItem, StatBlock> map, boolean blacksmith) {
        this.map = map;
        this.blacksmith = blacksmith;
    }

    public StatBlock standardEnchant(SlotItem slot) {
        return map.get(slot);
    }

    public List<StatBlock> alternateEnchant(SlotItem slot) {
        return switch (slot) {
            case Shoulder -> Arrays.asList(StatBlock.of(Primary, 200, Crit, 100), StatBlock.of(Stam, 300, Dodge, 100));
            case Back -> Arrays.asList(StatBlock.of(Stam, 200), StatBlock.of(Crit, 180), StatBlock.of(StatType.Hit, 180));
            case Wrist -> Arrays.asList(StatBlock.of(Primary, 180), StatBlock.of(Mastery, 170), StatBlock.of(Dodge, 170));
            case Hand -> Arrays.asList(StatBlock.of(Primary, 170), StatBlock.of(Mastery, 170), StatBlock.of(Expertise, 170));
            case Leg -> Arrays.asList(StatBlock.of(Primary, 285, Crit, 165), StatBlock.of(Stam, 430, Dodge, 165));
            default -> null;
        };
    }

    public static EnumMap<SlotItem, StatBlock> known(SpecType spec) {
        EnumMap<SlotItem, StatBlock> map = new EnumMap<>(SlotItem.class);
        if (spec == SpecType.PaladinRet) {
            map.put(SlotItem.Shoulder, StatBlock.of(Primary, 200, Crit, 100));
            map.put(SlotItem.Back, StatBlock.of(Crit, 180));
            map.put(SlotItem.Chest, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 80));
            map.put(SlotItem.Wrist, StatBlock.of(Primary, 180));
            map.put(SlotItem.Hand, StatBlock.of(Primary, 170));
            map.put(SlotItem.Leg, StatBlock.of(Primary, 285, Crit, 165));
            map.put(SlotItem.Foot, StatBlock.of(Mastery, 140));
        } else if (spec == SpecType.PaladinProtDps) {
            map.put(SlotItem.Shoulder, StatBlock.of(Primary, 200, Crit, 100));
            map.put(SlotItem.Back, StatBlock.of(Crit, 180));
            map.put(SlotItem.Chest, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 80));
            map.put(SlotItem.Wrist, StatBlock.of(Primary, 180));
            map.put(SlotItem.Hand, StatBlock.of(Primary, 170));
            map.put(SlotItem.Leg, StatBlock.of(Primary, 285, Crit, 165));
            map.put(SlotItem.Foot, StatBlock.of(Mastery, 140));
            map.put(SlotItem.Offhand, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 170, 0));
        } else if (spec == SpecType.PaladinProtMitigation) {
            map.put(SlotItem.Shoulder, StatBlock.of(Stam, 300, Dodge, 100));
            map.put(SlotItem.Back, StatBlock.of(Stam, 200));
            map.put(SlotItem.Chest, new StatBlock(0, 300, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Wrist, StatBlock.of(Mastery, 170));
            map.put(SlotItem.Hand, StatBlock.of(Mastery, 170));
            map.put(SlotItem.Leg, StatBlock.of(Stam, 430, Dodge, 165));
            map.put(SlotItem.Foot, StatBlock.of(Mastery, 140));
            map.put(SlotItem.Offhand, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 170, 0));
        } else if (spec == SpecType.DruidBoom) {
            map.put(SlotItem.Shoulder, new StatBlock(120, 0, 0, 80, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Back, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Chest, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 80));
            map.put(SlotItem.Wrist, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Hand, new StatBlock(0, 0, 0, 0, 0, 170, 0, 0, 0, 0));
            map.put(SlotItem.Leg, new StatBlock(285, 0, 0, 0, 0, 0, 0, 0, 0, 165));
            map.put(SlotItem.Foot, StatBlock.of(Mastery, 140));
            map.put(SlotItem.Offhand, new StatBlock(165, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        } else if (spec == SpecType.DruidTree) {
            map.put(SlotItem.Shoulder, new StatBlock(120, 0, 0, 80, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Back, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Chest, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 80));
            map.put(SlotItem.Wrist, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Hand, new StatBlock(0, 0, 170, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Leg, new StatBlock(285, 0, 0, 0, 0, 0, 0, 0, 0, 165));
            map.put(SlotItem.Foot, StatBlock.of(Mastery, 140));
            map.put(SlotItem.Offhand, new StatBlock(165, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        } else if (spec == SpecType.Warlock) {
            map.put(SlotItem.Shoulder, new StatBlock(120, 0, 0, 80, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Back, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Chest, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 80));
            map.put(SlotItem.Wrist, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Hand, new StatBlock(0, 0, 170, 0, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Leg, new StatBlock(285, 0, 0, 165, 0, 0, 0, 0, 0, 0));
            map.put(SlotItem.Foot, StatBlock.of(Mastery, 140));
            map.put(SlotItem.Offhand, new StatBlock(165, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        } else {
            throw new IllegalArgumentException("need enchants");
        }

        return map;
    }

    public boolean isBlacksmith() {
        return blacksmith;
    }
}
