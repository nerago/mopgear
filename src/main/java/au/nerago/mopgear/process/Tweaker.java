package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.BestHolder;

public class Tweaker {
    public static SolvableItemSet tweak(SolvableItemSet baseSet, ModelCombined model, EquipOptionsMap items) {
        BestHolder<SolvableItemSet> best = new BestHolder<>(baseSet, model.calcRating(baseSet));

        for (SlotEquip slot : SlotEquip.values()) {
            SolvableItemSet itemSet = best.get();
            SolvableEquipMap baseItems = itemSet.items();
            SolvableItem existing = baseItems.get(slot);
            SolvableItem[] slotItems = items.get(slot);
            if (existing == null && slotItems != null) {
                throw new IllegalStateException("options offered for slot " + slot + " but existing set has as empty");
            } else if (existing != null && slotItems == null) {
                throw new IllegalStateException("no options offered for slot " + slot + " but existing set has " + existing);
            } else if (existing == null) {
                continue;
            }

            for (SolvableItem replace : slotItems) {
                if (replace != existing) {
                    SolvableItemSet proposed = substitutedSet(slot, replace, baseItems);
                    if (model.filterOneSet(proposed)) {
                        best.add(proposed, model.calcRating(proposed));
                    }
                }
            }
        }

        return best.get();
    }

    private static SolvableItemSet substitutedSet(SlotEquip slot, SolvableItem replace, SolvableEquipMap baseItems) {
        SolvableEquipMap map = baseItems.copyWithReplace(slot, replace);
        return SolvableItemSet.manyItems(map, null);
    }
}
