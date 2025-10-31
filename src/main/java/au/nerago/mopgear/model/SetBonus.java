package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("SpellCheckingInspection")
public class SetBonus {
    public static final int DEFAULT_BONUS = 1025;
    public static final int DENOMIATOR = 1000;

    public static SetBonus forSpec(SpecType spec) {
        SetBonus setBonus = new SetBonus();
        SetInfo specified = allSets.stream().filter(s -> s.spec == spec).findAny().orElseThrow();
        setBonus.activeSets.add(specified);
        return setBonus;
    }

    private final List<SetInfo> activeSets = new ArrayList<>();
    private static final List<SetInfo> allSets = buildSets();

    private static List<SetInfo> buildSets() {
        List<SetInfo> sets = new ArrayList<>();
        sets.add(new SetInfo(whiteTigerPlate, SpecType.PaladinProtMitigation, DEFAULT_BONUS, DEFAULT_BONUS));
        sets.add(new SetInfo(whiteTigerBattlegear, SpecType.PaladinRet, WHITE_TIGER_BATTLEGEAR_2, WHITE_TIGER_BATTLEGEAR_4));
        sets.add(new SetInfo(regaliaEternalBlossom, SpecType.DruidBoom, DEFAULT_BONUS, DEFAULT_BONUS));
        sets.add(new SetInfo(vestmentsEternalBlossom, SpecType.DruidTree, DEFAULT_BONUS, DEFAULT_BONUS));
        return sets;
    }


    // <<<<<<<<<<<<< PALADIN PROT TEIR 14 >>>>>>>>>>>>>>>>
    private static final Set<Integer> whiteTigerPlate = makeHashSet(new int[]{
            86661, 86659, 86663, 86662, 86660,
            85321, 85319, 85323, 85322, 85320,
            87111, 87113, 87109, 87110, 87112
    });

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
        activeSets.addAll(allSets.stream().filter(s -> s.spec == SpecType.PaladinRet).toList());
        return this;
    }
    public SetBonus activateWhiteTigerBattlegearOnly4pc() {
        activeSets.add(new SetInfo(whiteTigerBattlegear, SpecType.PaladinRet, DENOMIATOR, 1050));
        return this;
    }

    // <<<<<<<<<<<<< DRUID BOOM TEIR 14 >>>>>>>>>>>>>>>>
    private static final Set<Integer> regaliaEternalBlossom = makeHashSet(new int[]{
            86647, 86644, 86645, 86648, 86646,
            85307, 85304, 85305, 85308, 85306,
            86934, 86937, 86936, 86933, 86935
    });
    public SetBonus activateRegaliaEternalBlossom() {
        activeSets.addAll(allSets.stream().filter(s -> s.spec == SpecType.DruidBoom).toList());
        return this;
    }

    // <<<<<<<<<<<<< DRUID RESTO TEIR 14 >>>>>>>>>>>>>>>>
    private static final Set<Integer> vestmentsEternalBlossom = makeHashSet(new int[]{
            86697, 86694, 86695, 86698, 86696,
            85357, 85354, 85355, 85358, 85356,
            86929, 86932, 86931, 86928, 86930
    });
    public SetBonus activateVestmentsEternalBlossom() {
        activeSets.addAll(allSets.stream().filter(s -> s.spec == SpecType.DruidTree).toList());
        return this;
    }

    private static Set<Integer> makeHashSet(int[] array) {
        HashSet<Integer> set = new HashSet<>();
        for (int id : array)
            set.add(id);
        return set;
    }

    public long calc(FullItemSet set) {
        if (activeSets.isEmpty()) {
            return DENOMIATOR;
        }
        int[] setCounts = countBySet(set.items());
        return calcMuliplier(setCounts);
    }

    public long calc(SolvableItemSet set) {
        if (activeSets.isEmpty()) {
            return DENOMIATOR;
        }
        int[] setCounts = countBySet(set.items());
        return calcMuliplier(setCounts);
    }

    private long calcMuliplier(int[] setCounts) {
        int result = DENOMIATOR;
        for (int i = 0; i < activeSets.size(); ++i) {
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

    private int[] countBySet(IEquipMap itemMap) {
        // TODO consider more optimising
        int activeSetCount = activeSets.size();
        int[] setCounts = new int[activeSetCount];
        for (SlotEquip slot : SlotEquip.values()) {
            IItem item = itemMap.get(slot);
            if (item != null) {
                int itemId = item.itemId();
                for (int i = 0; i < activeSetCount; ++i) {
                    if (activeSets.get(i).items.contains(itemId)) {
                        setCounts[i]++;
                    }
                }
            }
        }
        return setCounts;
    }

    public int countInSet(SolvableEquipMap itemMap) {
        int[] counts = countBySet(itemMap);
        int total = 0;
        for (int val : counts) {
            total += val;
        }
        return total;
    }

    public static SpecType forGear(List<LogItemInfo> itemInfoList) {
        for (SetInfo set : allSets) {
            for (LogItemInfo item : itemInfoList) {
                if (set.items.contains(item.itemId())) {
                    return set.spec;
                }
            }
        }
        return null;
    }

    private record SetInfo(Set<Integer> items, SpecType spec, int bonus2, int bonus4) {

    }
}
