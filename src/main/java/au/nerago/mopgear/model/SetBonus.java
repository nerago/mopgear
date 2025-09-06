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

    // templars verdict +15%, normally 21% of overall
    private static final int WHITE_TIGER_BATTLEGEAR_2 = 1032;
    // seal,judge +10%
    private static final int WHITE_TIGER_BATTLEGEAR_4 = 1024;

    public static final int DENOMIATOR = 1000;

    private static Set<Integer> makeHashSet(int[] array) {
        HashSet<Integer> set = new HashSet<>();
        for (int id : array)
            set.add(id);
        return set;
    }

    public static long calc(EquipMap itemMap) {
        int setItems = 0;
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData item = itemMap.get(slot);
            if (item != null && whiteTigerBattlegear.contains(item.id)) {
                setItems++;
            }
        }

        if (setItems >= 4) {
            return WHITE_TIGER_BATTLEGEAR_2 * WHITE_TIGER_BATTLEGEAR_4 / DENOMIATOR;
        } else if (setItems >= 2) {
            return WHITE_TIGER_BATTLEGEAR_2;
        } else {
            return DENOMIATOR;
        }
    }
}
