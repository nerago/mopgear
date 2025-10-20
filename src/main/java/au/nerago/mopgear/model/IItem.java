package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.ItemRef;
import au.nerago.mopgear.domain.ReforgeRecipe;
import au.nerago.mopgear.domain.SlotItem;
import au.nerago.mopgear.domain.StatBlock;
import org.jetbrains.annotations.NotNull;

public interface IItem {
    boolean isSameItem(ItemRef ref);

    @NotNull SlotItem slot();

    int itemId();

    @NotNull ReforgeRecipe reforge();

    @NotNull StatBlock totalRated();

    @NotNull StatBlock totalCap();
}
