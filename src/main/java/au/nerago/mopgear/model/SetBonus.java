package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.SlotEquip;

import java.util.*;

@SuppressWarnings("SpellCheckingInspection")
public class SetBonus {
    public static final int DEFAULT_BONUS = 1025;
    public static final int DENOMIATOR = 1000;

    private final List<SetInfo> activeSets = new ArrayList<>();

    // <<<<<<<<<<<<< PALADIN PROT TEIR 14 >>>>>>>>>>>>>>>>
    private static final Set<Integer> whiteTigerPlate = makeHashSet(new int[]{
            86661, 86659, 86663, 86662, 86660,
            85321, 85319, 85323, 85322, 85320,
            87111, 87113, 87109, 87110, 87112
    });
    public SetBonus activateWhiteTigerPlate() {
        activeSets.add(new SetInfo(whiteTigerPlate, DEFAULT_BONUS, DEFAULT_BONUS));
        return this;
    }

    // <<<<<<<<<<<<< PALADIN RET TEIR 14 >>>>>>>>>>>>>>>>
    private static final Set<Integer> whiteTigerBattlegear = makeHashSet(new int[]{
            86681, 86679, 86683, 86682, 86680,
            85341, 85339, 85343, 85342, 85340,
            87101, 87103, 87099, 87100, 87102
    });
    // templars verdict +15%, normally 21% of overall
    private static final int WHITE_TIGER_BATTLEGEAR_2 = 1032;
    // seal,judge +10%
    private static final int WHITE_TIGER_BATTLEGEAR_4 = 1024;
    public SetBonus activateWhiteTigerBattlegear() {
        activeSets.add(new SetInfo(whiteTigerBattlegear, WHITE_TIGER_BATTLEGEAR_2, WHITE_TIGER_BATTLEGEAR_4));
        return this;
    }
    public SetBonus activateWhiteTigerBattlegearOnly4pc() {
        activeSets.add(new SetInfo(whiteTigerBattlegear, DENOMIATOR, WHITE_TIGER_BATTLEGEAR_4));
        return this;
    }

    // <<<<<<<<<<<<< DRUID BOOM TEIR 14 >>>>>>>>>>>>>>>>
    private static final Set<Integer> regaliaEternalBlossom = makeHashSet(new int[]{
            86647, 86644, 86645, 86648, 86646,
            85307, 85304, 85305, 85308, 85306,
            86934, 86937, 86936, 86933, 86935
    });
    public SetBonus activateRegaliaEternalBlossom() {
        activeSets.add(new SetInfo(regaliaEternalBlossom, DEFAULT_BONUS, DEFAULT_BONUS));
        return this;
    }

    // <<<<<<<<<<<<< DRUID RESTO TEIR 14 >>>>>>>>>>>>>>>>
    private static final Set<Integer> vestmentsEternalBlossom = makeHashSet(new int[]{
            86697, 86694, 86695, 86698, 86696,
            85357, 85354, 85355, 85358, 85356,
            86929, 86932, 86931, 86928, 86930
    });
    public SetBonus activateVestmentsEternalBlossom() {
        activeSets.add(new SetInfo(vestmentsEternalBlossom, DEFAULT_BONUS, DEFAULT_BONUS));
        return this;
    }

    private static Set<Integer> makeHashSet(int[] array) {
        HashSet<Integer> set = new HashSet<>();
        for (int id : array)
            set.add(id);
        return set;
    }

    public long calc(EquipMap itemMap) {
        if (activeSets.isEmpty()) {
            return DENOMIATOR;
        }

        int activeSetCount = activeSets.size();
        int[] setCounts = new int[activeSetCount];
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData item = itemMap.get(slot);
            if (item != null) {
                int itemId = item.id;
                for (int i = 0; i < activeSetCount; ++i) {
                    if (activeSets.get(i).items.contains(itemId)) {
                        setCounts[i]++;
                    }
                }
            }
        }

        int result = DENOMIATOR;
        for (int i = 0; i < activeSetCount; ++i) {
            int numInSet = setCounts[i];
            SetInfo info = activeSets.get(i);
            if (numInSet >= 4) {
                result = result * info.bonus2 * info.bonus4 / DENOMIATOR / DENOMIATOR;
            } else if (numInSet >= 2) {
                result = result * info.bonus2 / DENOMIATOR;
            }
        }
        return result;
    }

    private record SetInfo(Set<Integer> items, int bonus2, int bonus4) {

    }
}
