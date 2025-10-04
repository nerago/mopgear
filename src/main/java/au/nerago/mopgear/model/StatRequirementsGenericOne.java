package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public class StatRequirementsGenericOne implements StatRequirements.StatRequirementsSkinnySupport {
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
    public SkinnyItem toSkinny(SlotEquip slot, ItemData item) {
        return new SkinnyItem(slot, item.totalStat(stat), 0);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, ItemData item) {
        return skinny.one() == item.totalStat(stat);
    }

    @Override
    public boolean filterOneSet(ItemSet set) {
        int value = set.getTotals().get(stat);
        if (hasMaximum()) {
            return minimum <= value && value <= maximum;
        } else {
            return minimum <= value;
        }
    }

    @Override
    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        if (hasMaximum()) {
            return stream.filter(set -> {
                int value = set.getTotals().get(stat);
                return minimum <= value && value <= maximum;
            });
        } else {
            return stream.filter(set -> {
                int value = set.getTotals().get(stat);
                return minimum <= value;
            });
        }
    }

    @Override
    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        if (hasMaximum()) {
            return stream.filter(set -> {
                int value = set.getTotals().get(stat);
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
