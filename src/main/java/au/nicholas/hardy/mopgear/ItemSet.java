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
        return ((long)totals.haste << 32) | ((long)totals.mastery << 16) | totals.crit;
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
