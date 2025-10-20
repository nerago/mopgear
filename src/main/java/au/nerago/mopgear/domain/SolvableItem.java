package au.nerago.mopgear.domain;

import au.nerago.mopgear.model.IItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SolvableItem(int itemId, int itemLevel, int itemLevelBase, int duplicateNum, @NotNull SlotItem slot,
                           @NotNull ReforgeRecipe reforge, @NotNull StatBlock totalRated, @NotNull StatBlock totalCap)
        implements IItem {
    public static SolvableItem of(ItemData item) {
        // TODO can we drop some of ref
        return new SolvableItem(item.shared.ref().itemId(), item.shared.ref().itemLevel(), item.shared.ref().itemLevelBase(), item.shared.ref().duplicateNum(),
                item.slot(), item.reforge, item.totalRated, item.totalCap);
    }

    @NotNull
    public ReforgeRecipe reforge() {
        return reforge;
    }

    public boolean isIdenticalItem(SolvableItem other) {
        return itemId == other.itemId && itemLevel == other.itemLevel && itemLevelBase == other.itemLevelBase && duplicateNum == other.duplicateNum && slot == other.slot && Objects.equals(reforge, other.reforge) && Objects.equals(totalRated, other.totalRated) && Objects.equals(totalCap, other.totalCap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return isIdenticalItem((SolvableItem) o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, itemLevel, itemLevelBase, duplicateNum, slot, reforge, totalRated, totalCap);
    }

    public boolean isSameItem(ItemRef ref) {
        return itemId == ref.itemId() && itemLevel == ref.itemLevel() && duplicateNum == ref.duplicateNum();
    }
}
