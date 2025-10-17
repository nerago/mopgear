package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.BestHolder;

public class Tweaker {
    public static ItemSet tweak(ItemSet baseSet, ModelCombined model, EquipOptionsMap items) {
        BestHolder<ItemSet> best = new BestHolder<>(baseSet, model.calcRating(baseSet));

        for (SlotEquip slot : SlotEquip.values()) {
            ItemSet itemSet = best.get();
            EquipMap baseItems = itemSet.items();
            ItemData existing = baseItems.get(slot);
            ItemData[] slotItems = items.get(slot);
            if (existing == null && slotItems != null) {
                throw new IllegalStateException("options offered for slot " + slot + " but existing set has as empty");
            } else if (existing != null && slotItems == null) {
                throw new IllegalStateException("no options offered for slot " + slot + " but existing set has " + existing);
            } else if (existing == null) {
                continue;
            }

            for (ItemData replace : slotItems) {
                if (replace != existing) {
                    ItemSet proposed = substitutedSet(slot, replace, baseItems);
                    if (model.filterOneSet(proposed)) {
                        best.add(proposed, model.calcRating(proposed));
                    }
                }
            }
        }

        return best.get();
    }

    private static ItemSet substitutedSet(SlotEquip slot, ItemData replace, EquipMap baseItems) {
        EquipMap map = baseItems.copyWithReplace(slot, replace);
        return ItemSet.manyItems(map, null);
    }
}
