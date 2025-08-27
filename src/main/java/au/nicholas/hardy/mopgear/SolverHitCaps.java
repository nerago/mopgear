package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.CurryQueue;
import au.nicholas.hardy.mopgear.util.TopCollectorN;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SolverHitCaps {
    private final ModelCombined model;

    // work out hit in a first pass
    // per item work out values it can contribute, solve on that
    // then remaining stats

    public SolverHitCaps(ModelCombined model) {
        this.model = model;
    }

    public Stream<SkinnySet> solveHitCaps(EquipOptionsMap items) {
        System.out.println("COMBINATION-RAW "+ ItemUtil.estimateSets(items));

        List<SkinnyItem[]> skinnyOptions = convertToSkinny(items);
        System.out.println("COMBINATION-HIT "+ ItemUtil.estimateSets(skinnyOptions));

        Stream<SkinnySet> initialSets = generateSkinnyComboStream(skinnyOptions);
        Stream<SkinnySet> filteredSets = filterSets(initialSets);

//        Optional<SkinnySet> best = filteredSets.min(Comparator.comparingInt(ss -> ss.totalHit + ss.totalExpertise));
//        System.out.printf("%d %d\n", best.get().totalExpertise, best.get().totalHit);

//        Set<SkinnySet> options = filteredSets
////                .peek(ss -> System.out.printf("%d %d\n", ss.totalExpertise, ss.totalHit))
//                .collect(Collectors.toSet());

//        filteredSets.sorted(Comparator.comparingInt(ss -> ss.totalHit + ss.totalExpertise))
//                .limit(100)
//                .forEach(ss -> System.out.printf("%d %d\n", ss.totalExpertise, ss.totalHit));

        filteredSets.collect(new TopCollectorN<>(100, ss -> -(ss.totalHit + ss.totalExpertise)))
                .forEach(ss -> System.out.printf("%d %d\n", ss.totalExpertise, ss.totalHit));

        return null;
    }

    private Stream<SkinnySet> filterSets(Stream<SkinnySet> setStream) {
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

    private Stream<SkinnySet> generateSkinnyComboStream(List<SkinnyItem[]> optionsList) {
        Stream<SkinnySet> stream = null;

        for (SkinnyItem[] slotOptions : optionsList) {
            if (stream == null) {
                stream = newCombinationStream(slotOptions);
            } else {
                stream = addSlotToCombination(stream, slotOptions);
            }
        }
        return stream;
    }

    private Stream<SkinnySet> addSlotToCombination(Stream<SkinnySet> stream, SkinnyItem[] slotOptions) {
        return stream.mapMulti((set, next) -> {
            for (SkinnyItem item : slotOptions) {
                next.accept(new SkinnySet(
                        set.totalHit + item.hit,
                        set.totalExpertise + item.expertise,
                        set.items().prepend(item)));
            }
        });
    }

    private Stream<SkinnySet> newCombinationStream(SkinnyItem[] slotOptions) {
        final SkinnySet[] initialSets = new SkinnySet[slotOptions.length];
        for (int i = 0; i < slotOptions.length; ++i) {
            SkinnyItem item = slotOptions[i];
            initialSets[i] = new SkinnySet(item.hit, item.expertise, CurryQueue.single(item));
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

    public record SkinnySet(int totalHit, int totalExpertise, CurryQueue<SkinnyItem> items) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SkinnySet skinnySet = (SkinnySet) o;
//            return totalHit == skinnySet.totalHit && totalExpertise == skinnySet.totalExpertise && CurryQueue.equalsOther(items, skinnySet.items);
            return totalHit == skinnySet.totalHit && totalExpertise == skinnySet.totalExpertise;
        }

        @Override
        public int hashCode() {
//            return Objects.hash(totalHit, totalExpertise, items);
            return Objects.hash(totalHit, totalExpertise);
        }
    }
}
