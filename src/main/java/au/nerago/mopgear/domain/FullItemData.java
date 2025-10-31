package au.nerago.mopgear.domain;

import au.nerago.mopgear.model.IItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class FullItemData implements IItem {
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

    private FullItemData(@NotNull ItemShared shared, @NotNull ReforgeRecipe reforge,
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

    public static FullItemData buildFromWowSim(@NotNull ItemRef ref, @NotNull SlotItem slot, @NotNull String name,
                                               @NotNull StatBlock statBase,
                                               @NotNull PrimaryStatType primaryStatType, @NotNull ArmorType armorType,
                                               @NotNull SocketType[] socketSlots, StatBlock socketBonus) {
        ItemShared shared = ItemSharedCache.get(ref, slot, name, primaryStatType, armorType, socketSlots, socketBonus);
        return new FullItemData(shared, ReforgeRecipe.empty(), statBase, StatBlock.empty);
    }

    public static FullItemData buildFromWowHead(int id, @NotNull SlotItem slot, @NotNull String name,
                                                @NotNull StatBlock statBase,
                                                @NotNull PrimaryStatType primaryStatType, @NotNull ArmorType armorType,
                                                @NotNull SocketType[] socketSlots, StatBlock socketBonus, int itemLevel) {
        ItemRef ref = ItemRef.buildBasic(id, itemLevel);
        ItemShared shared = ItemSharedCache.get(ref, slot, name, primaryStatType, armorType, socketSlots, socketBonus);
        return new FullItemData(shared, ReforgeRecipe.empty(), statBase, StatBlock.empty);
    }

    public FullItemData changeForReforge(@NotNull StatBlock changedStats, @NotNull ReforgeRecipe recipe) {
        return new FullItemData(shared, recipe, changedStats, statEnchant);
    }

    public FullItemData changeStatsBase(@NotNull StatBlock changedStats) {
        return new FullItemData(shared, reforge, changedStats, statEnchant);
    }

    public FullItemData changeEnchant(@NotNull StatBlock changedEnchant) {
        return new FullItemData(shared, reforge, statBase, changedEnchant);
    }

    public FullItemData changeDuplicate(int dupNum) {
        ItemRef changeRef = ref().changeDuplicate(dupNum);
        ItemShared changeShared = ItemSharedCache.get(changeRef, shared);
        return new FullItemData(changeShared, reforge, statBase, statEnchant);
    }

    public FullItemData changeItemLevel(int itemLevel) {
        ItemRef changeRef = ref().changeItemLevel(itemLevel);
        ItemShared changeShared = ItemSharedCache.get(changeRef, shared);
        return new FullItemData(changeShared, reforge, statBase, statEnchant);
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

    public static boolean isSameEquippedItem(FullItemData a, FullItemData b) {
        return a.shared.equalsTyped(b.shared);
    }

    public static boolean isIdenticalItem(FullItemData a, FullItemData b) {
        return a.isIdenticalItem(b.shared.ref(), b.totalCap, b.totalRated);
    }

    public boolean isIdenticalItem(ItemRef otherRef, StatBlock otherTotalCap, StatBlock otherTotalRated) {
        return shared.ref().equalsTyped(otherRef) && totalCap.equalsStats(otherTotalCap) && totalRated.equalsStats(otherTotalRated);
    }

    @Override
    public boolean isSameItem(ItemRef ref) {
        return shared.ref().equalsTyped(ref);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullItemData itemData = (FullItemData) o;
        return equalsTyped(itemData);
    }

    public boolean equalsTyped(FullItemData other) {
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
