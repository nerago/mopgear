package au.nerago.mopgear.model;

import au.nerago.mopgear.SolverCapPhased;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.ServiceEntry;
import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.StatBlock;

import java.util.stream.Stream;

public class StatRequirements {
//    private StatRequirements(int hit, int expertise, int exceed, boolean combine) {
//        hitMin = hit;
//        hitMax = hit + exceed;
//        expertiseMin = expertise;
//        expertiseMax = expertise + exceed;
//        combineHitLike = combine;
//    }

    public StatRequirements(int hitMin, int hitMax, int expertiseMin, int expertiseMax, boolean combineHitLike) {
        this.hitMin = hitMin;
        this.hitMax = hitMax;
        this.expertiseMin = expertiseMin;
        this.expertiseMax = expertiseMax;
        this.combineHitLike = combineHitLike;
    }

    public StatRequirements(int hitMin, int hitMax, boolean combineHitLike) {
        this.hitMin = hitMin;
        this.hitMax = hitMax;
        this.expertiseMin = 0;
        this.expertiseMax = Integer.MAX_VALUE;
        this.combineHitLike = combineHitLike;
    }

    public static StatRequirements ret() {
        return new StatRequirements(
                TARGET_RATING_MELEE,
                TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                TARGET_RATING_MELEE,
                TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements retWideCapRange() {
        return new StatRequirements(
                TARGET_RATING_MELEE,
                TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED * 5,
                TARGET_RATING_MELEE,
                TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED * 5,
                false);
    }

    public static StatRequirements prot() {
        return new StatRequirements(
                TARGET_RATING_MELEE, TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                TARGET_RATING_TANK, TARGET_RATING_TANK + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements protFlexibleParry() {
        return new StatRequirements(
                TARGET_RATING_MELEE, TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                TARGET_RATING_MELEE, TARGET_RATING_TANK + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements druidBalance() {
        return new StatRequirements(TARGET_RATING_CAST, TARGET_RATING_CAST + DEFAULT_CAP_ALLOW_EXCEED,
                true);
    }

    public static StatRequirements druidBear() {
        return new StatRequirements(
                TARGET_RATING_MELEE, TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                TARGET_RATING_MELEE, TARGET_RATING_TANK + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements warlock() {
        return new StatRequirements(TARGET_RATING_CAST_DUNGEON, TARGET_RATING_CAST_DUNGEON + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements zero() {
        return new StatRequirements(0, Integer.MAX_VALUE, false);
    }

    public static StatRequirements load(ServiceEntry.ServiceRequiredStats param) {
        return new StatRequirements(param.hit(),
                param.hit() != 0 && param.allowedExceed() != 0 ? param.hit() + param.allowedExceed() : Integer.MAX_VALUE,
                param.expertise(),
                param.expertise() != 0 && param.allowedExceed() != 0 ? param.expertise() + param.allowedExceed() : Integer.MAX_VALUE,
                param.combinedHit());
    }

    private static final double RATING_PER_PERCENT = 339.9534;
    private static final double TARGET_PERCENT_MELEE = 7.5;
    private static final double TARGET_PERCENT_TANK = 15;
    private static final double TARGET_PERCENT_CAST = 15;
    private static final int TARGET_RATING_MELEE = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_MELEE); // 2550
    private static final int TARGET_RATING_TANK = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_TANK); // 5100
    private static final int TARGET_RATING_CAST = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_CAST); // 5100
    private static final int TARGET_RATING_CAST_DUNGEON = (int) Math.ceil(RATING_PER_PERCENT * 12); // 4080

    private static final int DEFAULT_CAP_ALLOW_EXCEED = 250;

    private final int hitMin, hitMax;
    private final int expertiseMin, expertiseMax;
    private final boolean combineHitLike;

    public int effectiveHit(StatBlock totals) {
        return combineHitLike ? totals.hit + totals.expertise + totals.spirit : totals.hit;
    }

    public int effectiveHit(ItemData item) {
        if (combineHitLike) {
            return item.stat.hit + item.statFixed.hit +
                   item.stat.expertise + item.statFixed.expertise +
                   item.stat.spirit + item.statFixed.spirit;
        } else {
            return item.stat.hit + item.statFixed.hit;
        }
    }

    public int effectiveExpertise(ItemData item) {
        if (combineHitLike) {
            return 0;
        } else {
            return item.stat.expertise + item.statFixed.expertise;
        }
    }

    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        return stream.filter(this::filter);
    }

    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return stream.filter(set -> inRangeMax(set.getTotals()) && noDuplicates(set.items));
    }

    public boolean filter(ItemSet set) {
        return inRange(set.getTotals()) && noDuplicates(set.items);
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean inRange(StatBlock totals) {
        if (hitMin != 0) {
            int hit = effectiveHit(totals);
            if (hit < hitMin || hit > hitMax)
                return false;
        }
        if (expertiseMin != 0) {
            if (totals.expertise < expertiseMin || totals.expertise > expertiseMax)
                return false;
        }
        return true;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean inRangeMax(StatBlock totals) {
        if (hitMax != Integer.MAX_VALUE) {
            int hit = effectiveHit(totals);
            if (hit > hitMax)
                return false;
        }
        if (expertiseMax != Integer.MAX_VALUE) {
            if (totals.expertise > expertiseMax)
                return false;
        }
        return true;
    }

    public Stream<SolverCapPhased.SkinnyItemSet> filterSetsSkinny(Stream<SolverCapPhased.SkinnyItemSet> setStream) {
        final int minHit = hitMin, maxHit = hitMax;
        final int minExp = expertiseMin, maxExp = expertiseMax;

        if (minExp != 0 && maxExp != Integer.MAX_VALUE && minHit != 0 && maxHit != Integer.MAX_VALUE) {
            return setStream.filter(set -> set.totalHit() >= minHit && set.totalHit() <= maxHit
                    && set.totalExpertise() >= minExp && set.totalExpertise() <= maxExp);
        }

        if (minHit != 0 && maxHit != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> set.totalHit() >= minHit && set.totalHit() <= maxHit);
        } else if (minHit != 0) {
            setStream = setStream.filter(set -> set.totalHit() >= minHit);
        } else if (maxHit != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> set.totalHit() <= maxHit);
        }

        if (minExp != 0 && maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> set.totalExpertise() >= minExp && set.totalExpertise() <= maxExp);
        } else if (minExp != 0) {
            setStream = setStream.filter(set -> set.totalExpertise() >= minExp);
        } else if (maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> set.totalExpertise() <= maxExp);
        }

        return setStream;
    }

    private boolean noDuplicates(EquipMap items) {
        ItemData t1 = items.getTrinket1(), t2 = items.getTrinket2();
        ItemData r1 = items.getRing1(), r2 = items.getRing2();
        return (t1 == null || t2 == null || t1.id != t2.id) &&
                (r1 == null || r2 == null || r1.id != r2.id);
    }

    public int getMinimumHit() {
        return hitMin;
    }

    public int getMinimumExpertise() {
        return expertiseMin;
    }

    public int getMaximumHit() {
        return hitMax;
    }

    public int getMaximumExpertise() {
        return expertiseMax;
    }
}
