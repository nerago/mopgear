package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.StreamNeedClose;

import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class StatRequirementsCombinedSkinnyOnlyFirst implements StatRequirements {
    private final StatRequirements a;
    private final StatRequirements b;

    public StatRequirementsCombinedSkinnyOnlyFirst(StatRequirements a, StatRequirements b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean filterOneSet(SolvableItemSet set) {
        return a.filterOneSet(set) && b.filterOneSet(set);
    }

    @Override
    public StreamNeedClose<SolvableItemSet> filterSets(StreamNeedClose<SolvableItemSet> stream) {
        return b.filterSets(a.filterSets(stream));
    }

    @Override
    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> stream) {
        return b.filterSetsMax(a.filterSetsMax(stream));
    }

    @Override
    public StreamNeedClose<SkinnyItemSet> filterSetsSkinny(StreamNeedClose<SkinnyItemSet> stream) {
        return a.filterSetsSkinny(stream);
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> stream) {
        return a.filterSetsMaxSkinny(stream);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, SolvableItem item) {
        return a.skinnyMatch(skinny, item);
    }

    @Override
    public ToLongFunction<SkinnyItemSet> skinnyRatingMinimiseFunc() {
        return a.skinnyRatingMinimiseFunc();
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, SolvableItem item) {
        return a.toSkinny(slot, item);
    }

    @Override
    public boolean skinnyRecommended() {
        return a.skinnyRecommended();
    }
}
