package au.nerago.mopgear.model;

import au.nerago.mopgear.ServiceEntry;
import au.nerago.mopgear.SolverCapPhased;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.domain.StatBlock;

import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class StatRequirementsOriginal implements StatRequirements.StatRequirementsHitExpertise, StatRequirements.StatRequirementsSkinnyCompat {
    public StatRequirementsOriginal(int hitMin, int hitMax, int expertiseMin, int expertiseMax, boolean combineHitLike) {
        this.hitMin = hitMin;
        this.hitMax = hitMax;
        this.expertiseMin = expertiseMin;
        this.expertiseMax = expertiseMax;
        this.combineHitLike = combineHitLike;
        if (combineHitLike && expertiseMin != 0)
            throw new IllegalArgumentException("expect either expertise requirement or combined hit, not both");
    }

    public StatRequirementsOriginal(int hitMin, int hitMax, boolean combineHitLike) {
        this.hitMin = hitMin;
        this.hitMax = hitMax;
        this.expertiseMin = 0;
        this.expertiseMax = Integer.MAX_VALUE;
        this.combineHitLike = combineHitLike;
    }

    public static StatRequirements ret() {
        return new StatRequirementsOriginal(
                TARGET_RATING_MELEE,
                TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                TARGET_RATING_MELEE,
                TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements retWideCapRange() {
        return new StatRequirementsOriginal(
                TARGET_RATING_MELEE,
                TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED * 5,
                TARGET_RATING_MELEE,
                TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED * 5,
                false);
    }

    public static StatRequirements protFullExpertise() {
        return new StatRequirementsOriginal(
                TARGET_RATING_MELEE, TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                TARGET_RATING_TANK, TARGET_RATING_TANK + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements protFlexibleParry() {
        return new StatRequirementsOriginal(
                TARGET_RATING_MELEE, TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                TARGET_RATING_MELEE, TARGET_RATING_TANK,
                false);
    }

    public static StatRequirements druidBalance() {
        return new StatRequirementsOriginal(TARGET_RATING_CAST, TARGET_RATING_CAST + DEFAULT_CAP_ALLOW_EXCEED,
                true);
    }

    public static StatRequirements druidBear() {
        return new StatRequirementsOriginal(
                TARGET_RATING_MELEE, TARGET_RATING_MELEE + DEFAULT_CAP_ALLOW_EXCEED,
                TARGET_RATING_MELEE, TARGET_RATING_TANK + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements warlock() {
        return new StatRequirementsOriginal(TARGET_RATING_CAST_DUNGEON, TARGET_RATING_CAST_DUNGEON + DEFAULT_CAP_ALLOW_EXCEED,
                false);
    }

    public static StatRequirements zero() {
        return new StatRequirementsOriginal(0, Integer.MAX_VALUE, false);
    }

    public static StatRequirements load(ServiceEntry.ServiceRequiredStats param) {
        return new StatRequirementsOriginal(param.hit(),
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

    private static final int DEFAULT_CAP_ALLOW_EXCEED = 400;

    private final int hitMin, hitMax;
    private final int expertiseMin, expertiseMax;
    private final boolean combineHitLike;

    @Override
    public int effectiveHit(StatBlock totals) {
        return hitMin == 0 ? 0
                : combineHitLike ? totals.hit() + totals.expertise() + totals.spirit()
                : totals.hit();
    }

    @Override
    public int effectiveHit(ItemData item) {
        if (hitMin == 0) {
            return 0;
        } else if (combineHitLike) {
            return item.stat.hit() + item.statFixed.hit() +
                   item.stat.expertise() + item.statFixed.expertise() +
                   item.stat.spirit() + item.statFixed.spirit();
        } else {
            return item.stat.hit() + item.statFixed.hit();
        }
    }

    @Override
    public int effectiveExpertise(ItemData item) {
        if (combineHitLike || !hasExpertiseRange()) {
            return 0;
        } else {
            return item.stat.expertise() + item.statFixed.expertise();
        }
    }

    public ToIntFunction<StatBlock> effectiveHitFunc() {
        return combineHitLike
                ? totals -> totals.hit() + totals.expertise() + totals.spirit()
                : totals -> totals.hit();
    }

    @Override
    public boolean filter(ItemSet set) {
        return inRange(set.getTotals()) && set.validate();
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean inRange(StatBlock totals) {
        if (hitMin != 0) {
            int hit = effectiveHit(totals);
            if (hit < hitMin || hit > hitMax)
                return false;
        }
        if (expertiseMin != 0) {
            if (totals.expertise() < expertiseMin || totals.expertise() > expertiseMax)
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
            if (totals.expertise() > expertiseMax)
                return false;
        }
        return true;
    }

    @Override
    public Stream<ItemSet> filterSets(Stream<ItemSet> setStream) {
        final int minHit = hitMin, maxHit = hitMax;
        final int minExp = expertiseMin, maxExp = expertiseMax;

        if (minExp != 0 && maxExp != Integer.MAX_VALUE && minHit != 0 && maxHit != Integer.MAX_VALUE) {
            return setStream.filter(set -> {
                StatBlock stats = set.getTotals();
                int hit = stats.hit(), expertise = stats.expertise();
                return hit >= minHit && hit <= maxHit && expertise >= minExp && expertise <= maxExp;
            });
        }

        if (combineHitLike) {
            if (minHit != 0 && maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.getTotals();
                    int hit = stats.hit() + stats.expertise() + stats.spirit();
                    return hit >= minHit && hit <= maxHit;
                });
            } else if (minHit != 0) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.getTotals();
                    int hit = stats.hit() + stats.expertise() + stats.spirit();
                    return hit >= minHit;
                });
            } else if (maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.getTotals();
                    int hit = stats.hit() + stats.expertise() + stats.spirit();
                    return hit <= maxHit;
                });
            }
        } else {
            if (minHit != 0 && maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.getTotals();
                    int hit = stats.hit();
                    return hit >= minHit && hit <= maxHit;
                });
            } else if (minHit != 0) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.getTotals();
                    int hit = stats.hit();
                    return hit >= minHit;
                });
            } else if (maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.getTotals();
                    int hit = stats.hit();
                    return hit <= maxHit;
                });
            }
        }

        if (minExp != 0 && maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> {
                StatBlock stats = set.getTotals();
                int expertise = stats.expertise();
                return expertise >= minExp && expertise <= maxExp;
            });
        } else if (minExp != 0) {
            setStream = setStream.filter(set -> {
                StatBlock stats = set.getTotals();
                int expertise = stats.expertise();
                return expertise >= minExp;
            });
        } else if (maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> {
                StatBlock stats = set.getTotals();
                int expertise = stats.expertise();
                return expertise <= maxExp;
            });
        }

        return setStream;
    }

    @Override
    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> setStream) {
        final int maxHit = hitMax;
        final int maxExp = expertiseMax;

        if (combineHitLike) {
            if (maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.getTotals();
                    int hit = stats.hit() + stats.expertise() + stats.spirit();
                    return hit <= maxHit;
                });
            }
        } else {
            if (maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.getTotals();
                    int hit = stats.hit();
                    return hit <= maxHit;
                });
            }
        }

        if (maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> {
                StatBlock stats = set.getTotals();
                int expertise = stats.expertise();
                return expertise <= maxExp;
            });
        }

        return setStream;
    }

    @Override
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

    @Override
    public Stream<SolverCapPhased.SkinnyItemSet> filterSetsMaxSkinny(Stream<SolverCapPhased.SkinnyItemSet> setStream) {
        final int maxHit = hitMax;
        final int maxExp = expertiseMax;

        if (maxExp != Integer.MAX_VALUE && maxHit != Integer.MAX_VALUE) {
            return setStream.filter(set -> set.totalHit() <= maxHit && set.totalExpertise() <= maxExp);
        } else if (maxHit != Integer.MAX_VALUE) {
            return setStream.filter(set -> set.totalHit() <= maxHit);
        } else if (maxExp != Integer.MAX_VALUE) {
            return setStream.filter(set -> set.totalExpertise() <= maxExp);
        } else {
            return setStream;
        }
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

    public boolean hasExpertiseRange() {
        return expertiseMin != 0 || expertiseMax != Integer.MAX_VALUE;
    }
}
