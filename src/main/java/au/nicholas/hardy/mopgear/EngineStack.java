package au.nicholas.hardy.mopgear;

import java.util.*;

public class EngineStack {
    private final List<Map.Entry<SlotEquip, List<ItemData>>> slotItems;
    private final ArrayDeque<Step> queue;
    private ItemSet best;
    private long bestRating;
    private StatRatings statRatings;

    public EngineStack(Map<SlotEquip, List<ItemData>> items) {
        slotItems = items.entrySet().stream().toList();
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
        Map.Entry<SlotEquip, List<ItemData>> first = slotItems.getFirst();
        for (ItemData item : first.getValue()) {
            queue.addLast(new Step(1, ItemSet.singleItem(first.getKey(), item, null)));
        }
    }

    private void mainLoop() {
        int itemsSize = slotItems.size();
        while (!queue.isEmpty()) {
            Step prev = queue.removeLast();
            ItemSet prevSet = prev.set;
            int index = prev.nextIndex();
            if (index < itemsSize) {
                Map.Entry<SlotEquip, List<ItemData>> nextItems = slotItems.get(index);
                int nextIndex = index + 1;
                for (ItemData item : nextItems.getValue()) {
                    queue.addLast(new Step(nextIndex, prevSet.copyWithAddedItem(nextItems.getKey(), item)));
                }
            } else {
                long rating = statRatings.calcRating(prevSet.totals);
                if (best == null || bestRating < rating) {
                    best = prevSet;
                    bestRating = rating;
                }
            }
        }
    }

    private record Step(int nextIndex, ItemSet set) {
    }
}
