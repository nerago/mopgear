package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.BestHolder;

import java.util.EnumMap;

public class Tweaker {
    public static ItemSet tweak(ItemSet baseSet, ModelCombined model, EnumMap<SlotEquip, ItemData[]> items) {
        BestHolder<ItemSet> best = new BestHolder<>(baseSet, model.calcRating(baseSet));

        for (SlotEquip slot : SlotEquip.values()) {
            EnumMap<SlotEquip, ItemData> baseItems = best.get().getItems();
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
                    if (model.getStatRequirements().inRange(proposed.totals)) {
                        best.add(proposed, model.calcRating(proposed));
                    }
                }
            }
        }

        return best.get();
    }

    private static ItemSet substitutedSet(SlotEquip slot, ItemData replace, EnumMap<SlotEquip, ItemData> baseItems, ItemSet otherSet) {
        EnumMap<SlotEquip, ItemData> map = baseItems.clone();
        map.put(slot, replace);
        return ItemSet.manyItems(map, otherSet);
    }
}
