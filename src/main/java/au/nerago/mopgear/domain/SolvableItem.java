package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;

public interface SolvableItem {
    @NotNull ReforgeRecipe reforge();

    @NotNull StatBlock totalRated();

    @NotNull StatBlock totalCap();

    @NotNull SlotItem slot();

    int itemId();

    boolean isIdenticalItem(SolvableItem other);

    boolean isSameItem(ItemRef ref);
}
