package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.Tuple;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;

public class SolverLocalStack {
    private final List<Tuple.Tuple2<SlotEquip, ItemData[]>> slotItems;
    private final ArrayDeque<Step> queue;
    private BestHolder<ItemSet> best;
    private final ModelCombined model;
    private final StatBlock adjustment;

    public SolverLocalStack(ModelCombined model, EquipOptionsMap items, StatBlock adjustment) {
        this.slotItems = items.entryStream().toList();
        this.model = model;
        this.adjustment = adjustment;
        this.queue = new ArrayDeque<>();
    }

    public Optional<ItemSet> runSolver() {
        best = new BestHolder<>();
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
                if (model.statRequirements().filter(prevSet)) {
                    long rating = model.calcRating(prevSet);
                    best.add(prevSet, rating);
                }
            }
        }
    }

    private record Step(int nextIndex, ItemSet set) {
    }
}
