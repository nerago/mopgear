package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.CurryQueue;

class ItemSet {
    CurryQueue<ItemData> items;
    ItemData totals;

    public ItemSet(CurryQueue<ItemData> items) {
        this.items = items;
        totals = sum(items);
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
