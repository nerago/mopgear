package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public class StatRequirementsHitCombined implements StatRequirements {
    private final long minimum;
    private final long maximum;

    public StatRequirementsHitCombined(long minimum, long exceed) {
        this.minimum = minimum;
        this.maximum = minimum + exceed;
    }

    private int effectiveHit(ItemSet set) {
        StatBlock totals = set.totalForCaps();
        return totals.hit() + totals.expertise() + totals.spirit();
    }

    private int effectiveHit(ItemData item) {
        StatBlock base = item.statBase, enchant = item.statEnchant;
        if (item.slot.addEnchantToCap) {
            return base.hit() + base.expertise() + base.spirit() + enchant.hit() + enchant.expertise() + enchant.spirit();
        } else {
            return base.hit() + base.expertise() + base.spirit();
        }
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
    public boolean skinnyRecommended() {
        return true;
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
