package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class SolverCapPhased {
    public static final int TOP_HIT_COMBO_FILTER = 1000;
    private final ModelCombined model;
    private final StatBlock adjustment;
//    private AtomicLong initCount;
//    private AtomicLong filterCount;
//    private AtomicLong filter2Count;
    private long estimate;

    // work out hit in a first pass
    // per item work out values it can contribute, solve on that
    // then remaining stats

    public SolverCapPhased(ModelCombined model, StatBlock adjustment) {
        this.model = model;
        this.adjustment = adjustment;
    }

    public Optional<ItemSet> runSolver(EquipOptionsMap items) {
        try {
            Stream<ItemSet> finalSets = runSolverPartial(items, true);
            return BigStreamUtil.findBest(model, finalSets);
        } finally {
            postSolve();
        }
    }

    public Optional<ItemSet> runSolverSingleThread(EquipOptionsMap items) {
        try {
            Stream<ItemSet> finalSets = runSolverPartial(items, false);
            return BigStreamUtil.findBest(model, finalSets);
        } finally {
            postSolve();
        }
    }

    private void postSolve() {
//        System.out.printf("PHASED COUNTS est=%d init=%d filter=%d filter2=%d\n", estimate, initCount.get(), filterCount.get(), filter2Count.get());
    }

    private Stream<ItemSet> runSolverPartial(EquipOptionsMap items, boolean parallel) {
        List<SkinnyItem[]> skinnyOptions = convertToSkinny(items);

        estimate = ItemUtil.estimateSets(skinnyOptions);
//        initCount = new AtomicLong();
//        filterCount = new AtomicLong();
//        filter2Count = new AtomicLong();

        Stream<SkinnyItemSet> initialSets = generateSkinnyComboStream(skinnyOptions, parallel);
//        initialSets = initialSets.peek(x -> initCount.incrementAndGet());

        Stream<SkinnyItemSet> filteredSets = filterSetsInCapRange(initialSets);
//        filteredSets = filteredSets.peek(x -> filterCount.incrementAndGet());

//        ToLongFunction<SkinnyItemSet> ratingFunc = ss -> 1000000 - (ss.totalHit + ss.totalExpertise);
//        ToLongFunction<SkinnyItemSet> ratingFunc = ss -> ss.totalHit + ss.totalExpertise;
//        filteredSets = filteredSets.filter(new BottomNFilter<>(TOP_HIT_COMBO_FILTER, ratingFunc));
//        filteredSets = filteredSets.peek(x -> filter2Count.incrementAndGet());

        return filteredSets.map(skin -> makeFromSkinny(skin, items));
    }

    private ItemSet makeFromSkinny(SkinnyItemSet skinnySet, EquipOptionsMap fullItemOptions) {
        EquipMap chosenItems = EquipMap.empty();
        CurryQueue<SkinnyItem> itemQueue = skinnySet.items;
        while (itemQueue != null) {
            SkinnyItem skinny = itemQueue.item();
            SlotEquip slot = skinny.slot;
            ItemData[] fullSlotItems = fullItemOptions.get(slot);

            BestHolder<ItemData> bestSlotItem = new BestHolder<>(null, 0);
            for (ItemData item : fullSlotItems) {
                int hit = model.statRequirements().effectiveHit(item);
                int exp = model.statRequirements().effectiveExpertise(item);
                if (skinny.hit == hit && skinny.expertise == exp) {
                    long rating = model.statRatings().calcRating(item.stat, item.statFixed);
                    bestSlotItem.add(item, rating);
                }
            }

            chosenItems.put(slot, bestSlotItem.get());
            itemQueue = itemQueue.tail();
        }
        return ItemSet.manyItems(chosenItems, adjustment);
    }

    private Stream<SkinnyItemSet> filterSetsInCapRange(Stream<SkinnyItemSet> setStream) {
        final int minHit = model.statRequirements().getMinimumHit(), maxHit = model.statRequirements().getMaximumHit();
        final int minExp = model.statRequirements().getMinimumExpertise(), maxExp = model.statRequirements().getMaximumExpertise();
        if (minExp != 0 && maxExp != Integer.MAX_VALUE) {
            return setStream.filter(set ->
                    set.totalHit >= minHit && set.totalHit <= maxHit &&
                            set.totalExpertise >= minExp && set.totalExpertise <= maxExp);
        } else {
            return setStream.filter(set -> set.totalHit >= minHit && set.totalHit <= maxHit);
        }
    }

    private List<SkinnyItem[]> convertToSkinny(EquipOptionsMap items) {
        List<SkinnyItem[]> optionsList = new ArrayList<>();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] fullOptions = items.get(slot);
            if (fullOptions != null) {
                HashSet<SkinnyItem> slotSet = new HashSet<>();
                for (ItemData item : fullOptions) {
                    int hit = model.statRequirements().effectiveHit(item);
                    int expertise = model.statRequirements().effectiveExpertise(item);
                    SkinnyItem skinny = new SkinnyItem(slot, hit, expertise);
                    slotSet.add(skinny);
                }
                SkinnyItem[] slotArray = slotSet.toArray(SkinnyItem[]::new);
                optionsList.add(slotArray);
            }
        }
        return optionsList;
    }

    private Stream<SkinnyItemSet> generateSkinnyComboStream(List<SkinnyItem[]> optionsList, boolean parallel) {
        Stream<SkinnyItemSet> stream = null;

        for (SkinnyItem[] slotOptions : optionsList) {
            if (stream == null) {
                stream = newCombinationStream(slotOptions, parallel);
            } else {
                stream = addSlotToCombination(stream, slotOptions);
            }
        }
        return stream;
    }

    private Stream<SkinnyItemSet> addSlotToCombination(Stream<SkinnyItemSet> stream, SkinnyItem[] slotOptions) {
        return stream.mapMulti((set, next) -> {
            for (SkinnyItem item : slotOptions) {
                next.accept(new SkinnyItemSet(
                        set.totalHit + item.hit,
                        set.totalExpertise + item.expertise,
                        set.items().prepend(item)));
            }
        });
    }

    private Stream<SkinnyItemSet> newCombinationStream(SkinnyItem[] slotOptions, boolean parallel) {
        final SkinnyItemSet[] initialSets = new SkinnyItemSet[slotOptions.length];
        for (int i = 0; i < slotOptions.length; ++i) {
            SkinnyItem item = slotOptions[i];
            initialSets[i] = new SkinnyItemSet(item.hit, item.expertise, CurryQueue.single(item));
        }
        return parallel ? ArrayUtil.arrayStream(initialSets) : Arrays.stream(initialSets);
    }

    public record SkinnyItem(SlotEquip slot, int hit, int expertise) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SkinnyItem that = (SkinnyItem) o;
            return hit == that.hit && expertise == that.expertise && slot == that.slot;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, hit, expertise);
        }
    }

    public record SkinnyItemSet(int totalHit, int totalExpertise, CurryQueue<SkinnyItem> items) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SkinnyItemSet skinnySet = (SkinnyItemSet) o;
            return totalHit == skinnySet.totalHit && totalExpertise == skinnySet.totalExpertise;
        }

        @Override
        public int hashCode() {
//            return Objects.hash(totalHit, totalExpertise, items);
            return Objects.hash(totalHit, totalExpertise);
        }
    }
}
