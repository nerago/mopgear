package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.ServiceEntry;
import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.StatBlock;

import java.util.stream.Stream;

public class StatRequirements {
    private StatRequirements(int hit, int expertise, int exceed, boolean combine) {
        requiredHit = hit;
        requiredExpertise = expertise;
        maxExceed = exceed;
        combineHitLike = combine;
    }

    public static StatRequirements ret() {
        return new StatRequirements(TARGET_RATING_MELEE, TARGET_RATING_MELEE, DEFAULT_CAP_ALLOW_EXCEED, false);
    }

    public static StatRequirements retWideCapRange() {
        return new StatRequirements(TARGET_RATING_MELEE, TARGET_RATING_MELEE, DEFAULT_CAP_ALLOW_EXCEED * 10, false);
    }

    public static StatRequirements prot() {
        return new StatRequirements(TARGET_RATING_MELEE, TARGET_RATING_TANK, DEFAULT_CAP_ALLOW_EXCEED, false);
    }

    public static StatRequirements druidBalance() {
        return new StatRequirements(TARGET_RATING_CAST, 0, DEFAULT_CAP_ALLOW_EXCEED, true);
    }

    public static StatRequirements warlock() {
        return new StatRequirements(TARGET_RATING_CAST_DUNGEON, 0, DEFAULT_CAP_ALLOW_EXCEED, false);
    }

    public static StatRequirements zero() {
        return new StatRequirements(0, 0, DEFAULT_CAP_ALLOW_EXCEED, false);
    }

    public static StatRequirements load(ServiceEntry.ServiceRequiredStats param) {
        return new StatRequirements(param.hit(), param.expertise(), param.allowedExceed(), param.combinedHit());
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

    private final int requiredHit;
    private final int requiredExpertise;
    private final int maxExceed;
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
        if (requiredHit != 0) {
            int hit = effectiveHit(totals);
            if (hit < requiredHit || hit > requiredHit + maxExceed)
                return false;
        }
        if (requiredExpertise != 0) {
            if (totals.expertise < requiredExpertise || totals.expertise > requiredExpertise + maxExceed)
                return false;
        }
        return true;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean inRangeMax(StatBlock totals) {
        if (requiredHit != 0) {
            int hit = effectiveHit(totals);
            if (hit > requiredHit + maxExceed)
                return false;
        }
        if (requiredExpertise != 0) {
            if (totals.expertise > requiredExpertise + maxExceed)
                return false;
        }
        return true;
    }

    private boolean noDuplicates(EquipMap items) {
        ItemData t1 = items.getTrinket1(), t2 = items.getTrinket2();
        ItemData r1 = items.getRing1(), r2 = items.getRing2();
        return (t1 == null || t2 == null || t1.id != t2.id) &&
                (r1 == null || r2 == null || r1.id != r2.id);
    }

    public int getMinimumHit() {
        return requiredHit;
    }

    public int getMinimumExpertise() {
        return requiredExpertise;
    }


    public int getMaximumHit() {
        return requiredHit != 0 && maxExceed != 0 ? requiredHit + maxExceed : Integer.MAX_VALUE;
    }

    public int getMaximumExpertise() {
        return requiredExpertise != 0 && maxExceed != 0 ? requiredExpertise + maxExceed : Integer.MAX_VALUE;
    }
}
