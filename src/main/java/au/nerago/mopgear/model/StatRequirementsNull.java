package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public class StatRequirementsNull implements StatRequirements {
    @Override
    public boolean filterOneSet(ItemSet set) {
        return true;
    }

    @Override
    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        return stream;
    }

    @Override
    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return stream;
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsSkinny(Stream<SkinnyItemSet> stream) {
        return stream;
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> stream) {
        return stream;
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, ItemData item) {
        return true;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, ItemData item) {
        return new SkinnyItem(slot, 0, 0);
    }

    @Override
    public boolean skinnyRecommended() {
        return false;
    }
}
