package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.StreamNeedClose;

import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class StatRequirementsHitCombined implements StatRequirements {
    private final long minimum;
    private final long maximum;

    public StatRequirementsHitCombined(long minimum, long exceed) {
        this.minimum = minimum;
        this.maximum = minimum + exceed;
    }

    private int effectiveHit(SolvableItemSet set) {
        StatBlock totals = set.totalForCaps();
        return totals.hit() + totals.expertise() + totals.spirit();
    }

    private int effectiveHit(SolvableItem item) {
        StatBlock cap = item.totalCap();
        return cap.hit() + cap.expertise() + cap.spirit();
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, SolvableItem item) {
        int value = effectiveHit(item);
        return new SkinnyItem(slot, value, 0, 0);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, SolvableItem item) {
        int value = effectiveHit(item);
        return skinny.one() == value;
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
        int value = effectiveHit(set);
        return minimum <= value && value <= maximum;
    }

    @Override
    public StreamNeedClose<SolvableItemSet> filterSets(StreamNeedClose<SolvableItemSet> stream) {
        return stream.filter(set -> {
            int value = effectiveHit(set);
            return minimum <= value && value <= maximum;
        });
    }

    @Override
    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> stream) {
        return stream.filter(set -> {
            int value = effectiveHit(set);
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
