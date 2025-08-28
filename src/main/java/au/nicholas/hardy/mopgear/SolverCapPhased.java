package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.*;

import java.util.*;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class SolverCapPhased {
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
        Stream<ItemSet> finalSets = runSolverPartial(items);
        return BigStreamUtil.findBest(model, finalSets);
    }

    private Stream<ItemSet> runSolverPartial(EquipOptionsMap items) {
        return findBestHitCapSetups(items)
            .map(skin -> convertFromSkinny(skin, items))
            .map(this::makeSet);
    }

    private Stream<EquipOptionsMap> runSolverOptions(EquipOptionsMap items) {
        Stream<SkinnyItemSet> skinnyStream = findBestHitCapSetups(items);
        return skinnyStream.map(skin -> convertFromSkinny(skin, items));
    }

    private Stream<ItemSet> makeSet(Stream<EquipOptionsMap> optionStream) {
        return optionStream.map(this::makeSet);
    }

    private ItemSet makeSet(EquipOptionsMap options) {
        EquipMap chosenItems = EquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] slotOptions = options.get(slot);
            if (slotOptions.length == 0) {
                throw new IllegalStateException();
            } else if (slotOptions.length == 1) {
                chosenItems.put(slot, slotOptions[0]);
            } else {
                BestHolder<ItemData> bestHolder = new BestHolder<>(null, 0);
                for (ItemData item : slotOptions) {
                    bestHolder.add(item, model.statRatings().calcRating(item.stat, item.statFixed));
                }
                chosenItems.put(slot, bestHolder.get());
            }
        }
        return ItemSet.manyItems(chosenItems, adjustment);
    }

    private EquipOptionsMap convertFromSkinny(SkinnyItemSet skinnySet, EquipOptionsMap fullItemOptions) {
        EquipOptionsMap optionMap = EquipOptionsMap.empty();
        CurryQueue<SkinnyItem> itemQueue = skinnySet.items;
        while (itemQueue != null) {
            SkinnyItem skinny = itemQueue.item();
            SlotEquip slot = skinny.slot;
            optionMap.put(slot, matchingHitItems(skinny, fullItemOptions.get(slot)));
            itemQueue = itemQueue.tail();
        }
        return optionMap;
    }

    private ItemData[] matchingHitItems(SkinnyItem skinny, ItemData[] itemArray) {
        ArrayList<ItemData> matches = new ArrayList<>();
        for (ItemData fullItem : itemArray) {
            int hit = fullItem.totalStat(StatType.Hit);
            int exp = fullItem.totalStat(StatType.Expertise);
            if (skinny.hit == hit && skinny.expertise == exp) {
                matches.add(fullItem);
            }
        }
        return matches.toArray(ItemData[]::new);
    }

    private Stream<SkinnyItemSet> findBestHitCapSetups(EquipOptionsMap items) {
        System.out.println("COMBINATION-RAW "+ ItemUtil.estimateSets(items));

        List<SkinnyItem[]> skinnyOptions = convertToSkinny(items);
        System.out.println("COMBINATION-HIT "+ ItemUtil.estimateSets(skinnyOptions));

        Stream<SkinnyItemSet> initialSets = generateSkinnyComboStream(skinnyOptions);
        Stream<SkinnyItemSet> filteredSets = filterSets(initialSets);

        ToLongFunction<? super SkinnyItemSet> ratingFunc = ss -> (ss.totalHit + ss.totalExpertise) * -1;

        return filteredSets.filter(new TopNFilter<>(100, ratingFunc));
    }

    private Stream<SkinnyItemSet> filterSets(Stream<SkinnyItemSet> setStream) {
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
                    SkinnyItem skinny = new SkinnyItem(slot, item);
                    slotSet.add(skinny);
                }
                SkinnyItem[] slotArray = slotSet.toArray(SkinnyItem[]::new);
                optionsList.add(slotArray);
            }
        }
        return optionsList;
    }

    private Stream<SkinnyItemSet> generateSkinnyComboStream(List<SkinnyItem[]> optionsList) {
        Stream<SkinnyItemSet> stream = null;

        for (SkinnyItem[] slotOptions : optionsList) {
            if (stream == null) {
                stream = newCombinationStream(slotOptions);
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

    private Stream<SkinnyItemSet> newCombinationStream(SkinnyItem[] slotOptions) {
        final SkinnyItemSet[] initialSets = new SkinnyItemSet[slotOptions.length];
        for (int i = 0; i < slotOptions.length; ++i) {
            SkinnyItem item = slotOptions[i];
            initialSets[i] = new SkinnyItemSet(item.hit, item.expertise, CurryQueue.single(item));
        }
        return ArrayUtil.arrayStream(initialSets);
    }

    public record SkinnyItem(SlotEquip slot, int hit, int expertise) {
        public SkinnyItem(SlotEquip slot, ItemData item) {
            this(slot, item.totalStat(StatType.Hit), item.totalStat(StatType.Expertise));
        }

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
