package au.nerago.mopgear.domain;

import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.OutputText;

public final class ItemSet {
    public final EquipMap items;
    public final StatBlock totals;

    private ItemSet(EquipMap items, StatBlock totals) {
        this.items = items;
        this.totals = totals;
    }

    public static ItemSet manyItems(EquipMap items, StatBlock adjustment) {
        // trust caller is creating unique maps
        StatBlock totals = StatBlock.sum(items);
        if (adjustment != null) {
            totals = totals.plus(adjustment);
        }
        return new ItemSet(items, totals);
    }

    public static ItemSet singleItem(SlotEquip slot, ItemData item, StatBlock adjustment) {
        EquipMap itemMap = EquipMap.single(slot, item);
        StatBlock total;
        if (adjustment == null) {
            total = item.totalStatCopy();
        } else {
            total = adjustment.plus(item.stat, item.statFixed);
        }
        return new ItemSet(itemMap, total);
    }

    public ItemSet copyWithAddedItem(SlotEquip slot, ItemData item) {
        EquipMap itemMap = items.copyWithReplace(slot, item);
        return new ItemSet(itemMap, totals.plus(item.stat, item.statFixed));
    }

    public StatBlock getTotals() {
        return totals;
    }

    public EquipMap getItems() {
        return items;
    }

    public void outputSet(ModelCombined model) {
        OutputText.println(getTotals().toStringExtended() + " " + model.calcRating(this));
        getItems().forEachValue(it -> OutputText.println(it + " " + model.calcRating(it)));
    }

    public void outputSetDetailed(ModelCombined model) {
        OutputText.println(getTotals().toStringExtended() + " " + model.calcRating(this));
        getItems().forEachValue(it -> OutputText.println(it.toStringExtended() + " " + model.calcRating(it)));
    }

    public void outputSetLight() {
        getItems().forEachValue(it -> OutputText.printf("%s [%d]\n", it.name, it.ref.itemLevel()));
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
        return totals.toString();
    }
}
