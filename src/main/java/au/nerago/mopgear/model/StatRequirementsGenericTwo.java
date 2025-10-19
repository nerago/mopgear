package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public class StatRequirementsGenericTwo implements StatRequirements {
    private final StatType statOne, statTwo;
    private final long minimumOne, maximumOne, minimumTwo, maximumTwo;

    public StatRequirementsGenericTwo(StatType statOne, long minimumOne, StatType statTwo, long minimumTwo) {
        this.statOne = statOne;
        this.minimumOne = minimumOne;
        this.maximumOne = Long.MAX_VALUE;
        this.statTwo = statTwo;
        this.minimumTwo = minimumTwo;
        this.maximumTwo = Long.MAX_VALUE;
    }

    public StatRequirementsGenericTwo(StatType statOne, long minimumOne, StatType statTwo, long minimumTwo, long exceed) {
        this.statOne = statOne;
        this.minimumOne = minimumOne;
        this.maximumOne = minimumOne + exceed;
        this.statTwo = statTwo;
        this.minimumTwo = minimumTwo;
        this.maximumTwo = minimumTwo + exceed;
    }

    public StatRequirementsGenericTwo(StatType statOne, long minimumOne, long maximumOne, StatType statTwo, long minimumTwo, long maximumTwo) {
        this.statOne = statOne;
        this.minimumOne = minimumOne;
        this.maximumOne = maximumOne;
        this.statTwo = statTwo;
        this.minimumTwo = minimumTwo;
        this.maximumTwo = maximumTwo;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, ItemData item) {
        return new SkinnyItem(slot, item.totalCap.get(statOne), item.totalCap.get(statTwo));
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, ItemData item) {
        if (skinny.one() != item.totalCap.get(statOne)) return false;
        return skinny.two() == item.totalCap.get(statTwo);
    }

    @Override
    public boolean skinnyRecommended() {
        return true;
    }

    @Override
    public boolean filterOneSet(ItemSet set) {
        int one = set.totalForCaps().get(statOne), two = set.totalForCaps().get(statTwo);
        return minimumOne <= one && one <= maximumOne && minimumTwo <= two && two <= maximumTwo;
    }

    @Override
    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        if (maximumOne < Long.MAX_VALUE && maximumTwo < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int one = set.totalForCaps().get(statOne), two = set.totalForCaps().get(statTwo);
                return minimumOne <= one && one <= maximumOne && minimumTwo <= two && two <= maximumTwo;
            });
        } else if (maximumOne < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int one = set.totalForCaps().get(statOne), two = set.totalForCaps().get(statTwo);
                return minimumOne <= one && one <= maximumOne && minimumTwo <= two;
            });
        } else if (maximumTwo < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int one = set.totalForCaps().get(statOne), two = set.totalForCaps().get(statTwo);
                return minimumOne <= one && minimumTwo <= two && two <= maximumTwo;
            });
        } else {
            return stream.filter(set -> {
                int one = set.totalForCaps().get(statOne), two = set.totalForCaps().get(statTwo);
                return minimumOne <= one && minimumTwo <= two;
            });
        }
    }

    @Override
    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        if (maximumOne < Long.MAX_VALUE && maximumTwo < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int one = set.totalForCaps().get(statOne), two = set.totalForCaps().get(statTwo);
                return one <= maximumOne && two <= maximumTwo;
            });
        } else if (maximumOne < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int one = set.totalForCaps().get(statOne);
                return one <= maximumOne;
            });
        } else if (maximumTwo < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int two = set.totalForCaps().get(statTwo);
                return two <= maximumTwo;
            });
        } else {
            return stream;
        }
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsSkinny(Stream<SkinnyItemSet> stream) {
        return stream.filter(set -> {
            int one = set.totalOne(), two = set.totalTwo();
            return minimumOne <= one && one <= maximumOne && minimumTwo <= two && two <= maximumTwo;
        });
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> stream) {
        if (maximumOne < Long.MAX_VALUE && maximumTwo < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int one = set.totalOne(), two = set.totalTwo();
                return one <= maximumOne && two <= maximumTwo;
            });
        } else if (maximumOne < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int one = set.totalOne();
                return one <= maximumOne;
            });
        } else if (maximumTwo < Long.MAX_VALUE) {
            return stream.filter(set -> {
                int two = set.totalTwo();
                return two <= maximumTwo;
            });
        } else {
            return stream;
        }
    }
}
