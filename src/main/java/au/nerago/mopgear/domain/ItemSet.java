package au.nerago.mopgear.domain;

import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.Tuple;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    @Override
    public String toString() {
        return totals.toString();
    }
}
