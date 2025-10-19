package au.nerago.mopgear.domain;

import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.OutputText;

public record ItemSet(StatBlock totalForRating, StatBlock totalForCaps, EquipMap items) {
    public static ItemSet manyItems(EquipMap items, StatBlock adjustment) {
        // trust caller is creating unique maps
        StatBlock rating = StatBlock.sumForRating(items);
        StatBlock caps = StatBlock.sumForCaps(items);
        if (adjustment != null) {
            rating = rating.plus(adjustment);
            caps = caps.plus(adjustment);
        }
        return new ItemSet(rating, caps, items);
    }

    public static ItemSet singleItem(SlotEquip slot, ItemData item, StatBlock adjustment) {
        EquipMap itemMap = EquipMap.single(slot, item);
        StatBlock rating, caps;
        if (item.slot.addEnchantToCap) {
            caps = item.totalRated;
            if (adjustment != null) {
                caps = caps.plus(adjustment);
            }
            rating = caps;
        } else {
            caps = item.statBase;
            rating = item.totalRated;
            if (adjustment != null) {
                rating = rating.plus(adjustment);
                caps = caps.plus(adjustment);
            }
        }
        return new ItemSet(rating, caps, itemMap);
    }

    public ItemSet copyWithAddedItem(SlotEquip slot, ItemData item) {
        EquipMap itemMap = items.copyWithReplace(slot, item);
        StatBlock rating = totalForRating.plus(item.statBase, item.statEnchant);
        StatBlock caps = item.slot.addEnchantToCap
                ? totalForCaps.plus(item.statBase, item.statEnchant)
                : totalForCaps.plus(item.statBase);
        return new ItemSet(rating, caps, itemMap);
    }

    public void outputSet(ModelCombined model) {
        OutputText.println(totalForRating.toStringExtended() + " " + model.calcRating(this));
        items.forEachValue(it -> OutputText.println(it + " " + model.calcRating(it)));
    }

    public void outputSetDetailed(ModelCombined model) {
        OutputText.println("SET RATED " + totalForRating.toStringExtended() + " " + model.calcRating(this));
        OutputText.println("SET CONSTANT " + totalForCaps.toStringExtended());
        items.forEachValue(it -> OutputText.println(it.toStringExtended() + " " + model.calcRating(it)));
    }

    public void outputSetLight() {
        items.forEachValue(it -> OutputText.printf("%s [%d]\n", it.name, it.ref.itemLevel()));
    }

    public boolean validate() {
        ItemData weapon = items.getWeapon();
        if (weapon == null)
            throw new IllegalStateException("no weapon in set");
        if (weapon.slot == SlotItem.Weapon2H && items.getOffhand() != null)
            throw new IllegalStateException("weapon 2H with unexpected offhand");
        if (weapon.slot == SlotItem.Weapon1H && items.getOffhand() == null)
            throw new IllegalStateException("weapon 1H with missing offhand");

        ItemData t1 = items.getTrinket1(), t2 = items.getTrinket2();
        ItemData r1 = items.getRing1(), r2 = items.getRing2();
        return (t1 == null || t2 == null || t1.ref.itemId() != t2.ref.itemId()) &&
                (r1 == null || r2 == null || r1.ref.itemId() != r2.ref.itemId());
    }

    @Override
    public String toString() {
        return totalForRating.toString();
    }
}
