package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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
        values[StatType.Primary.ordinal()] = primary;
        values[StatType.Stam.ordinal()] = stam;
        values[StatType.Mastery.ordinal()] = mastery;
        values[StatType.Crit.ordinal()] = crit;
        values[StatType.Hit.ordinal()] = hit;
        values[StatType.Haste.ordinal()] = haste;
        values[StatType.Expertise.ordinal()] = expertise;
        values[StatType.Dodge.ordinal()] = dodge;
        values[StatType.Parry.ordinal()] = parry;
        values[StatType.Spirit.ordinal()] = spirit;
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
        int primary = values[StatType.Primary.ordinal()];
        int stam = values[StatType.Stam.ordinal()];
        int mastery = values[StatType.Mastery.ordinal()];
        int crit = values[StatType.Crit.ordinal()];
        int hit = values[StatType.Hit.ordinal()];
        int haste = values[StatType.Haste.ordinal()];
        int expertise = values[StatType.Expertise.ordinal()];
        int dodge = values[StatType.Dodge.ordinal()];
        int parry = values[StatType.Parry.ordinal()];
        int spirit = values[StatType.Spirit.ordinal()];

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
        int count = 0;
        for (int i = 0; i < VALUES_SIZE; ++i) {
            if (values[i] != 0) {
                count++;
            }
        }
        return count == 1;
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
        return values[StatType.Primary.ordinal()];
    }

    public int stam() {
        return values[StatType.Stam.ordinal()];
    }

    public int mastery() {
        return values[StatType.Mastery.ordinal()];
    }

    public int crit() {
        return values[StatType.Crit.ordinal()];
    }

    public int hit() {
        return values[StatType.Hit.ordinal()];
    }

    public int haste() {
        return values[StatType.Haste.ordinal()];
    }

    public int expertise() {
        return values[StatType.Expertise.ordinal()];
    }

    public int dodge() {
        return values[StatType.Dodge.ordinal()];
    }

    public int parry() {
        return values[StatType.Parry.ordinal()];
    }

    public int spirit() {
        return values[StatType.Spirit.ordinal()];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

}
