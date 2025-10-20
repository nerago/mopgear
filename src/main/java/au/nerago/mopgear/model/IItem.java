package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import org.jetbrains.annotations.NotNull;

public interface IItem {
    boolean isSameItem(ItemRef ref);

    @NotNull SlotItem slot();

    int itemId();

    @NotNull ReforgeRecipe reforge();

    @NotNull StatBlock totalRated();

    @NotNull StatBlock totalCap();
}
