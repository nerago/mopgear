package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ItemData implements SolvableItem {
    @NotNull
    public final ItemShared shared;
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

    private ItemData(@NotNull ItemShared shared, @NotNull ReforgeRecipe reforge,
                     @NotNull StatBlock statBase, @NotNull StatBlock statEnchant) {
        this.shared = shared;
        this.reforge = reforge;
        this.statBase = statBase;
        this.statEnchant = statEnchant;

        if (statEnchant.isEmpty()) {
            totalCap = totalRated = statBase;
        } else if (slot().addEnchantToCap) {
            totalCap = totalRated = statBase.plus(statEnchant);
        } else {
            totalCap = statBase;
            totalRated = statBase.plus(statEnchant);
        }
    }

    public static ItemData buildFromWowSim(@NotNull ItemRef ref, @NotNull SlotItem slot, @NotNull String name, @NotNull StatBlock statBase, @NotNull SocketType[] socketSlots, StatBlock socketBonus) {
        ItemShared shared = ItemSharedCache.get(ref, slot, name, socketSlots, socketBonus);
        return new ItemData(shared, ReforgeRecipe.empty(), statBase, StatBlock.empty);
    }

    public static ItemData buildFromWowHead(int id, @NotNull SlotItem slot, @NotNull String name, @NotNull StatBlock statBase, @NotNull SocketType[] socketSlots, StatBlock socketBonus, int itemLevel) {
        ItemRef ref = ItemRef.buildBasic(id, itemLevel);
        ItemShared shared = ItemSharedCache.get(ref, slot, name, socketSlots, socketBonus);
        return new ItemData(shared, ReforgeRecipe.empty(), statBase, StatBlock.empty);
    }

    public ItemData changeForReforge(@NotNull StatBlock changedStats, @NotNull ReforgeRecipe recipe) {
        return new ItemData(shared, recipe, changedStats, statEnchant);
    }

    public ItemData changeStatsBase(@NotNull StatBlock changedStats) {
        return new ItemData(shared, reforge, changedStats, statEnchant);
    }

    public ItemData changeEnchant(@NotNull StatBlock changedEnchant) {
        return new ItemData(shared, reforge, statBase, changedEnchant);
    }

    public ItemData changeDuplicate(int dupNum) {
        ItemRef changeRef = ref().changeDuplicate(dupNum);
        ItemShared changeShared = ItemSharedCache.get(changeRef, shared);
        return new ItemData(changeShared, reforge, statBase, statEnchant);
    }

    public ItemData changeItemLevel(int itemLevel) {
        ItemRef changeRef = ref().changeItemLevel(itemLevel);
        ItemShared changeShared = ItemSharedCache.get(changeRef, shared);
        return new ItemData(changeShared, reforge, statBase, statEnchant);
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
        sb.append("ilevel=").append(ref().itemLevel()).append(' ');
        sb.append("itemId=").append(itemId()).append(' ');
        sb.append('}');
        return sb.toString();
    }

    public static String toStringExtended(ItemData item) {
        return item != null ? item.toStringExtended() : "null";
    }

    public String fullName() {
        if (reforge.isEmpty())
            return shared.name();
        else
            return shared.name() + " (" + reforge.source() + "->" + reforge.dest() + ")";
    }

    private void append(StringBuilder sb) {
        sb.append(slot()).append(' ');
        sb.append('"').append(fullName()).append("\" ");
        statBase.append(sb, false);
        if (!statEnchant.isEmpty()) {
            sb.append("ENCHANT ");
            statEnchant.append(sb, false);
        }
    }

    public static boolean isSameEquippedItem(ItemData a, ItemData b) {
        return a.shared.equalsTyped(b.shared);
    }

    public static boolean isIdenticalItem(ItemData a, ItemData b) {
        return a.shared.equalsTyped(b.shared) && a.statBase.equalsStats(b.statBase) && a.statEnchant.equalsStats(b.statEnchant);
    }

    @Override
    public boolean isIdenticalItem(SolvableItem other) {
        return isIdenticalItem(this, (ItemData) other);
    }

    @Override
    public boolean isSameItem(ItemRef ref) {
        return shared.ref().equalsTyped(ref);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemData itemData = (ItemData) o;
        return equalsTyped(itemData);
    }

    public boolean equalsTyped(ItemData other) {
        return shared.equalsTyped(other.shared) && Objects.equals(reforge, other.reforge) &&
                statBase.equalsStats(other.statBase) && statEnchant.equalsStats(other.statEnchant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shared, reforge, statBase, statEnchant);
    }

    public boolean isUpgradable() {
        return !shared.name().contains("Gladiator");
    }

    public ItemRef ref() {
        return shared.ref();
    }

    @Override
    public @NotNull SlotItem slot() {
        return shared.slot();
    }

    @Override
    public int itemId() {
        return shared.ref().itemId();
    }

    public int itemLevel() {
        return shared.ref().itemLevel();
    }

    @Override
    public @NotNull ReforgeRecipe reforge() {
        return reforge;
    }

    @Override
    public @NotNull StatBlock totalRated() {
        return totalRated;
    }

    @Override
    public @NotNull StatBlock totalCap() {
        return totalCap;
    }
}
