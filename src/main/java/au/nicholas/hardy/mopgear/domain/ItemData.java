package au.nicholas.hardy.mopgear.domain;

import java.util.Objects;

public final class ItemData {
    public final int id;
    public final SlotItem slot;
    public final String name;
    public final ReforgeRecipe reforge;
    public final StatBlock stat;
    public final StatBlock statFixed;
    public final SocketType[] socketSlots;
    public final int socketBonus;
    public final int itemLevel;

    private ItemData(int id, SlotItem slot, String name, ReforgeRecipe reforge, StatBlock stat, StatBlock statFixed, SocketType[] socketSlots, int socketBonus, int itemLevel) {
        this.id = id;
        this.slot = slot;
        this.name = name;
        this.reforge = reforge;
        this.stat = stat;
        this.statFixed = statFixed;
        this.socketSlots = socketSlots;
        this.socketBonus = socketBonus;
        this.itemLevel = itemLevel;
    }

    public static ItemData build(int id, SlotItem slot, String name, StatBlock stat, SocketType[] socketSlots, int socketBonus, int itemLevel) {
        return new ItemData(id, slot, name, null, stat, StatBlock.empty, socketSlots, socketBonus, itemLevel);
    }

    public ItemData changeNameAndStats(String changedName, StatBlock changedStats, ReforgeRecipe recipe) {
        return new ItemData(id, slot, changedName, recipe, changedStats, statFixed, socketSlots, socketBonus, itemLevel);
    }

    public ItemData changeStats(StatBlock changedStats) {
        return new ItemData(id, slot, name, reforge, changedStats, statFixed, socketSlots, socketBonus, itemLevel);
    }

    public ItemData changeFixed(StatBlock changedFixed) {
        return new ItemData(id, slot, name, reforge, stat, changedFixed, socketSlots, socketBonus, itemLevel);
    }

    public ItemData withoutFixed() {
        return new ItemData(id, slot, name, reforge, stat, StatBlock.empty, socketSlots, socketBonus, itemLevel);
    }

    public StatBlock totalStatCopy() {
        if (statFixed.isEmpty())
            return stat;
        else
            return stat.plus(statFixed);
    }

    public int totalStat(StatType type) {
        return stat.get(type) + statFixed.get(type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{ ");
        append(sb);
        sb.append('}');
        return sb.toString();
    }

    public String toStringExtended() {
        final StringBuilder sb = new StringBuilder("{ ");
        append(sb);
        sb.append("ilevel=").append(itemLevel).append(' ');
        sb.append("itemId=").append(id).append(' ');
        sb.append('}');
        return sb.toString();
    }

    private void append(StringBuilder sb) {
        if (slot != null)
            sb.append(slot).append(' ');
        if (name != null)
            sb.append('"').append(name).append("\" ");
        else
            sb.append("TOTAL ");
        stat.append(sb, false);
        if (!statFixed.isEmpty()) {
            sb.append("GEMS ");
            statFixed.append(sb, false);
        }
    }

    public static boolean isSameEquippedItem(ItemData a, ItemData b) {
        return a.id == b.id && a.statFixed.equalsStats(b.statFixed);
    }

    public static boolean isIdenticalItem(ItemData a, ItemData b) {
        return a.id == b.id && a.stat.equalsStats(b.stat) && a.statFixed.equalsStats(b.statFixed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemData itemData = (ItemData) o;
        return id == itemData.id && slot == itemData.slot && Objects.equals(reforge, itemData.reforge) && Objects.equals(stat, itemData.stat) && Objects.equals(statFixed, itemData.statFixed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slot, reforge, stat, statFixed);
    }
}
