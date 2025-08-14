package au.nicholas.hardy.mopgear;

public final class ItemSet {
    public final EquipMap items;
    public final StatBlock totals;
    public final ItemSet otherSet;

    private ItemSet(EquipMap items, StatBlock totals, ItemSet otherSet) {
        this.items = items;
        this.totals = totals;
        this.otherSet = otherSet;
    }

    public static ItemSet manyItems(EquipMap items, ItemSet otherSet) {
        // trust caller is creating unique maps
        StatBlock totals = StatBlock.sum(items);
        return new ItemSet(items, totals, otherSet);
    }

    public static ItemSet singleItem(SlotEquip slot, ItemData item, ItemSet otherSet) {
        EquipMap itemMap = new EquipMap();
        itemMap.put(slot, item);
        return new ItemSet(itemMap, item.totalStatCopy(), otherSet);
    }

    public ItemSet copyWithAddedItem(SlotEquip slot, ItemData item) {
        EquipMap itemMap = items.clone();
        itemMap.put(slot, item);
        return new ItemSet(itemMap, totals.plus(item.stat, item.statFixed), otherSet);
    }

    public StatBlock getTotals() {
        return totals;
    }

    public EquipMap getItems() {
        return items;
    }

    void outputSet(ModelCombined model) {
        System.out.println(getTotals().toStringExtended() + " " + model.calcRating(getTotals()));
        getItems().forEachValue(it -> System.out.println(it + " " + model.calcRating(it.totalStatCopy())));
    }
}
