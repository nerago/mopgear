package au.nicholas.hardy.mopgear.domain;

import au.nicholas.hardy.mopgear.model.ModelCombined;

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
        System.out.println(getTotals().toStringExtended() + " " + model.calcRating(getTotals()));
        getItems().forEachValue(it -> System.out.println(it + " " + model.calcRating(it.totalStatCopy())));
    }

    @Override
    public String toString() {
        return totals.toString();
    }
}
