package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.StreamNeedClose;

import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class StatRequirementsHitOnly implements StatRequirements {
    private final long minimum;
    private final long maximum;

    public StatRequirementsHitOnly(long minimum, long exceed) {
        this.minimum = minimum;
        this.maximum = minimum + exceed;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, SolvableItem item) {
        int hit = item.totalCap().hit();
        return new SkinnyItem(slot, hit, 0, 0);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, SolvableItem item) {
        int hit = item.totalCap().hit();
        return skinny.one() == hit;
    }

    @Override
    public boolean skinnyRecommended() {
        return true;
    }

    @Override
    public ToLongFunction<SkinnyItemSet> skinnyRatingMinimiseFunc() {
        return SkinnyItemSet::totalOne;
    }

    @Override
    public boolean filterOneSet(SolvableItemSet set) {
        int value = set.totalForCaps().hit();
        return minimum <= value && value <= maximum;
    }

    @Override
    public StreamNeedClose<SolvableItemSet> filterSets(StreamNeedClose<SolvableItemSet> stream) {
        return stream.filter(set -> {
            int value = set.totalForCaps().hit();
            return minimum <= value && value <= maximum;
        });
    }

    @Override
    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> stream) {
        return stream.filter(set -> {
            int value = set.totalForCaps().hit();
            return value <= maximum;
        });
    }

    @Override
    public StreamNeedClose<SkinnyItemSet> filterSetsSkinny(StreamNeedClose<SkinnyItemSet> stream) {
        return stream.filter(set -> {
            int value = set.totalOne();
            return minimum <= value && value <= maximum;
        });
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> stream) {
        return stream.filter(set -> {
            int value = set.totalOne();
            return value <= maximum;
        });
    }
}
