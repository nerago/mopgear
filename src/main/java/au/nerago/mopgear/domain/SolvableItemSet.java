package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;

public record SolvableItemSet(StatBlock totalForRating, StatBlock totalForCaps, SolvableEquipMap items) {
    public static SolvableItemSet manyItems(SolvableEquipMap items, StatBlock adjustment) {
        // trust caller is creating unique maps
        StatBlock rating = StatBlock.sumForRating(items);
        StatBlock caps = StatBlock.sumForCaps(items);
        if (adjustment != null) {
            rating = rating.plus(adjustment);
            caps = caps.plus(adjustment);
        }
        return new SolvableItemSet(rating, caps, items);
    }

    public static SolvableItemSet singleItem(SlotEquip slot, SolvableItem item, StatBlock adjustment) {
        SolvableEquipMap itemMap = SolvableEquipMap.single(slot, item);
        StatBlock rating = item.totalRated();
        StatBlock caps = item.totalCap();
        if (adjustment != null) {
            rating = rating.plus(adjustment);
            caps = caps.plus(adjustment);
        }
        return new SolvableItemSet(rating, caps, itemMap);
    }

    public SolvableItemSet copyWithAddedItem(SlotEquip slot, SolvableItem item) {
        SolvableEquipMap itemMap = items.copyWithReplace(slot, item);
        StatBlock rating = totalForRating.plus(item.totalRated());
        StatBlock caps = totalForCaps.plus(item.totalCap());
        return new SolvableItemSet(rating, caps, itemMap);
    }

    public boolean validate() {
        SolvableItem weapon = items.getWeapon();
        if (weapon == null)
            throw new IllegalStateException("no weapon in set");
        if (weapon.slot() == SlotItem.Weapon2H && items.getOffhand() != null)
            throw new IllegalStateException("weapon 2H with unexpected offhand");
        if (weapon.slot() == SlotItem.Weapon1H && items.getOffhand() == null)
            throw new IllegalStateException("weapon 1H with missing offhand");

        SolvableItem t1 = items.getTrinket1(), t2 = items.getTrinket2();
        SolvableItem r1 = items.getRing1(), r2 = items.getRing2();
        return (t1 == null || t2 == null || t1.itemId() != t2.itemId()) &&
                (r1 == null || r2 == null || r1.itemId() != r2.itemId());
    }

    @Override
    public @NotNull String toString() {
        return totalForRating.toString();
    }
}
