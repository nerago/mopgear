package au.nicholas.hardy.mopgear;

import java.util.*;

public class EngineStack {
    private final List<List<ItemData>> slotItems;
    private final ArrayDeque<Step> queue;
    private ItemSet best;
    private Model model;

    public EngineStack(Map<SlotEquip, List<ItemData>> items) {
        slotItems = items.values().stream().toList();
        queue = new ArrayDeque<>();
    }

    public Collection<ItemSet> runSolver() {
        addFirstItem();
        mainLoop();

        if (best != null)
            return Collections.singleton(best);
        else
            return Collections.emptySet();
    }

    private void addFirstItem() {
        for (ItemData item : slotItems.getFirst()) {
            queue.addLast(new Step(1, ItemSet.singleItem(item)));
        }
    }

    private void mainLoop() {
        int itemsSize = slotItems.size();
        while (!queue.isEmpty()) {
            Step prev = queue.removeLast();
            ItemSet prevSet = prev.set;
            int index = prev.nextIndex();
            if (index < itemsSize) {
                List<ItemData> nextItems = slotItems.get(index);
                int nextIndex = index + 1;
                for (ItemData item : nextItems) {
                    queue.addLast(new Step(nextIndex, prevSet.copyWithAddedItem(item)));
                }
            } else {
                prevSet.finished(model::calcRating);
                if (best == null || best.statRating < prevSet.statRating) {
                    best = prevSet;
                }
            }
        }
    }

    private record Step(int nextIndex, ItemSet set) {
    }
}
