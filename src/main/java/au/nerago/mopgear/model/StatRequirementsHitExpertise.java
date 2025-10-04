package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public class StatRequirementsHitExpertise implements StatRequirements.StatRequirementsSkinnySupport, StatRequirements.StatRequirementsWithHitExpertise {
    private final int hitMin, hitMax;
    private final int expertiseMin, expertiseMax;

    public StatRequirementsHitExpertise(int hitMin, int hitMax, int expertiseMin, int expertiseMax) {
        this.hitMin = hitMin;
        this.hitMax = hitMax;
        this.expertiseMin = expertiseMin;
        this.expertiseMax = expertiseMax;
    }

    public static StatRequirements ret() {
        return new StatRequirementsHitExpertise(
                StatRequirements.TARGET_RATING_MELEE,
                StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED,
                StatRequirements.TARGET_RATING_MELEE,
                StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED);
    }

    public static StatRequirements retWideCapRange() {
        return new StatRequirementsHitExpertise(
                StatRequirements.TARGET_RATING_MELEE,
                StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED * 5,
                StatRequirements.TARGET_RATING_MELEE,
                StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED * 5);
    }

    public static StatRequirements protFullExpertise() {
        return new StatRequirementsHitExpertise(
                StatRequirements.TARGET_RATING_MELEE, StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED,
                StatRequirements.TARGET_RATING_TANK, StatRequirements.TARGET_RATING_TANK + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED);
    }

    public static StatRequirements protFlexibleParry() {
        return new StatRequirementsHitExpertise(
                StatRequirements.TARGET_RATING_MELEE, StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED,
                StatRequirements.TARGET_RATING_MELEE, StatRequirements.TARGET_RATING_TANK);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, ItemData item) {
        int hit = item.stat.hit() + item.statFixed.hit();
        int exp = item.stat.expertise() + item.statFixed.expertise();
        return skinny.one() == hit && skinny.two() == exp;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, ItemData item) {
        int hit = item.stat.hit() + item.statFixed.hit();
        int expertise = item.stat.expertise() + item.statFixed.expertise();
        return new SkinnyItem(slot, hit, expertise);
    }

    @Override
    public boolean filterOneSet(ItemSet set) {
        StatBlock totals = set.getTotals();
        int hit = totals.hit();
        int expertise = totals.expertise();
        return hitMin <= hit && hit <= hitMax && expertiseMin <= expertise && expertise <= expertiseMax;
    }

    @Override
    public Stream<ItemSet> filterSets(Stream<ItemSet> setStream) {
        return setStream.filter(set -> {
            StatBlock stats = set.getTotals();
            int hit = stats.hit(), expertise = stats.expertise();
            return hitMin <= hit && hit <= hitMax && expertiseMin <= expertise && expertise <= expertiseMax;
        });
    }

    @Override
    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> setStream) {
        return setStream.filter(set -> {
            StatBlock stats = set.getTotals();
            int hit = stats.hit(), expertise = stats.expertise();
            return hit <= hitMax && expertise <= expertiseMax;
        });
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsSkinny(Stream<SkinnyItemSet> setStream) {
        return setStream.filter(set -> hitMin <= set.totalOne() && set.totalOne() <= hitMax
                && expertiseMin <= set.totalTwo() && set.totalTwo() <= expertiseMax);
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> setStream) {
        return setStream.filter(set -> set.totalOne() <= hitMax && set.totalTwo() <= expertiseMax);
    }

    @Override
    public int effectiveHit(StatBlock totals) {
        return totals.hit();
    }

    @Override
    public int getMinimumHit() {
        return hitMin;
    }

    @Override
    public int getMinimumExpertise() {
        return expertiseMin;
    }

    @Override
    public int getMaximumHit() {
        return hitMax;
    }

    @Override
    public int getMaximumExpertise() {
        return expertiseMax;
    }
}
