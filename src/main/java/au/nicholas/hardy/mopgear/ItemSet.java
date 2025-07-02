package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;

class ItemSet {
    CurryQueue<ItemData> items;
    ItemData totals;
    long statRating;

    public ItemSet(CurryQueue<ItemData> items) {
        this.items = items;
        totals = sum(items);
        statRating = makeRating(totals);
    }

    /***
     * @see ModelParams#priority
     * Secondary.Haste, Secondary.Mastery, Secondary.Crit
     */
    private static long makeRating(ItemData totals) {
        long value = 0;
        for (Secondary stat : ModelParams.priority) {
            value = (value << 16) | totals.get(stat);
        }
        return value;
    }

    private static ItemData sum(CurryQueue<ItemData> items) {
        ItemData value = new ItemData();
        while (items != null) {
            value.increment(items.item());
            items = items.tail();
        }
        return value;
    }
}
