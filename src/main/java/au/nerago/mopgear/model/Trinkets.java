package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.util.HashMap;
import java.util.Map;

import static au.nerago.mopgear.domain.StatType.*;

public class Trinkets {
    private static final Map<Integer, TrinketData> knownTrinkets = buildKnown();

    private static Map<Integer, TrinketData> buildKnown() {
        Map<Integer, TrinketData> map = new HashMap<>();
        map.put(86802, new TrinketData(Primary, 2866, 20, 55, 15)); // lei shen 476
        map.put(79329, new TrinketData(Dodge, 8871, 10, 60, null)); // relic of niuzao
        map.put(86131, new TrinketData(Dodge, 3236, 20, 55, 15)); // vial of dragon's 489
        map.put(86042, new TrinketData(Haste, 3595, 15, 60, null)); // jade charioteer 489
        map.put(81138, new TrinketData(Crit, 3386, 30, 115, 15)); // carbonic carbuncle

        map.put(83731, new TrinketData(Mastery, 1852, 20, 115, 15)); // dunno the details, druid green
        map.put(86792, new TrinketData(Primary, 2866, 20, 55, 15)); // light cosmos 476
        map.put(86907, new TrinketData(Haste, 6121, 20, 115, 15)); // essence terror
        map.put(86773, new TrinketData(Crit, 3184, 15, 60, null));
        map.put(89081, new TrinketData(Crit, 3595, 15, 60, null)); // Blossom of Pure Sno

//        map.put(89080, new TrinketData(Spirit, 3595, 15, 60, null));
        map.put(89080, new TrinketData(Spirit, 0, 15, 60, null)); // nerfed so don't try out on balance

        return map;
    }

    public static ItemData updateTrinket(ItemData item) {
        TrinketData trinketData = knownTrinkets.get(item.id);
        if (trinketData == null) {
            throw new IllegalArgumentException("unknown trinket " + item.toStringExtended());
        }

        StatBlock stats = calcStats(trinketData);
        return item.changeFixed(item.statFixed.plus(stats));
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
