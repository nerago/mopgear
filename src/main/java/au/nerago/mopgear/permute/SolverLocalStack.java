package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.Tuple;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;

public class SolverLocalStack {
    private final List<Tuple.Tuple2<SlotEquip, SolvableItem[]>> slotItems;
    private final ArrayDeque<Step> queue;
    private BestHolder<SolvableItemSet> best;
    private final ModelCombined model;
    private final StatBlock adjustment;

    public SolverLocalStack(ModelCombined model, SolvableEquipOptionsMap items, StatBlock adjustment) {
        this.slotItems = items.entryStream().toList();
        this.model = model;
        this.adjustment = adjustment;
        this.queue = new ArrayDeque<>();
    }

    public Optional<SolvableItemSet> runSolver() {
        best = new BestHolder<>();
        addFirstItem();
        mainLoop();

        if (best != null)
            return Optional.ofNullable(best.get());
        else
            return Optional.empty();
    }

    private void addFirstItem() {
        Tuple.Tuple2<SlotEquip, SolvableItem[]> first = slotItems.getFirst();
        for (SolvableItem item : first.b()) {
            queue.addLast(new Step(1, SolvableItemSet.singleItem(first.a(), item, adjustment)));
        }
    }

    private void mainLoop() {
        int itemsSize = slotItems.size();
        while (!queue.isEmpty()) {
            Step prev = queue.removeLast();
            SolvableItemSet prevSet = prev.set;
            int index = prev.nextIndex();
            if (index < itemsSize) {
                Tuple.Tuple2<SlotEquip, SolvableItem[]> nextEntry = slotItems.get(index);
                int nextIndex = index + 1;
                for (SolvableItem item : nextEntry.b()) {
                    queue.addLast(new Step(nextIndex, prevSet.copyWithAddedItem(nextEntry.a(), item)));
                }
            } else {
                if (model.filterOneSet(prevSet)) {
                    long rating = model.calcRating(prevSet);
                    best.add(prevSet, rating);
                }
            }
        }
    }

    private record Step(int nextIndex, SolvableItemSet set) {
    }
}
