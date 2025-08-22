package au.nicholas.hardy.mopgear.domain;

import au.nicholas.hardy.mopgear.model.ModelCombined;

public final class ItemSet {
    public final EquipMap items;
    public final StatBlock totals;
    public final ItemSet otherSet;

    private ItemSet(EquipMap items, StatBlock totals, ItemSet otherSet) {
        this.items = items;
        this.totals = totals;
        this.otherSet = otherSet;
    }

    public static ItemSet manyItems(EquipMap items, ItemSet otherSet, StatBlock adjustment) {
        // trust caller is creating unique maps
        StatBlock totals = StatBlock.sum(items);
        if (adjustment != null) {
            totals = totals.plus(adjustment);
        }
        return new ItemSet(items, totals, otherSet);
    }

    public static ItemSet singleItem(SlotEquip slot, ItemData item, ItemSet otherSet, StatBlock adjustment) {
        EquipMap itemMap = EquipMap.single(slot, item);
        StatBlock total;
        if (adjustment == null) {
            total = item.totalStatCopy();
        } else {
            total = adjustment.plus(item.stat, item.statFixed);
        }
        return new ItemSet(itemMap, total, otherSet);
    }

    public ItemSet copyWithAddedItem(SlotEquip slot, ItemData item) {
        EquipMap itemMap = items.copyWithReplace(slot, item);
        return new ItemSet(itemMap, totals.plus(item.stat, item.statFixed), otherSet);
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
}
