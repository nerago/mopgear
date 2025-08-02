package au.nicholas.hardy.mopgear;

import java.util.EnumMap;

public final class ItemSet {
    public final EnumMap<SlotEquip, ItemData> items;
    public final StatBlock totals;
    public final ItemSet otherSet;

    private ItemSet(EnumMap<SlotEquip, ItemData> items, StatBlock totals, ItemSet otherSet) {
        this.items = items;
        this.totals = totals;
        this.otherSet = otherSet;
    }

    public static ItemSet manyItems(EnumMap<SlotEquip, ItemData> items, ItemSet otherSet) {
        // trust caller is creating unique maps
        StatBlock totals = StatBlock.sum(items);
        return new ItemSet(items, totals, otherSet);
    }

    public static ItemSet singleItem(SlotEquip slot, ItemData item, ItemSet otherSet) {
        EnumMap<SlotEquip, ItemData> itemMap = new EnumMap<>(SlotEquip.class);
        itemMap.put(slot, item);
        return new ItemSet(itemMap, item.totalStatCopy(), otherSet);
    }

    public ItemSet copyWithAddedItem(SlotEquip slot, ItemData item) {
        EnumMap<SlotEquip, ItemData> itemMap = items.clone();
        itemMap.put(slot, item);
        return new ItemSet(itemMap, totals.plus(item.stat, item.statFixed), otherSet);
    }

    public StatBlock getTotals() {
        return totals;
    }

    public EnumMap<SlotEquip, ItemData> getItems() {
        return items;
    }

    void outputSet(StatRatings statRatings) {
        System.out.println(getTotals() + " " + statRatings.calcRating(getTotals()));
        for (ItemData it : getItems().values()) {
            System.out.println(it + " " + statRatings.calcRating(it.totalStatCopy()));
        }
    }
}
