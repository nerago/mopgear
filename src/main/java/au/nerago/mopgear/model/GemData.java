package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.SocketType;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.util.BestHolder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToLongFunction;

public class GemData {
    private static final Map<Integer, StatBlock> standardGems = buildGemsStandard();
    private static final Map<Integer, StatBlock> knownGems = buildGems();
    private static final Map<Integer, StatBlock> knownEnchants = buildEnchant();
    private static final Map<Integer, StatBlock> knownSocketBonus = buildSocketBonus();

    private static Map<Integer, StatBlock> buildSocketBonus() {
        Map<Integer, StatBlock> map = new HashMap<>();
        map.put(4860, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4838, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 60));
        map.put(4853, new StatBlock(120, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4868, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4832, new StatBlock(0, 90, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4839, new StatBlock(0, 0, 0, 0, 0, 0, 0, 60, 0, 0));
        map.put(4844, new StatBlock(0, 0, 0, 0, 0, 0, 0, 120, 0, 0));
        map.put(4851, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4833, new StatBlock(0, 0, 0, 60, 0, 0, 0, 0, 0, 0));
        map.put(4855, new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
        map.put(4845, new StatBlock(0, 0, 0, 0, 0, 0, 120, 0, 0, 0));
        map.put(4840, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 60, 0));
        map.put(4830, new StatBlock(60, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4835, new StatBlock(0, 0, 0, 0, 60, 0, 0, 0, 0, 0));
        map.put(4836, new StatBlock(0, 0, 0, 0, 0, 60, 0, 0, 0, 0));
        map.put(4867, new StatBlock(0, 270, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4846, new StatBlock(0, 0, 0, 0, 0, 120, 0, 0, 0, 0));
        map.put(4854, new StatBlock(0, 180, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4831, new StatBlock(80, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4848, new StatBlock(120, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4858, new StatBlock(160, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4852, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 120));
        map.put(4842, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4863, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4834, new StatBlock(0, 0, 60, 0, 0, 0, 0, 0, 0, 0));
        map.put(4850, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 120, 0));
        return map;
    }

    private static Map<Integer, StatBlock> buildGems() {
        Map<Integer, StatBlock> map = new HashMap<>();
        gemsMeta(map);
        gemsEngineering(map);
        gemsStandard(map);
        return map;
    }

    private static Map<Integer, StatBlock> buildGemsStandard() {
        Map<Integer, StatBlock> map = new HashMap<>();
        gemsStandard(map);
        return map;
    }

    private static void gemsStandard(Map<Integer, StatBlock> map) {
        map.put(76628, new StatBlock(160, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(76694, new StatBlock(160, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(76618, new StatBlock(80, 0, 0, 0, 160, 0, 0, 0, 0, 0));
        map.put(76682, new StatBlock(80, 0, 0, 0, 160, 0, 0, 0, 0, 0));
        map.put(76669, new StatBlock(80, 0, 0, 0, 0, 160, 0, 0, 0, 0));
        map.put(76603, new StatBlock(80, 0, 0, 0, 0, 160, 0, 0, 0, 0));
        map.put(76700, new StatBlock(0, 0, 320, 0, 0, 0, 0, 0, 0, 0));
        map.put(76570, new StatBlock(0, 0, 0, 0, 320, 0, 0, 0, 0, 0));
        map.put(76636, new StatBlock(0, 0, 0, 0, 320, 0, 0, 0, 0, 0));
        map.put(76642, new StatBlock(0, 0, 0, 0, 160, 160, 0, 0, 0, 0));
        map.put(76576, new StatBlock(0, 0, 0, 0, 160, 160, 0, 0, 0, 0));
        map.put(76615, new StatBlock(0, 0, 0, 0, 160, 0, 160, 0, 0, 0));
        map.put(76681, new StatBlock(0, 0, 0, 0, 160, 0, 160, 0, 0, 0));
        map.put(76699, new StatBlock(0, 0, 0, 0, 0, 320, 0, 0, 0, 0));
        map.put(76633, new StatBlock(0, 0, 0, 0, 0, 320, 0, 0, 0, 0));
        map.put(76667, new StatBlock(0, 0, 0, 0, 0, 160, 160, 0, 0, 0));
        map.put(76601, new StatBlock(0, 0, 0, 0, 0, 160, 160, 0, 0, 0));
        map.put(76585, new StatBlock(0, 0, 0, 0, 0, 160, 0, 0, 0, 160));
        map.put(76627, new StatBlock(0, 0, 0, 0, 0, 0, 320, 0, 0, 0));
        map.put(76693, new StatBlock(0, 0, 0, 0, 0, 0, 320, 0, 0, 0));
    }

    private static void gemsEngineering(Map<Integer, StatBlock> map) {
        map.put(77547, new StatBlock(0, 0, 600, 0, 0, 0, 0, 0, 0, 0));
        map.put(77541, new StatBlock(0, 0, 0, 600, 0, 0, 0, 0, 0, 0));
        map.put(77545, new StatBlock(0, 0, 0, 0, 600, 0, 0, 0, 0, 0));
        map.put(77542, new StatBlock(0, 0, 0, 0, 0, 600, 0, 0, 0, 0));
        map.put(77543, new StatBlock(0, 0, 0, 0, 0, 0, 600, 0, 0, 0));
        map.put(77546, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 600));
    }

    private static void gemsMeta(Map<Integer, StatBlock> map) {
        map.put(76886, new StatBlock(216, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(76885, new StatBlock(216, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(76895, new StatBlock(0, 324, 0, 0, 0, 0, 0, 0, 0, 0)); // tank meta, stam
    }

    private static Map<Integer, StatBlock> buildEnchant() {
        Map<Integer, StatBlock> map = new HashMap<>();
        map.put(4419, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 80)); // chest stats
        map.put(4411, new StatBlock(0, 0, 170, 0, 0, 0, 0, 0, 0, 0)); // bracer
        map.put(4432, new StatBlock(170, 0, 0, 0, 0, 0, 0, 0, 0, 0)); // glove
        map.put(4426, new StatBlock(0, 0, 0, 0, 0, 175, 0, 0, 0, 0)); // foot
        map.put(4099, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)); // weap
        map.put(4441, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)); // weap
        map.put(5001, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)); // shield spike
        map.put(4412, new StatBlock(0, 0, 0, 0, 0, 0, 0, 170, 0, 0)); // bracer
        map.put(4427, new StatBlock(0, 0, 0, 0, 175, 0, 0, 0, 0, 0)); // foot
        map.put(4431, new StatBlock(0, 0, 0, 0, 0, 0, 170, 0, 0, 0)); // hand
        map.put(4805, new StatBlock(0, 0, 0, 0, 0, 0, 0, 100, 0, 0));
        map.put(4422, new StatBlock(0, 200, 0, 0, 0, 0, 0, 0, 0, 0)); // back stam
        map.put(4824, new StatBlock(0, 430, 0, 0, 0, 0, 0, 165, 0, 0)); // leg  tank
        map.put(4803, new StatBlock(200, 0, 0, 100, 0, 0, 0, 0, 0, 0)); // dps shoulder
        map.put(4443, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)); // weapon
        map.put(4420, new StatBlock(0, 300, 0, 0, 0, 0, 0, 0, 0, 0)); // chest stam
        map.put(4421, new StatBlock(0, 0, 0, 0, 180, 0, 0, 0, 0, 0));// cloak hit
        map.put(4993, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 170, 0));// shield
        map.put(4823, new StatBlock(285, 0, 0, 165, 0, 0, 0, 0, 0, 0));// leg dps
        map.put(4429, new StatBlock(0, 0, 140, 0, 0, 0, 0, 0, 0, 0));// panda feet
        map.put(4909, new StatBlock(120, 0, 0, 80, 0, 0, 0, 0, 0, 0));// int shoulder
        map.put(4423, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));// int back
        map.put(4430, new StatBlock(0, 0, 0, 0, 0, 170, 0, 0, 0, 0));// hand
        map.put(4414, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));// int bracer
        map.put(4415, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));// str bracer
        map.put(4892, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));// lightweave
//        map.put(4895, new StatBlock(285, 0, 0, 165, 0, 0, 0, 0, 0, 0));// int leg
        map.put(4826, new StatBlock(285, 0, 0, 165, 0, 0, 0, 0, 0, 0));// int leg
        map.put(4434, new StatBlock(165, 0, 0, 0, 0, 0, 0, 0, 0, 0));// int offhand
        map.put(4424, new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));// int offhand
        return map;
    }

    public static StatBlock process(int[] gemIds, int socketBonus, String name) {
        StatBlock result = StatBlock.empty;
        for (int id : gemIds) {
            StatBlock stats = knownGems.get(id);
            if (stats == null)
                stats = knownEnchants.get(id);
            if (stats == null)
                throw new IllegalArgumentException("unknown gem " + id + " on " + name);
            result = result.plus(stats);
        }
        if (socketBonus != 0) {
            StatBlock bonus = getSocketBonus(name, socketBonus);
            result = result.plus(bonus);
        }

        return result;
    }

    @Deprecated
    public static int standardValue(StatType stat) {
        switch (stat) {
            case Primary -> {
                return 160;
            }
            case Stam -> {
                return 240;
            }
            default -> {
                return 320;
            }
        }
    }

    public static StatBlock getSocketBonus(ItemData item) {
        return getSocketBonus(item.name, item.socketBonus);
    }

    private static StatBlock getSocketBonus(String name, int socketBonus) {
        StatBlock bonus = knownSocketBonus.get(socketBonus);
        if (bonus == null)
            throw new IllegalArgumentException("unknown socket bonus " + socketBonus + " on " + name);
        return bonus;
    }

    public static Map<Integer, StatBlock> standardGems() {
        return standardGems;
    }

    public static void chooseGem(EnumMap<SocketType, StatBlock> map, SocketType socket, ToLongFunction<StatBlock> calcRating) {
        BestHolder<StatBlock> best = new BestHolder<>(StatBlock.empty, 0);
        for (StatBlock block : standardGems().values()) {
            boolean hasMatchingStat = socket == SocketType.General;
            for (StatType type : socket.getMatchingStats()) {
                if (block.get(type) > 0) {
                    hasMatchingStat = true;
                    break;
                }
            }
            if (hasMatchingStat) {
                long rating = calcRating.applyAsLong(block);
                best.add(block, rating);
            }
        }
        map.put(socket, best.get());
    }
}
