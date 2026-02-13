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
    public static final Map<Integer, StatBlock> standardGems = buildGemsStandard();
    public static final Map<Integer, StatBlock> engineerGems = buildGemsEngineer();
    public static final Map<Integer, StatBlock> knownGems = buildGems();
    public static final Map<Integer, StatBlock> knownEnchants = buildEnchant();
    public static final Map<Integer, StatBlock> knownSocketBonus = buildSocketBonus();
    public static final Map<Integer, StatBlock> allKnown = ArrayUtil.combineMaps(knownGems, knownEnchants);

    private static void addGem0(Map<Integer, StatBlock> map, int id) {
        map.put(id, StatBlock.empty);
    }

    private static void addGem1(Map<Integer, StatBlock> map, int id, StatType stat, int value) {
        map.put(id, StatBlock.of(stat, value));
    }

    private static void addGem2(Map<Integer, StatBlock> map, int id, StatType statA, int valueA, StatType statB, int valueB) {
        map.put(id, StatBlock.of(statA, valueA, statB, valueB));
    }

    private static void addGemBlock(Map<Integer, StatBlock> map, int id, StatBlock block) {
        map.put(id, block);
    }

    private static Map<Integer, StatBlock> buildSocketBonus() {
        Map<Integer, StatBlock> map = new HashMap<>();
        addGem1(map, 4827, Primary, 60);
        addGem1(map, 4828, Primary, 120);
        addGem1(map, 4829, Primary, 180);
        addGem1(map, 4830, Primary, 60);
        addGem1(map, 4831, Primary, 80);
        addGem1(map, 4832, Stam, 90);
        addGem1(map, 4833, Crit, 60);
        addGem1(map, 4834, Mastery, 60);
        addGem1(map, 4835, Hit, 60);
        addGem1(map, 4836, Haste, 60);
        addGem1(map, 4837, Expertise, 60);
        addGem1(map, 4838, Spirit, 60);
        addGem1(map, 4839, Dodge, 60);
        addGem1(map, 4840, Parry, 60);
        addGem0(map, 4842);
        addGem1(map, 4843, Crit, 120);
        addGem1(map, 4844, Dodge, 120);
        addGem1(map, 4845, Expertise, 120);
        addGem1(map, 4846, Haste, 120);
        addGem1(map, 4848, Primary, 120);
        addGem1(map, 4850, Parry, 120);
        addGem0(map, 4851);
        addGem1(map, 4852, Spirit, 120);
        addGem1(map, 4853, Primary, 120);
        addGem1(map, 4854, Stam, 180);
        addGem1(map, 4855, Crit, 180);
        addGem1(map, 4858, Haste, 180);
        addGem1(map, 4860, Primary, 180);
        addGem0(map, 4863);
        addGem1(map, 4867, Stam, 270);
        addGem1(map, 4868, Primary, 180);
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
        addGem2(map, 76537, Primary, 60, Haste, 120);
        addGem1(map, 76570, Hit, 320);
        addGem2(map, 76576, Hit, 160, Haste, 160);
        addGem2(map, 76585, Haste, 160, Spirit, 160);
        addGem2(map, 76588, Stam, 120, Haste, 160);
        addGem2(map, 76593, Crit, 160, Expertise, 160);
        addGem2(map, 76601, Haste, 160, Expertise, 160);
        addGem2(map, 76603, Primary, 80, Haste, 160);
        addGem2(map, 76606, Primary, 80, Mastery, 160);
        addGem2(map, 76615, Hit, 160, Expertise, 160);
        addGem2(map, 76618, Primary, 80, Hit, 160);
        addGem1(map, 76627, Expertise, 320);
        addGem1(map, 76628, Primary, 160);
        addGem1(map, 76633, Haste, 320);
        addGem1(map, 76636, Hit, 320);
        addGem2(map, 76642, Hit, 160, Haste, 160);
        addGem2(map, 76654, Stam, 120, Haste, 160);
        addGem2(map, 76667, Haste, 160, Expertise, 160);
        addGem2(map, 76668, Primary, 80, Haste, 160);
        addGem2(map, 76669, Primary, 80, Haste, 160);
        addGem2(map, 76681, Hit, 160, Expertise, 160);
        addGem2(map, 76682, Primary, 80, Hit, 160);
        addGem2(map, 76686, Primary, 80, Spirit, 160);
        addGem1(map, 76693, Expertise, 320);
        addGem1(map, 76694, Primary, 160);
        addGem1(map, 76697, Crit, 320);
        addGem1(map, 76699, Haste, 320);
        addGem1(map, 76700, Mastery, 320);
    }

    private static void gemsEngineering(Map<Integer, StatBlock> map) {
        addGem1(map, 77541, Crit, 600);
        addGem1(map, 77542, Haste, 600);
        addGem1(map, 77543, Expertise, 600);
        addGem1(map, 77545, Hit, 600);
        addGem1(map, 77546, Spirit, 600);
        addGem1(map, 77547, Mastery, 600);
    }

    private static void gemsMeta(Map<Integer, StatBlock> map) {
        addGem1(map,76885,Primary,216);
        addGem1(map,76886,Primary,216);
        addGem1(map,76895,Stam,324);
        map.put(95344, StatBlock.of(Stam, 324));
        map.put(95346, StatBlock.of(Crit, 324));
    }

    private static void gemsShaTouched(Map<Integer, StatBlock> map) {
        map.put(89881, StatBlock.of(StatType.Primary, 500));
    }

    private static Map<Integer, StatBlock> buildEnchant() {
        Map<Integer, StatBlock> map = new HashMap<>();
        addGem0(map, 4099);
        addGem1(map, 4411, Mastery, 170);
        addGem1(map, 4412, Dodge, 170);
        addGem1(map, 4414, Primary, 180);
        addGem1(map, 4415, Primary, 180);
        addGemBlock(map, 4419, new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 80));
        addGem1(map, 4420, Stam, 300);
        addGem1(map, 4421, Hit, 180);
        addGem1(map, 4422, Stam, 200);
        addGem1(map, 4423, Primary, 180);
        addGem1(map, 4424, Crit, 180);
        addGem1(map, 4426, Haste, 175);
        addGem1(map, 4427, Hit, 175);
        addGem1(map, 4429, Mastery, 140);
        addGem1(map, 4430, Haste, 170);
        addGem1(map, 4431, Expertise, 170);
        addGem1(map, 4432, Primary, 170);
        addGem1(map, 4433, Mastery, 170);
        addGem1(map, 4434, Primary, 165);
        addGem0(map, 4441);
        addGem0(map, 4443);
        addGem0(map, 4444);
        addGem2(map, 4803, Primary, 200, Crit, 100);
        addGem2(map, 4805, Stam, 300, Dodge, 100);
        addGem2(map, 4806, Primary, 200, Crit, 100);
        addGem2(map, 4823, Primary, 285, Crit, 165);
        addGem2(map, 4824, Stam, 430, Dodge, 165);
        addGem2(map, 4826, Primary, 285, Spirit, 165);
        addGem0(map, 4892);
        addGem2(map, 4909, Primary, 120, Crit, 80);
        addGem1(map, 4993, Parry, 170);
        addGem0(map, 5001);
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
