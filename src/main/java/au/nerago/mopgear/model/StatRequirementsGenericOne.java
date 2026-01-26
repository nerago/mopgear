package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.StreamNeedClose;

import java.util.function.ToLongFunction;
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
        return new SkinnyItem(slot, 0, 0, item.totalCap().get(stat));
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, SolvableItem item) {
        return skinny.three() == item.totalCap().get(stat);
    }

    @Override
    public boolean skinnyRecommended() {
        return true;
    }

    @Override
    public ToLongFunction<SkinnyItemSet> skinnyRatingMinimiseFunc() {
        return SkinnyItemSet::totalThree;
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
    public StreamNeedClose<SolvableItemSet> filterSets(StreamNeedClose<SolvableItemSet> stream) {
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
    public StreamNeedClose<SkinnyItemSet> filterSetsSkinny(StreamNeedClose<SkinnyItemSet> stream) {
        if (hasMaximum()) {
            return stream.filter(set -> {
                int value = set.totalThree();
                return minimum <= value && value <= maximum;
            });
        } else {
            return stream.filter(set -> {
                int value = set.totalThree();
                return minimum <= value;
            });
        }
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> stream) {
        if (hasMaximum()) {
            return stream.filter(set -> {
                int value = set.totalThree();
                return value <= maximum;
            });
        } else {
            return stream;
        }
    }
}
