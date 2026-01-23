package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class StatRequirementsHitExpertise implements StatRequirements, StatRequirements.StatRequirementsWithHitExpertise {
    private final int hitMin, hitMax;
    private final int expertiseMin, expertiseMax;
    private boolean minimiseExpertise;

    public StatRequirementsHitExpertise(int hitMin, int hitMax, int expertiseMin, int expertiseMax, boolean minimiseExpertise) {
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
                StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED,
                true);
    }

    public static StatRequirements retWideCapRange() {
        return new StatRequirementsHitExpertise(
                StatRequirements.TARGET_RATING_MELEE,
                StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED * 5,
                StatRequirements.TARGET_RATING_MELEE,
                StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED * 5,
                true);
    }

    public static StatRequirements protFullExpertise() {
        return new StatRequirementsHitExpertise(
                StatRequirements.TARGET_RATING_MELEE, StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED,
                StatRequirements.TARGET_RATING_TANK, StatRequirements.TARGET_RATING_TANK + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED,
                true);
    }

    public static StatRequirements protFlexibleParry() {
        return new StatRequirementsHitExpertise(
                StatRequirements.TARGET_RATING_MELEE, StatRequirements.TARGET_RATING_MELEE + StatRequirements.DEFAULT_CAP_ALLOW_EXCEED * 2,
                StatRequirements.TARGET_RATING_MELEE, StatRequirements.TARGET_RATING_TANK,
                false);
    }

    public static StatRequirements protFlexibleParryNarrowHit() {
        return new StatRequirementsHitExpertise(
                StatRequirements.TARGET_RATING_MELEE, StatRequirements.TARGET_RATING_MELEE + 50,
                StatRequirements.TARGET_RATING_MELEE, StatRequirements.TARGET_RATING_TANK,
                false);
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, SolvableItem item) {
        int hit = item.totalCap().hit();
        int exp = item.totalCap().expertise();
        return skinny.one() == hit && skinny.two() == exp;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, SolvableItem item) {
        int hit = item.totalCap().hit();
        int exp = item.totalCap().expertise();
        return new SkinnyItem(slot, hit, exp, 0);
    }

    @Override
    public boolean skinnyRecommended() {
        return true;
    }

    @Override
    public ToLongFunction<SkinnyItemSet> skinnyRatingMinimiseFunc() {
        if (minimiseExpertise)
            return skinny -> skinny.totalOne() + skinny.totalTwo();
        else
            return SkinnyItemSet::totalOne;
    }

    @Override
    public boolean filterOneSet(SolvableItemSet set) {
        StatBlock totals = set.totalForCaps();
        int hit = totals.hit();
        int expertise = totals.expertise();
        return hitMin <= hit && hit <= hitMax && expertiseMin <= expertise && expertise <= expertiseMax;
    }

    @Override
    public Stream<SolvableItemSet> filterSets(Stream<SolvableItemSet> setStream) {
        return setStream.filter(set -> {
            StatBlock stats = set.totalForCaps();
            int hit = stats.hit(), expertise = stats.expertise();
            return hitMin <= hit && hit <= hitMax && expertiseMin <= expertise && expertise <= expertiseMax;
        });
    }

    @Override
    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> setStream) {
        return setStream.filter(set -> {
            StatBlock stats = set.totalForCaps();
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
