package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.StreamNeedClose;

import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class StatRequirementsNull implements StatRequirements {
    @Override
    public boolean filterOneSet(SolvableItemSet set) {
        return true;
    }

    @Override
    public StreamNeedClose<SolvableItemSet> filterSets(StreamNeedClose<SolvableItemSet> stream) {
        return stream;
    }

    @Override
    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> stream) {
        return stream;
    }

    @Override
    public StreamNeedClose<SkinnyItemSet> filterSetsSkinny(StreamNeedClose<SkinnyItemSet> stream) {
        return stream;
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> stream) {
        return stream;
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, SolvableItem item) {
        return true;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, SolvableItem item) {
        return new SkinnyItem(slot, 0, 0, 0);
    }

    @Override
    public boolean skinnyRecommended() {
        return false;
    }

    @Override
    public ToLongFunction<SkinnyItemSet> skinnyRatingMinimiseFunc() {
        return skinny -> 0;
    }
}
