package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BestHolder;
import au.nerago.mopgear.util.BigStreamUtil;
import au.nerago.mopgear.util.CurryQueue;

import java.util.*;
import java.util.stream.Stream;

public class SolverCapPhased {
    //    public static final int TOP_HIT_COMBO_FILTER = 1000;
    private final ModelCombined model;
    private final StatBlock adjustment;

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

//        long estimate = ItemUtil.estimateSets(skinnyOptions);
//        System.out.printf("SKINNY COMBO %,d\n", estimate);

        Stream<SkinnyItemSet> initialSets = generateSkinnyComboStream(skinnyOptions, parallel);

        Stream<SkinnyItemSet> filteredSets = model.statRequirements().filterSetsSkinny(initialSets);

//        ToLongFunction<SkinnyItemSet> ratingFunc = ss -> ss.totalHit + ss.totalExpertise;
//        filteredSets = filteredSets.filter(new BottomNFilter<>(TOP_HIT_COMBO_FILTER, ratingFunc));

        return filteredSets.map(skin -> makeFromSkinny(skin, items));
    }

    private ItemSet makeFromSkinny(SkinnyItemSet skinnySet, EquipOptionsMap fullItemOptions) {
        StatRequirements statReq = model.statRequirements();
        EquipMap chosenItems = EquipMap.empty();
        CurryQueue<SkinnyItem> itemQueue = skinnySet.items;
        while (itemQueue != null) {
            SkinnyItem skinny = itemQueue.item();
            SlotEquip slot = skinny.slot;
            ItemData[] fullSlotItems = fullItemOptions.get(slot);

            BestHolder<ItemData> bestSlotItem = new BestHolder<>();
            for (ItemData item : fullSlotItems) {
                int hit = statReq.effectiveHit(item);
                int exp = statReq.effectiveExpertise(item);
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

    private List<SkinnyItem[]> convertToSkinny(EquipOptionsMap items) {
        StatRequirements statRequirements = model.statRequirements();
        List<SkinnyItem[]> optionsList = new ArrayList<>();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] fullOptions = items.get(slot);
            if (fullOptions != null) {
                HashSet<SkinnyItem> slotSet = new HashSet<>();
                for (ItemData item : fullOptions) {
                    int hit = statRequirements.effectiveHit(item);
                    int expertise = statRequirements.effectiveExpertise(item);
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

//        optionsList.sort(Comparator.comparingInt(array -> array.length));

        for (SkinnyItem[] slotOptions : optionsList) {
            if (stream == null) {
                stream = newCombinationStream(slotOptions, parallel);
            } else {
                stream = addSlotToCombination(stream, slotOptions);
            }
            stream = model.statRequirements().filterSetsMaxSkinny(stream);
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
