package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;

class ItemSet {
    private CurryQueue<ItemData> items;
    private StatBlock totals;
    private long statRating;

    private ItemSet() {
    }

    private ItemSet(CurryQueue<ItemData> items, StatBlock totals) {
        this.items = items;
        this.totals = totals;
    }

    public static ItemSet singleItem(ItemData item) {
        return new ItemSet(CurryQueue.single(item), item.stat.copy());
    }

    public ItemSet copyWithAddedItem(ItemData item) {
        return new ItemSet(items.prepend(item), totals.plus(item.stat));
    }

    public void addItem(ItemData item) {
        items = CurryQueue.prepend(item, items);
        totals.increment(item.stat);
    }

    public ItemSet finished() {
        statRating = makeRating(totals);
        return this;
    }

    /***
     * @see ModelParams#priority
     * Secondary.Haste, Secondary.Mastery, Secondary.Crit
     * Maxes on armor around 1349, weapon 1021
     */
    private static long makeRating(StatBlock totals) {
        long value = 0;
        for (Secondary stat : ModelParams.priority) {
            value = (value << 16) | totals.get(stat);
        }
        return value;
    }

    public long getStatRating() {
        return statRating;
    }

    public StatBlock getTotals() {
        return totals;
    }

    public CurryQueue<ItemData> getItems() {
        return items;
    }
}
