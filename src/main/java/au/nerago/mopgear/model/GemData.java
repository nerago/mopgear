package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ToLongFunction;

import static au.nerago.mopgear.domain.StatType.*;

public class GemData {
    private static final Map<Integer, StatBlock> standardGems = buildGemsStandard();
    private static final Map<Integer, StatBlock> engineerGems = buildGemsEngineer();
    private static final Map<Integer, StatBlock> knownGems = buildGems();
    private static final Map<Integer, StatBlock> knownEnchants = buildEnchant();
    private static final Map<Integer, StatBlock> knownSocketBonus = buildSocketBonus();
    private static final Map<Integer, StatBlock> allKnown = ArrayUtil.combineMaps(knownGems, knownEnchants);

    private static Map<Integer, StatBlock> buildSocketBonus() {
        Map<Integer, StatBlock> map = new HashMap<>();
        map.put(4860, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4829, new StatBlock(180, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4838, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 60));
        map.put(4853, new StatBlock(120, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4828, new StatBlock(120, 0, 0, 0, 0, 0, 0, 0, 0, 0));
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
        map.put(4827, new StatBlock(60, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4835, new StatBlock(0, 0, 0, 0, 60, 0, 0, 0, 0, 0));
        map.put(4836, new StatBlock(0, 0, 0, 0, 0, 60, 0, 0, 0, 0));
        map.put(4867, new StatBlock(0, 270, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4846, new StatBlock(0, 0, 0, 0, 0, 120, 0, 0, 0, 0));
        map.put(4854, new StatBlock(0, 180, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4831, new StatBlock(80, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4848, new StatBlock(120, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4858, new StatBlock(0, 0, 0, 0, 0, 180, 0, 0, 0, 0));
        map.put(4852, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 120));
        map.put(4842, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4863, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(4834, new StatBlock(0, 0, 60, 0, 0, 0, 0, 0, 0, 0));
        map.put(4850, new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 120, 0));
        map.put(4843, new StatBlock(0, 0, 0, 120, 0, 0, 0, 0, 0, 0));
        map.put(4837, new StatBlock(0, 0, 0, 0, 0, 0, 60, 0, 0, 0));
        return map;
    }

    private static Map<Integer, StatBlock> buildGems() {
        Map<Integer, StatBlock> map = new HashMap<>();
        gemsMeta(map);
        gemsEngineering(map);
        gemsStandard(map);
        gemsShaTouched(map);
        return map;
    }

    private static Map<Integer, StatBlock> buildGemsStandard() {
        Map<Integer, StatBlock> map = new HashMap<>();
        gemsStandard(map);
        return map;
    }

    private static Map<Integer, StatBlock> buildGemsEngineer() {
        Map<Integer, StatBlock> map = new HashMap<>();
        gemsEngineering(map);
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
        map.put(76697, new StatBlock(0, 0, 0, 320, 0, 0, 0, 0, 0, 0));
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
        map.put(76593, new StatBlock(0, 0, 0, 160, 0, 0, 160, 0, 0, 0));
        map.put(76606, new StatBlock(80, 0, 160, 0, 0, 0, 0, 0, 0, 0));
        map.put(76668, new StatBlock(80, 0, 0, 0, 0, 160, 0, 0, 0, 0));
        map.put(76537, new StatBlock(60, 0, 0, 0, 0, 120, 0, 0, 0, 0));
        map.put(76686, new StatBlock(80, 0, 0, 0, 0, 0, 0, 0, 0, 160));
        map.put(76588, StatBlock.of(Haste, 160, Stam, 120));
        map.put(76654, StatBlock.of(Haste, 160, Stam, 120));
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
        map.put(68778, new StatBlock(54, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(76886, new StatBlock(216, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(76885, new StatBlock(216, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        map.put(76895, new StatBlock(0, 324, 0, 0, 0, 0, 0, 0, 0, 0)); // tank meta, stam
        map.put(95344, StatBlock.of(Stam, 324));
        map.put(95346, StatBlock.of(Crit, 324));
    }

    private static void gemsShaTouched(Map<Integer, StatBlock> map) {
        map.put(89881, StatBlock.of(StatType.Primary, 500));
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
        map.put(4805, new StatBlock(0, 300, 0, 0, 0, 0, 0, 100, 0, 0));
        map.put(4422, new StatBlock(0, 200, 0, 0, 0, 0, 0, 0, 0, 0)); // back stam
        map.put(4824, new StatBlock(0, 430, 0, 0, 0, 0, 0, 165, 0, 0)); // leg  tank
        map.put(4803, new StatBlock(200, 0, 0, 100, 0, 0, 0, 0, 0, 0)); // dps shoulder
        map.put(4806, new StatBlock(200, 0, 0, 100, 0, 0, 0, 0, 0, 0)); // caster shoulder
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
        map.put(4826, new StatBlock(285, 0, 0, 0, 0, 0, 0, 0, 0, 165));// int leg
        map.put(4434, new StatBlock(165, 0, 0, 0, 0, 0, 0, 0, 0, 0));// int offhand
        map.put(4424, new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));// cloak crit
        map.put(4433, StatBlock.of(StatType.Mastery, 170));
        map.put(4444, StatBlock.empty); // dancing steel
        return map;
    }

    public static StatBlock process(@Nullable List<GemInfo> gemChoice, Integer enchant, SocketType[] socketTypes, StatBlock socketBonus, String name, boolean possibleBlacksmith) {
        boolean socketBonusMet = true;

        StatBlock result = StatBlock.empty;
        if (enchant != null) {
            result = findGemStats(name, enchant);
        }

        for (int i = 0; i < socketTypes.length; ++i) {
            SocketType socket = socketTypes[i];
            GemInfo gemInfo = gemChoice != null ? gemChoice.get(i) : new GemInfo(-1, StatBlock.empty);
            StatBlock gemStat = gemInfo.stat();
            if (!matchesSocket(socket, gemStat)) {
                socketBonusMet = false;
            }

            result = result.plus(gemStat);
        }

        if (gemChoice != null) {
            if (possibleBlacksmith && gemChoice.size() == socketTypes.length + 1) {
                StatBlock gemStat = gemChoice.getLast().stat();
                result = result.plus(gemStat);
            } else if (gemChoice.size() != socketTypes.length) {
                throw new IllegalArgumentException("gem count");
            }
        }

        if (socketBonus != null && socketBonusMet) {
            result = result.plus(socketBonus);
        }

        return result;
    }

    public static Tuple.Tuple2<StatBlock, List<GemInfo>> process(int[] gemIds, Integer enchant, SocketType[] socketTypes, StatBlock socketBonus, String name, boolean possibleBlacksmith) {
        List<GemInfo> gemChoice = Arrays.stream(gemIds).mapToObj(id -> findGemInfo(name, id)).toList();
        StatBlock stat = process(gemChoice, enchant, socketTypes, socketBonus, name, possibleBlacksmith);
        return Tuple.create(stat, gemChoice);
    }

    @NotNull
    private static GemInfo findGemInfo(String name, int id) {
        StatBlock gemStat = findGemStats(name, id);
        return new GemInfo(id, gemStat);
    }

    @NotNull
    private static StatBlock findGemStats(String name, int id) {
        StatBlock gemStat = knownGems.get(id);
        if (gemStat == null)
            gemStat = knownEnchants.get(id);
        if (gemStat == null)
            throw new IllegalArgumentException("unknown gem " + id + " on " + name);
        return gemStat;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean matchesSocket(SocketType socket, StatBlock gemStat) {
        StatType[] statArray = socket.getMatchingStats();
        if (statArray.length == 0)
            return true;

        for (StatType stat : statArray) {
            if (gemStat.get(stat) != 0)
                return true;
        }

        return false;
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

    public static StatBlock getSocketBonus(String name, int socketBonus) {
        StatBlock bonus = knownSocketBonus.get(socketBonus);
        if (bonus == null)
            throw new IllegalArgumentException("unknown socket bonus " + socketBonus + " on " + name);
        return bonus;
    }

    public static Map<Integer, StatBlock> standardGems() {
        return standardGems;
    }

    public static Map<Integer, StatBlock> engineerGems() {
        return engineerGems;
    }

    public static Map<Integer, StatBlock> allGems() {
        return knownGems;
    }

    public static void chooseGem(EnumMap<SocketType, GemInfo> map, SocketType socket, ToLongFunction<StatBlock> calcRating, Map<Integer, StatBlock> available) {
        BestHolder<GemInfo> best = new BestHolder<>();
        for (Map.Entry<Integer, StatBlock> entry : available.entrySet()) {
            StatBlock block = entry.getValue();
            boolean hasMatchingStat = socket == SocketType.General;
            for (StatType type : socket.getMatchingStats()) {
                if (block.get(type) > 0) {
                    hasMatchingStat = true;
                    break;
                }
            }
            if (hasMatchingStat) {
                long rating = calcRating.applyAsLong(block);
                best.add(new GemInfo(entry.getKey(), block), rating);
            }
        }
        map.put(socket, best.orElseThrow());
    }

    public static StatBlock getEnchant(Integer enchant) {
        return knownEnchants.get(enchant);
    }

    public static Integer getEnchantId(StatBlock stat) {
        return knownEnchants.entrySet().stream().filter(entry -> entry.getValue().equalsStats(stat)).findAny().orElseThrow().getKey();
    }

    public static int reverseLookup(StatBlock stat, PrimaryStatType primaryType) {
        if (stat.hasSingleStat() && stat.primary() == 216) {
            if (primaryType == PrimaryStatType.Strength)
                return 76886;
            else
                throw new RuntimeException("unknown meta gem");
        } else if (stat.hasSingleStat() && stat.primary() == 324) {
            throw new RuntimeException("unknown meta gem");
        }

        for (Map.Entry<Integer, StatBlock> entry : allKnown.entrySet()) {
            if (stat.equalsStats(entry.getValue()))
                return entry.getKey();
        }
        throw new RuntimeException("stat/enchant not found " + stat.toStringExtended());
    }

    public static GemInfo getGemInfo(int gemId) {
        StatBlock stat = allKnown.get(gemId);
        if (stat == null)
            throw new RuntimeException("unknown meta gem");
        return new GemInfo(gemId, stat);
    }
}
