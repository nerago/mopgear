package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.BestHolder;

public class Tweaker {
    public static ItemSet tweak(ItemSet baseSet, ModelCombined model, EquipOptionsMap items) {
        BestHolder<ItemSet> best = new BestHolder<>(baseSet, model.calcRating(baseSet));

        for (SlotEquip slot : SlotEquip.values()) {
            EquipMap baseItems = best.get().getItems();
            ItemData existing = baseItems.get(slot);
            ItemData[] slotItems = items.get(slot);
            if (existing == null ^ slotItems == null) {
                throw new IllegalStateException();
            } else if (existing == null) {
                continue;
            }

            for (ItemData replace : slotItems) {
                if (replace != existing) {
                    ItemSet proposed = substitutedSet(slot, replace, baseItems, baseSet.otherSet);
                    if (model.statRequirements().filter(proposed)) {
                        best.add(proposed, model.calcRating(proposed));
                    }
                }
            }
        }

        return best.get();
    }

    private static ItemSet substitutedSet(SlotEquip slot, ItemData replace, EquipMap baseItems, ItemSet otherSet) {
        EquipMap map = baseItems.copyWithReplace(slot, replace);
        return ItemSet.manyItems(map, otherSet, null);
    }
}
