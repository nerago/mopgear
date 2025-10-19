package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;

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
    public final StatBlock statBase;
    @NotNull
    public final StatBlock statEnchant;
    @NotNull
    public final StatBlock totalCap;
    @NotNull
    public final StatBlock totalRated;
    @NotNull
    public final SocketType[] socketSlots;
    public final StatBlock socketBonus;

    private ItemData(@NotNull ItemRef ref, @NotNull SlotItem slot, @NotNull String name, @NotNull ReforgeRecipe reforge,
                     @NotNull StatBlock statBase, @NotNull StatBlock statEnchant, @NotNull SocketType[] socketSlots, StatBlock socketBonus) {
        this.ref = ref;
        this.slot = slot;
        this.name = name;
        this.reforge = reforge;
        this.statBase = statBase;
        this.statEnchant = statEnchant;
        this.socketSlots = socketSlots;
        this.socketBonus = socketBonus;

        if (statEnchant.isEmpty()) {
            totalCap = totalRated = statBase;
        } else if (slot.addEnchantToCap) {
            totalCap = totalRated = statBase.plus(statEnchant);
        } else {
            totalCap = statBase;
            totalRated = statBase.plus(statEnchant);
        }
    }

    public static ItemData buildFromWowSim(ItemRef ref, @NotNull SlotItem slot, @NotNull String name, @NotNull StatBlock statBase, @NotNull SocketType[] socketSlots, StatBlock socketBonus) {
        return new ItemData(ref, slot, name, ReforgeRecipe.empty(), statBase, StatBlock.empty, socketSlots, socketBonus);
    }

    public static ItemData buildFromWowHead(int id, @NotNull SlotItem slot, @NotNull String name, @NotNull StatBlock statBase, @NotNull SocketType[] socketSlots, StatBlock socketBonus, int itemLevel) {
        return new ItemData(ItemRef.buildBasic(id, itemLevel), slot, name, ReforgeRecipe.empty(), statBase, StatBlock.empty, socketSlots, socketBonus);
    }

    public ItemData changeNameAndStats(@NotNull String changedName, @NotNull StatBlock changedStats, @NotNull ReforgeRecipe recipe) {
        return new ItemData(ref, slot, changedName, recipe, changedStats, statEnchant, socketSlots, socketBonus);
    }

    public ItemData changeStatsBase(@NotNull StatBlock changedStats) {
        return new ItemData(ref, slot, name, reforge, changedStats, statEnchant, socketSlots, socketBonus);
    }

    public ItemData changeEnchant(@NotNull StatBlock changedEnchant) {
        return new ItemData(ref, slot, name, reforge, statBase, changedEnchant, socketSlots, socketBonus);
    }

    public ItemData changeDuplicate(int dupNum) {
        return new ItemData(ref.changeDuplicate(dupNum), slot, name, reforge, statBase, statEnchant, socketSlots, socketBonus);
    }

    public ItemData changeItemLevel(int itemLevel) {
        return new ItemData(ref.changeItemLevel(itemLevel), slot, name, reforge, statBase, statEnchant, socketSlots, socketBonus);
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

    public static String toStringExtended(ItemData item) {
        return item != null ? item.toStringExtended() : "null";
    }

    private void append(StringBuilder sb) {
        sb.append(slot).append(' ');
        sb.append('"').append(name).append("\" ");
        statBase.append(sb, false);
        if (!statEnchant.isEmpty()) {
            sb.append("ENCHANT ");
            statEnchant.append(sb, false);
        }
    }

    public static boolean isSameEquippedItem(ItemData a, ItemData b) {
        return a.ref.equalsTyped(b.ref);
    }

    public static boolean isIdenticalItem(ItemData a, ItemData b) {
        return a.ref.equalsTyped(b.ref) && a.statBase.equalsStats(b.statBase) && a.statEnchant.equalsStats(b.statEnchant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemData itemData = (ItemData) o;
        return equalsTyped(itemData);
    }

    public boolean equalsTyped(ItemData other) {
        return ref.equalsTyped(other.ref) && slot == other.slot && Objects.equals(reforge, other.reforge) &&
                statBase.equalsStats(other.statBase) && statEnchant.equalsStats(other.statEnchant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, slot, reforge, statBase, statEnchant);
    }

    public boolean isUpgradable() {
        return !name.contains("Gladiator");
    }
}
