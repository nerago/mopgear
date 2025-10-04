package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public class StatRequirementsHitOnly implements StatRequirements.StatRequirementsSkinnySupport {
    private final long minimum;
    private final long maximum;

    public StatRequirementsHitOnly(long minimum, long exceed) {
        this.minimum = minimum;
        this.maximum = minimum + exceed;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, ItemData item) {
        int value = item.stat.hit() + item.statFixed.hit();
        return new SkinnyItem(slot, value, 0);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, ItemData item) {
        int value = item.stat.hit() + item.statFixed.hit();
        return skinny.one() == value;
    }

    @Override
    public boolean filterOneSet(ItemSet set) {
        int value = set.getTotals().hit();
        return minimum <= value && value <= maximum;
    }

    @Override
    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        return stream.filter(set -> {
            int value = set.getTotals().hit();
            return minimum <= value && value <= maximum;
        });
    }

    @Override
    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return stream.filter(set -> {
            int value = set.getTotals().hit();
            return value <= maximum;
        });
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsSkinny(Stream<SkinnyItemSet> stream) {
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
