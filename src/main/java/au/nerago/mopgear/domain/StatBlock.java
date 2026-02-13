package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class StatBlock {
    public final static StatBlock empty = new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final int VALUES_SIZE = 10;
    private final int @NotNull [] values;

    private StatBlock(int @NotNull [] values) {
        this.values = values;
    }

    public StatBlock(int primary, int stam, int mastery, int crit, int hit, int haste,
                     int expertise, int dodge, int parry, int spirit) {
        values = new int[VALUES_SIZE];
        values[StatType.Primary_Ordinal] = primary;
        values[StatType.Stam_Ordinal] = stam;
        values[StatType.Mastery_Ordinal] = mastery;
        values[StatType.Crit_Ordinal] = crit;
        values[StatType.Hit_Ordinal] = hit;
        values[StatType.Haste_Ordinal] = haste;
        values[StatType.Expertise_Ordinal] = expertise;
        values[StatType.Dodge_Ordinal] = dodge;
        values[StatType.Parry_Ordinal] = parry;
        values[StatType.Spirit_Ordinal] = spirit;
    }

    public static StatBlock add(@Nullable StatBlock a, @Nullable StatBlock b) {
        if (a != null && b != null)
            return a.plus(b);
        else if (a != null)
            return a;
        else
            return b;
    }

    private static int[] addValues(int[] a, int[] b) {
        int[] result = new int[VALUES_SIZE];
        for (int i = 0; i < VALUES_SIZE; ++i)
            result[i] = a[i] + b[i];
        return result;
    }

    private static int[] addValues(int[] a, int[] b, int[] c) {
        int[] result = new int[VALUES_SIZE];
        for (int i = 0; i < VALUES_SIZE; ++i)
            result[i] = a[i] + b[i] + c[i];
        return result;
    }

    public StatBlock plus(@NotNull StatBlock other) {
        return new StatBlock(addValues(this.values, other.values));
    }

    public StatBlock plus(@NotNull StatBlock first, @NotNull StatBlock second) {
        return new StatBlock(addValues(this.values, first.values, second.values));
    }

    public StatBlock multiply(int multiply) {
        int[] result = new int[VALUES_SIZE];
        for (int i = 0; i < VALUES_SIZE; ++i)
            result[i] = this.values[i] * multiply;
        return new StatBlock(result);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static long multiplyForTotalSum(StatBlock a, StatBlock b) {
        int[] v = a.values, w = b.values;
        long total = 0;
        for (int i = 0; i < VALUES_SIZE; ++i)
            total += Math.multiplyFull(v[i], w[i]);
        return total;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static StatBlock sumForRating(@NotNull EquipMap items) {
        int[] result = new int[VALUES_SIZE];
        for (SlotEquip slot : SlotEquip.values()) {
            FullItemData item = items.get(slot);
            if (item != null) {
                int[] base = item.statBase.values;
                for (int i = 0; i < VALUES_SIZE; ++i)
                    result[i] += base[i];

                int[] enchant = item.statEnchant.values;
                for (int i = 0; i < VALUES_SIZE; ++i)
                    result[i] += enchant[i];
            }
        }
        return new StatBlock(result);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static StatBlock sumForCaps(@NotNull EquipMap items) {
        int[] result = new int[VALUES_SIZE];
        for (SlotEquip slot : SlotEquip.values()) {
            FullItemData item = items.get(slot);
            if (item != null) {
                int[] base = item.statBase.values;
                for (int i = 0; i < VALUES_SIZE; ++i)
                    result[i] += base[i];

                if (item.slot().addEnchantToCap) {
                    int[] enchant = item.statEnchant.values;
                    for (int i = 0; i < VALUES_SIZE; ++i)
                        result[i] += enchant[i];
                }
            }
        }
        return new StatBlock(result);
    }

    public static StatBlock sumForRating(@NotNull SolvableEquipMap items) {
        int[] result = new int[VALUES_SIZE];
        for (SlotEquip slot : SlotEquip.values()) {
            SolvableItem item = items.get(slot);
            if (item != null) {
                int[] base = item.totalRated().values;
                for (int i = 0; i < VALUES_SIZE; ++i)
                    result[i] += base[i];
            }
        }
        return new StatBlock(result);
    }

    public static StatBlock sumForCaps(@NotNull SolvableEquipMap items) {
        int[] result = new int[VALUES_SIZE];
        for (SlotEquip slot : SlotEquip.values()) {
            SolvableItem item = items.get(slot);
            if (item != null) {
                int[] base = item.totalCap().values;
                for (int i = 0; i < VALUES_SIZE; ++i)
                    result[i] += base[i];
            }
        }
        return new StatBlock(result);
    }

    public int get(@NotNull StatType stat) {
        return values[stat.ordinal()];
    }

    public static StatBlock of(StatType stat, int value) {
        int[] result = new int[VALUES_SIZE];
        result[stat.ordinal()] = value;
        return new StatBlock(result);
    }

    public static StatBlock of(StatType a_stat, int a_value, StatType b_stat, int b_value) {
        int[] result = new int[VALUES_SIZE];
        if (a_stat == b_stat)
            throw new IllegalArgumentException();
        result[a_stat.ordinal()] = a_value;
        result[b_stat.ordinal()] = b_value;
        return new StatBlock(result);
    }

    public StatBlock withChange(StatType stat, int value) {
        int[] result = Arrays.copyOf(values, VALUES_SIZE);
        result[stat.ordinal()] = value;
        return new StatBlock(result);
    }

    public StatBlock withChange(StatType a_stat, int a_value, StatType b_stat, int b_value) {
        int[] result = Arrays.copyOf(values, VALUES_SIZE);
        if (a_stat == b_stat)
            throw new IllegalArgumentException();
        result[a_stat.ordinal()] = a_value;
        result[b_stat.ordinal()] = b_value;
        return new StatBlock(result);
    }

    public void append(StringBuilder sb, boolean extended) {
        int primary = values[StatType.Primary_Ordinal];
        int stam = values[StatType.Stam_Ordinal];
        int mastery = values[StatType.Mastery_Ordinal];
        int crit = values[StatType.Crit_Ordinal];
        int hit = values[StatType.Hit_Ordinal];
        int haste = values[StatType.Haste_Ordinal];
        int expertise = values[StatType.Expertise_Ordinal];
        int dodge = values[StatType.Dodge_Ordinal];
        int parry = values[StatType.Parry_Ordinal];
        int spirit = values[StatType.Spirit_Ordinal];

        if (primary != 0)
            sb.append("primary=").append(primary).append(' ');
        if (stam != 0)
            sb.append("stam=").append(stam).append(' ');
        if (mastery != 0)
            sb.append("mastery=").append(mastery).append(' ');
        if (crit != 0)
            sb.append("crit=").append(crit).append(' ');
        if (hit != 0)
            sb.append("hit=").append(hit).append(' ');
        if (haste != 0)
            sb.append("haste=").append(haste).append(' ');
        if (expertise != 0)
            sb.append("expertise=").append(expertise).append(' ');
        if (dodge != 0)
            sb.append("dodge=").append(dodge).append(' ');
        if (parry != 0)
            sb.append("parry=").append(parry).append(' ');
        if (spirit != 0)
            sb.append("spirit=").append(spirit).append(' ');
        if (extended && (hit != 0 || expertise != 0 || spirit != 0))
            sb.append("combohit=").append(hit + expertise + spirit).append(' ');
    }

    public boolean isEmpty() {
        for (int i = 0; i < VALUES_SIZE; ++i) {
            if (values[i] != 0)
                return false;
        }
        return true;
    }

    public boolean hasSingleStat() {
        return nonZeroCount() == 1;
    }

    public int nonZeroCount() {
        int count = 0;
        for (int i = 0; i < VALUES_SIZE; ++i) {
            if (values[i] != 0) {
                count++;
            }
        }
        return count;
    }

    public List<StatType> nonZeroStats() {
        List<StatType> stat = new ArrayList<>();
        for (int i = 0; i < VALUES_SIZE; ++i) {
            if (values[i] != 0) {
                stat.add(StatType.values()[i]);
            }
        }
        return stat;
    }

    public boolean equalsStats(StatBlock stats) {
        for (int i = 0; i < VALUES_SIZE; ++i) {
            if (values[i] != stats.values[i])
                return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equalsStats((StatBlock) o);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        append(builder, false);
        builder.append('}');
        return builder.toString();
    }

    public @NotNull String toStringExtended() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        append(builder, true);
        builder.append('}');
        return builder.toString();
    }

    public int primary() {
        return values[StatType.Primary_Ordinal];
    }

    public int stam() {
        return values[StatType.Stam_Ordinal];
    }

    public int hit() {
        return values[StatType.Hit_Ordinal];
    }

    public int expertise() {
        return values[StatType.Expertise_Ordinal];
    }

    public int spirit() {
        return values[StatType.Spirit_Ordinal];
    }

    public int mastery() {
        return values[StatType.Mastery_Ordinal];
    }

    public int crit() {
        return values[StatType.Crit_Ordinal];
    }

    public int haste() {
        return values[StatType.Haste_Ordinal];
    }

    public int dodge() {
        return values[StatType.Dodge_Ordinal];
    }

    public int parry() {
        return values[StatType.Parry_Ordinal];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}
