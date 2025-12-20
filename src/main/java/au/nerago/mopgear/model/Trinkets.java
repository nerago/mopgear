package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.FullItemData;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static au.nerago.mopgear.domain.StatType.*;

public class Trinkets {
    private static final Map<Integer, TrinketData> knownTrinkets = buildKnown();

    private static Map<Integer, TrinketData> buildKnown() {
        Map<Integer, TrinketData> map = new HashMap<>();
        map.put(89083, new TrinketData(Haste, 3595, 15, 60, null)); // wok 489
        map.put(89079, new TrinketData(Mastery, 3595, 15, 60, null)); // lao courage 489
        map.put(86802, new TrinketData(Primary, 2866, 20, 55, 15)); // lei shen 476
        map.put(86144, new TrinketData(Primary, 3236, 20, 55, 15)); // lei shen 489
        map.put(87072, new TrinketData(Primary, 3653, 20, 55, 15)); // lei shen 502
        map.put(86894, new TrinketData(Haste, 6121, 20, 115, 15)); // darkmist 483
        map.put(86336, new TrinketData(Haste, 6908, 20, 115, 15)); // darkmist 496
        map.put(87172, new TrinketData(Haste, 7796, 20, 115, 15)); // darkmist 509
        map.put(86881, new TrinketData(Dodge, 6121, 20, 115, 15)); // nightmares 483
        map.put(86323, new TrinketData(Dodge, 6908, 20, 115, 15)); // nightmares 496
        map.put(87160, new TrinketData(Dodge, 7796, 20, 115, 15)); // nightmares 509
        map.put(79329, new TrinketData(Dodge, 8871, 10, 60, null)); // relic of niuzao
        map.put(79327, new TrinketData(Primary, 3027, 15, 55, 20)); // relic of xuen
        map.put(86131, new TrinketData(Dodge, 3236, 20, 55, 15)); // vial of dragon's 489
        map.put(87063, new TrinketData(Dodge, 3653, 20, 55, 15)); // vial of dragon's 502
        map.put(86042, new TrinketData(Haste, 3595, 15, 60, null)); // jade charioteer 489
        map.put(86046, new TrinketData(Mastery, 3595, 15, 60, null)); // jade warlord 489
        map.put(81138, new TrinketData(Crit, 3386, 30, 115, 15)); // carbonic carbuncle
        map.put(81268, new TrinketData(Primary, 4232, 20, 120, null)); // lessons of darkmaster


        map.put(86792, new TrinketData(Primary, 2866, 20, 55, 15)); // light cosmos 476
        map.put(86907, new TrinketData(Haste, 6121, 20, 115, 15)); // essence terror
        map.put(86773, new TrinketData(Crit, 3184, 15, 60, null));
        map.put(89081, new TrinketData(Crit, 3595, 15, 60, null)); // Blossom of Pure Sno
        map.put(86885, new TrinketData(Spirit, 6121, 20, 115, 15)); // Spirits of the sun
        map.put(86774, new TrinketData(Spirit, 3184, 15, 60, null)); // Jade Courtesan Figurine

//        map.put(89080, new TrinketData(Spirit, 3595, 15, 60, null));
        map.put(89080, new TrinketData(Spirit, 0, 15, 60, null)); // nerfed so don't try out on balance
        map.put(81133, new TrinketData(Primary, 3386, 10, 115, 15)); // doesn't show proc nums
        map.put(86805, new TrinketData(Primary, 2866, 20, 55, 15)); // doesn't show proc nums

        // some crappy greens druid has
        map.put(84077, new TrinketData(Haste, 1851, 20, 115, 15)); // doesn't show proc nums
        map.put(83736, new TrinketData(Spirit, 1852, 20, 115, 15)); // doesn't show proc nums
        map.put(84071, new TrinketData(Haste, 1851, 20, 55, 15)); // doesn't show proc nums
        map.put(88585, new TrinketData(Primary, 0, 6, 55, 15)); // doesn't show proc nums
        map.put(83731, new TrinketData(Mastery, 1852, 20, 115, 15)); // dunno the details

        // future
        map.put(96543, new TrinketData(Crit, 3238, 20, 75, null)); // TODO do these cooldowns mean the same thing?
        map.put(94507, new TrinketData(Dodge, 16000/2, 20, 120, null));
        map.put(94508, new TrinketData(Primary, 8800, 15, 85, 15));

        map.put(103988, new TrinketData(Spirit, 8282, 15, 90, null));
        map.put(103986, new TrinketData(Mastery, 9945, 20, 115, 15));
        map.put(103987, new TrinketData(Crit, 9945, 20, 115, 15));

        // other ppl thunder
        map.put(96558, new TrinketData(Crit, 20000, 4, 37, null)); // actually 100% crit, not modelled
        map.put(94509, new TrinketData(Primary, 0, 1, 30, null)); // mana gain, not modelled
        map.put(96561, new TrinketData(Primary, 0, 1, 30, null)); // smart heal, not modelled
        map.put(96385, new TrinketData(Primary, 0, 1, 30, null)); // mana gain, not modelled
        map.put(96456, new TrinketData(Primary, 0, 1, 30, null)); // heal shield, not modelled
        map.put(96507, new TrinketData(Primary, 0, 1, 30, null)); // heal shield, not modelled
        map.put(94510, new TrinketData(Haste, 8800, 10, 55, 15));
        map.put(96455, new TrinketData(Primary, 8279, 10, 60*10/11, null));
        map.put(96516, new TrinketData(Primary, 8279, 10, 60*85/100, null));
        map.put(96413, new TrinketData(Primary, 1505*5, 10, 60*100/121, null));

        // other item levels
        // Gaze of the Twins
        map.put(96915, new TrinketData(Crit, 3424, 20, 75, null));
        map.put(96171, new TrinketData(Crit, 3034, 20, 75, null));
        map.put(94529, new TrinketData(Crit, 2868, 20, 75, null));
        map.put(95799, new TrinketData(Crit, 2381, 20, 75, null));
        // Delicate Vial of the Sanguinaire
        // "when you dodge" and it stacks
        map.put(96895, new TrinketData(Dodge, 10505, 20, 30, null));
        map.put(96523, new TrinketData(Dodge, 9935, 20, 30, null));
        map.put(96151, new TrinketData(Dodge, 9308, 20, 30, null));
        map.put(94518, new TrinketData(Dodge, 8800, 20, 30, null));
        map.put(95779, new TrinketData(Dodge, 7306, 20, 30, null));
//        Fortitude of the Zandalari
        // max hp can't model like others
        map.put(96793, new TrinketData(Primary, 0, 1, 30, null));
        map.put(96421, new TrinketData(Primary, 0, 1, 30, null));
        map.put(96049, new TrinketData(Primary, 0, 1, 30, null));
        map.put(94516, new TrinketData(Primary, 0, 1, 30, null));
        map.put(95677, new TrinketData(Primary, 0, 1, 30, null));
        // Ji-Kun's Rising Winds
        // self-heal can't model like others
        map.put(96843, new TrinketData(Primary, 0, 1, 30, null));
        map.put(96471, new TrinketData(Primary, 0, 1, 30, null));
        map.put(96099, new TrinketData(Primary, 0, 1, 30, null));
        map.put(94527, new TrinketData(Primary, 0, 1, 30, null));
        map.put(95727, new TrinketData(Primary, 0, 1, 30, null));
        // Resolve of Niuzao
        map.put(103990, new TrinketData(Dodge, 8282, 20, 120, null));
        map.put(103690, new TrinketData(Dodge, 5759, 20, 120, null));
        // Soul Barrier
        // absorb shield can't model like others
        map.put(96927, new TrinketData(Primary, 0, 1, 30, null));
        map.put(96555, new TrinketData(Primary, 0, 1, 30, null));
        map.put(96183, new TrinketData(Primary, 0, 1, 30, null));
        map.put(94528, new TrinketData(Primary, 0, 1, 30, null));
        map.put(95811, new TrinketData(Primary, 0, 1, 30, null));
        // Alacrity of Xuen
        map.put(103989, new TrinketData(Haste, 9945, 20, 115, 15));
        map.put(103689, new TrinketData(Haste, 6915, 20, 115, 15));
        // Fabled Feather of Ji-Kun
        map.put(96842, new TrinketData(Primary, 1592*5, 10, 49, null));
        map.put(96470, new TrinketData(Primary, 1505*5, 10, 49, null));
        map.put(96098, new TrinketData(Primary, 1410*5, 10, 49, null));
        map.put(94515, new TrinketData(Primary, 1333*5, 10, 49, null));
        map.put(95726, new TrinketData(Primary, 1107*5, 10, 49, null));
        // Primordius' Talisman of Rage
        // model doesn't do stacking
        map.put(96873, new TrinketData(Primary, 1836, 10, 17, null));
        map.put(96501, new TrinketData(Primary, 1736, 10, 17, null));
        map.put(96129, new TrinketData(Primary, 1627, 10, 17, null));
        map.put(94519, new TrinketData(Primary, 1538, 10, 17, null));
        map.put(95757, new TrinketData(Primary, 1277, 10, 17, null));
        // Spark of Zandalar
        map.put(96770, new TrinketData(Primary, 700, 10, 54, null));
        map.put(96398, new TrinketData(Primary, 700, 10, 54, null));
        map.put(96026, new TrinketData(Primary, 700, 10, 54, null));
        map.put(94526, new TrinketData(Primary, 700, 10, 54, null));
        map.put(95654, new TrinketData(Primary, 700, 10, 54, null));
        return map;
    }

    public static FullItemData updateTrinket(FullItemData item) {
        TrinketData trinketData = knownTrinkets.get(item.itemId());
        if (trinketData == null) {
            throw new IllegalArgumentException("unknown trinket " + item.toStringExtended());
        }

        StatBlock stats = calcStats(trinketData);
        return item.changeEnchant(stats, null, null);
    }

    private static StatBlock calcStats(TrinketData data) {
        int averageBonus;
        if (data.procChance == null)
            averageBonus = calcUsedTrinket(data.bonusSize, data.duration, data.cooldown);
        else
            averageBonus = calcProcTrinket(data.bonusSize, data.duration, data.cooldown, data.procChance);
        return StatBlock.of(data.bonus, averageBonus);
    }

    private static int calcUsedTrinket(int bonusSize, int duration, int cooldown) {
        return bonusSize * duration / cooldown;
    }

    private static int calcProcTrinket(int bonusSize, int duration, int cooldown, Integer procChance) {
        double swingTime = 2;
        double chance = procChance / 100.0, missChance = 1.0 - chance;

        double waitTime = averageWaitTime(swingTime, missChance, chance);
        double totalCooldown = cooldown + waitTime;

        return (int) Math.round(bonusSize * duration / totalCooldown);
    }

    private static double averageWaitTime(double swingTime, double missChance, double chance) {
        double waitTime = swingTime / 2;
        double cumulativeMiss = missChance;
        while (cumulativeMiss > 0.5) {
            waitTime += swingTime;
            cumulativeMiss *= chance;
        }
        return waitTime;
    }

    private record TrinketData(StatType bonus, int bonusSize, int duration, int cooldown, Integer procChance) {

    }
}
