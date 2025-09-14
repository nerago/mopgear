package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.SlotEquip;

import java.util.*;

@SuppressWarnings("SpellCheckingInspection")
public class SetBonus {
    private static final Set<Integer> whiteTigerBattlegear = makeHashSet(new int[] {
            86681, 86679, 86683, 86682, 86680,
            85341, 85339, 85343, 85342, 85340,
            87101, 87103, 87099, 87100, 87102
    });

    private static final Set<Integer> regaliaEternalBlossom = makeHashSet(new int[] {
            86647,86644,86645,86648,86646,
            85307,85304,85305,85308,85306,
            86934,86937,86936,86933,86935
    });

    private static final Set<Integer> vestmentsEternalBlossom = makeHashSet(new int[] {
            86697,86694,86695,86698,86696,
            85357,85354,85355,85358,85356,
            86929,86932,86931,86928,86930
    });

    // templars verdict +15%, normally 21% of overall
    private static final int WHITE_TIGER_BATTLEGEAR_2 = 1032;
    // seal,judge +10%
    private static final int WHITE_TIGER_BATTLEGEAR_4 = 1024;

    public static final int DEFAULT_BONUS = 1025;
    public static final int DENOMIATOR = 1000;

    private static Set<Integer> makeHashSet(int[] array) {
        HashSet<Integer> set = new HashSet<>();
        for (int id : array)
            set.add(id);
        return set;
    }

    public static long calc(EquipMap itemMap) {
        int countWhiteTigerBattlegear = 0, countRegaliaEternalBlossom = 0, countVestmentsEternalBlossom = 0;
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData item = itemMap.get(slot);
            if (item != null) {
                if (whiteTigerBattlegear.contains(item.id)) {
                    countWhiteTigerBattlegear++;
                }
                if (regaliaEternalBlossom.contains(item.id)) {
                    countRegaliaEternalBlossom++;
                }
                if (vestmentsEternalBlossom.contains(item.id)) {
                    countVestmentsEternalBlossom++;
                }
            }
        }

        int result = DENOMIATOR;
        result = result * calcSet(countWhiteTigerBattlegear, WHITE_TIGER_BATTLEGEAR_2, WHITE_TIGER_BATTLEGEAR_4) / DENOMIATOR;
        result = result * calcSet(countRegaliaEternalBlossom, DEFAULT_BONUS, DEFAULT_BONUS) / DENOMIATOR;
        result = result * calcSet(countVestmentsEternalBlossom, DEFAULT_BONUS, DEFAULT_BONUS) / DENOMIATOR;
        return result;
    }

    private static int calcSet(int setCount, int bonus2, int bonus4) {
        if (setCount >= 4) {
            return bonus2 * bonus4 / DENOMIATOR;
        } else if (setCount >= 2) {
            return bonus2;
        } else {
            return DENOMIATOR;
        }
    }
}
