package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.BestHolder;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.util.*;

public class EngineStack {
    private final List<Tuple.Tuple2<SlotEquip, ItemData[]>> slotItems;
    private final ArrayDeque<Step> queue;
    private BestHolder<ItemSet> best;
    private ModelCombined model;
    private StatBlock adjustment;

    public EngineStack(ModelCombined model, EquipOptionsMap items, StatBlock adjustment) {
        this.slotItems = items.entrySet();
        this.model = model;
        this.adjustment = adjustment;
        this.queue = new ArrayDeque<>();
    }

    public Optional<ItemSet> runSolver() {
        addFirstItem();
        mainLoop();

        if (best != null)
            return Optional.ofNullable(best.get());
        else
            return Optional.empty();
    }

    private void addFirstItem() {
        Tuple.Tuple2<SlotEquip, ItemData[]> first = slotItems.getFirst();
        for (ItemData item : first.b()) {
            queue.addLast(new Step(1, ItemSet.singleItem(first.a(), item, adjustment)));
        }
    }

    private void mainLoop() {
        int itemsSize = slotItems.size();
        while (!queue.isEmpty()) {
            Step prev = queue.removeLast();
            ItemSet prevSet = prev.set;
            int index = prev.nextIndex();
            if (index < itemsSize) {
                Tuple.Tuple2<SlotEquip, ItemData[]> nextEntry = slotItems.get(index);
                int nextIndex = index + 1;
                for (ItemData item : nextEntry.b()) {
                    queue.addLast(new Step(nextIndex, prevSet.copyWithAddedItem(nextEntry.a(), item)));
                }
            } else {
                long rating = model.calcRating(prevSet.totals);
                best.add(prevSet, rating);
            }
        }
    }

    private record Step(int nextIndex, ItemSet set) {
    }
}
