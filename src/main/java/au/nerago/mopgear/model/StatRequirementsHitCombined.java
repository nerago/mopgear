package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public class StatRequirementsHitCombined implements StatRequirements.StatRequirementsSkinnySupport {
    private final long minimum;
    private final long maximum;

    public StatRequirementsHitCombined(long minimum, long exceed) {
        this.minimum = minimum;
        this.maximum = minimum + exceed;
    }

    private int effectiveHit(ItemSet set) {
        StatBlock totals = set.totals;
        return totals.hit() + totals.expertise() + totals.spirit();
    }

    private int effectiveHit(ItemData item) {
        StatBlock stat = item.stat, statFixed = item.statFixed;
        return stat.hit() + statFixed.hit() +
                stat.expertise() + statFixed.expertise() +
                stat.spirit() + statFixed.spirit();
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, ItemData item) {
        int value = effectiveHit(item);
        return new SkinnyItem(slot, value, 0);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, ItemData item) {
        int value = effectiveHit(item);
        return skinny.one() == value;
    }

    @Override
    public boolean filterOneSet(ItemSet set) {
        int value = effectiveHit(set);
        return minimum <= value && value <= maximum;
    }

    @Override
    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        return stream.filter(set -> {
            int value = effectiveHit(set);
            return minimum <= value && value <= maximum;
        });
    }

    @Override
    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return stream.filter(set -> {
            int value = effectiveHit(set);
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
