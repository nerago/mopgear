package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public class StatRequirementsGenericOne implements StatRequirements {
    private final StatType stat;
    private final long minimum;
    private final long maximum;

    public StatRequirementsGenericOne(StatType stat, long minimum) {
        this.stat = stat;
        this.minimum = minimum;
        this.maximum = Long.MAX_VALUE;
    }

    public StatRequirementsGenericOne(StatType stat, long minimum, long exceed) {
        this.stat = stat;
        this.minimum = minimum;
        this.maximum = minimum + exceed;
    }

    private boolean hasMaximum() {
        return maximum < Long.MAX_VALUE;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, SolvableItem item) {
        return new SkinnyItem(slot, item.totalCap().get(stat), 0);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, SolvableItem item) {
        return skinny.one() == item.totalCap().get(stat);
    }

    @Override
    public boolean skinnyRecommended() {
        return true;
    }

    @Override
    public boolean filterOneSet(SolvableItemSet set) {
        int value = set.totalForCaps().get(stat);
        if (hasMaximum()) {
            return minimum <= value && value <= maximum;
        } else {
            return minimum <= value;
        }
    }

    @Override
    public Stream<SolvableItemSet> filterSets(Stream<SolvableItemSet> stream) {
        if (hasMaximum()) {
            return stream.filter(set -> {
                int value = set.totalForCaps().get(stat);
                return minimum <= value && value <= maximum;
            });
        } else {
            return stream.filter(set -> {
                int value = set.totalForCaps().get(stat);
                return minimum <= value;
            });
        }
    }

    @Override
    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> stream) {
        if (hasMaximum()) {
            return stream.filter(set -> {
                int value = set.totalForCaps().get(stat);
                return value <= maximum;
            });
        } else {
            return stream;
        }
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsSkinny(Stream<SkinnyItemSet> stream) {
        if (hasMaximum()) {
            return stream.filter(set -> {
                int value = set.totalOne();
                return minimum <= value && value <= maximum;
            });
        } else {
            return stream.filter(set -> {
                int value = set.totalOne();
                return minimum <= value;
            });
        }
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> stream) {
        if (hasMaximum()) {
            return stream.filter(set -> {
                int value = set.totalOne();
                return value <= maximum;
            });
        } else {
            return stream;
        }
    }
}
