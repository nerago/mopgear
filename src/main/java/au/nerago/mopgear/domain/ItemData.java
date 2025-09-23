package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ItemData {
    @NotNull
    public final ItemRef ref;
    @NotNull
    public final SlotItem slot;
    @NotNull
    public final String name;
    @NotNull
    public final ReforgeRecipe reforge;
    @NotNull
    public final StatBlock stat;
    @NotNull
    public final StatBlock statFixed;
    @NotNull
    public final SocketType[] socketSlots;
    public final int socketBonus;

    private ItemData(@NotNull ItemRef ref, @NotNull SlotItem slot, @NotNull String name, @NotNull ReforgeRecipe reforge,
                     @NotNull StatBlock stat, @NotNull StatBlock statFixed, @NotNull SocketType[] socketSlots, int socketBonus) {
        this.ref = ref;
        this.slot = slot;
        this.name = name;
        this.reforge = reforge;
        this.stat = stat;
        this.statFixed = statFixed;
        this.socketSlots = socketSlots;
        this.socketBonus = socketBonus;
    }

    public static ItemData build(int id, @NotNull SlotItem slot, @NotNull String name, @NotNull StatBlock stat, @NotNull SocketType[] socketSlots, int socketBonus, int itemLevel) {
        return new ItemData(ItemRef.build(id, itemLevel), slot, name, ReforgeRecipe.empty(), stat, StatBlock.empty, socketSlots, socketBonus);
    }

    public ItemData changeNameAndStats(@NotNull String changedName, @NotNull StatBlock changedStats, @NotNull ReforgeRecipe recipe) {
        return new ItemData(ref, slot, changedName, recipe, changedStats, statFixed, socketSlots, socketBonus);
    }

    public ItemData changeStats(@NotNull StatBlock changedStats) {
        return new ItemData(ref, slot, name, reforge, changedStats, statFixed, socketSlots, socketBonus);
    }

    public ItemData changeFixed(@NotNull StatBlock changedFixed) {
        return new ItemData(ref, slot, name, reforge, stat, changedFixed, socketSlots, socketBonus);
    }

    public ItemData changeDuplicate(int dupNum) {
        return new ItemData(ref.changeDuplicate(dupNum), slot, name, reforge, stat, statFixed, socketSlots, socketBonus);
    }

    public ItemData changeItemLevel(int itemLevel) {
        return new ItemData(ref.changeItemLevel(itemLevel), slot, name, reforge, stat, statFixed, socketSlots, socketBonus);
    }

    public StatBlock totalStatCopy() {
        if (statFixed.isEmpty())
            return stat;
        else
            return stat.plus(statFixed);
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
        sb.append("REF ");
        sb.append("ilevel=").append(ref.itemLevel()).append(' ');
        sb.append("itemId=").append(ref.itemId()).append(' ');
        sb.append('}');
        return sb.toString();
    }

    private void append(StringBuilder sb) {
        sb.append(slot).append(' ');
        sb.append('"').append(name).append("\" ");
        stat.append(sb, false);
        if (!statFixed.isEmpty()) {
            sb.append("GEMS ");
            statFixed.append(sb, false);
        }
    }

    public static boolean isSameEquippedItem(ItemData a, ItemData b) {
        return a.ref.equalsTyped(b.ref);
    }

    public static boolean isIdenticalItem(ItemData a, ItemData b) {
        return a.ref.equalsTyped(b.ref) && a.stat.equalsStats(b.stat) && a.statFixed.equalsStats(b.statFixed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemData itemData = (ItemData) o;
        return ref.equalsTyped(itemData.ref) && slot == itemData.slot && Objects.equals(reforge, itemData.reforge) && Objects.equals(stat, itemData.stat) && Objects.equals(statFixed, itemData.statFixed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, slot, reforge, stat, statFixed);
    }
}
