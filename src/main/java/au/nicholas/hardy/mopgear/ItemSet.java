package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;

import java.util.function.ToLongFunction;

public final class ItemSet {
    public final CurryQueue<ItemData> items;
    public final StatBlock totals;
//    public long statRating;

    private ItemSet(CurryQueue<ItemData> items, StatBlock totals) {
        this.items = items;
        this.totals = totals;
    }

    public static ItemSet singleItem(ItemData item) {
        return new ItemSet(CurryQueue.single(item), item.totalStatCopy());
    }

    public ItemSet copyWithAddedItem(ItemData item) {
        return new ItemSet(items.prepend(item), totals.plus(item.totalStatCopy()));
    }

//    public ItemSet finished(ToLongFunction<StatBlock> func) {
//        statRating = func.applyAsLong(totals);
//        return this;
//    }
//
//    public long getStatRating() {
//        return statRating;
//    }

    public StatBlock getTotals() {
        return totals;
    }

    public CurryQueue<ItemData> getItems() {
        return items;
    }
}
