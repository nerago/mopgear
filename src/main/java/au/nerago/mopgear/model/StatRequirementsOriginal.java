package au.nerago.mopgear.model;

import au.nerago.mopgear.ServiceEntry;
import au.nerago.mopgear.domain.*;

import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class StatRequirementsOriginal implements StatRequirements.StatRequirementsWithHitExpertise {
    public StatRequirementsOriginal(int hitMin, int hitMax, int expertiseMin, int expertiseMax, boolean combineHitLike) {
        this.hitMin = hitMin;
        this.hitMax = hitMax;
        this.expertiseMin = expertiseMin;
        this.expertiseMax = expertiseMax;
        this.combineHitLike = combineHitLike;
        if (combineHitLike && expertiseMin != 0)
            throw new IllegalArgumentException("expect either two requirement or combined one, not both");
    }

    public StatRequirementsOriginal(int hitMin, int hitMax, boolean combineHitLike) {
        this.hitMin = hitMin;
        this.hitMax = hitMax;
        this.expertiseMin = 0;
        this.expertiseMax = Integer.MAX_VALUE;
        this.combineHitLike = combineHitLike;
    }

    public static StatRequirements load(ServiceEntry.ServiceRequiredStats param) {
        return new StatRequirementsOriginal(param.hit(),
                param.hit() != 0 && param.allowedExceed() != 0 ? param.hit() + param.allowedExceed() : Integer.MAX_VALUE,
                param.expertise(),
                param.expertise() != 0 && param.allowedExceed() != 0 ? param.expertise() + param.allowedExceed() : Integer.MAX_VALUE,
                param.combinedHit());
    }

    private final int hitMin, hitMax;
    private final int expertiseMin, expertiseMax;
    private final boolean combineHitLike;

    @Override
    public int effectiveHit(StatBlock totals) {
        return hitMin == 0 ? 0
                : combineHitLike ? totals.hit() + totals.expertise() + totals.spirit()
                : totals.hit();
    }

    private int effectiveHit(SolvableItem item) {
        if (hitMin == 0) {
            return 0;
        } else if (combineHitLike) {
            StatBlock cap = item.totalCap();
            return cap.hit() + cap.expertise() + cap.spirit();
        } else {
            StatBlock cap = item.totalCap();
            return cap.hit();
        }
    }

    private int effectiveExpertise(SolvableItem item) {
        if (combineHitLike || !hasExpertiseRange()) {
            return 0;
        } else {
            return item.totalCap().get(StatType.Expertise);
        }
    }

    @Override
    public boolean skinnyMatch(SkinnyItem skinny, SolvableItem item) {
        int hit = effectiveHit(item);
        int exp = effectiveExpertise(item);
        return skinny.one() == hit && skinny.two() == exp;
    }

    @Override
    public SkinnyItem toSkinny(SlotEquip slot, SolvableItem item) {
        int hit = effectiveHit(item);
        int expertise = effectiveExpertise(item);
        return new SkinnyItem(slot, hit, expertise, 0);
    }

    @Override
    public boolean skinnyRecommended() {
        return true;
    }

    @Override
    public ToLongFunction<SkinnyItemSet> skinnyRatingMinimiseFunc() {
        return skinny -> skinny.totalOne() + skinny.totalTwo();
    }

    @Override
    public boolean filterOneSet(SolvableItemSet set) {
        return inRange(set.totalForCaps());
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

    @Override
    public Stream<SolvableItemSet> filterSets(Stream<SolvableItemSet> setStream) {
        final int minHit = hitMin, maxHit = hitMax;
        final int minExp = expertiseMin, maxExp = expertiseMax;

        if (minExp != 0 && maxExp != Integer.MAX_VALUE && minHit != 0 && maxHit != Integer.MAX_VALUE) {
            return setStream.filter(set -> {
                StatBlock stats = set.totalForCaps();
                int hit = stats.hit(), expertise = stats.expertise();
                return hit >= minHit && hit <= maxHit && expertise >= minExp && expertise <= maxExp;
            });
        }

        if (combineHitLike) {
            if (minHit != 0 && maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.totalForCaps();
                    int hit = stats.hit() + stats.expertise() + stats.spirit();
                    return hit >= minHit && hit <= maxHit;
                });
            } else if (minHit != 0) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.totalForCaps();
                    int hit = stats.hit() + stats.expertise() + stats.spirit();
                    return hit >= minHit;
                });
            } else if (maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.totalForCaps();
                    int hit = stats.hit() + stats.expertise() + stats.spirit();
                    return hit <= maxHit;
                });
            }
        } else {
            if (minHit != 0 && maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.totalForCaps();
                    int hit = stats.hit();
                    return hit >= minHit && hit <= maxHit;
                });
            } else if (minHit != 0) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.totalForCaps();
                    int hit = stats.hit();
                    return hit >= minHit;
                });
            } else if (maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.totalForCaps();
                    int hit = stats.hit();
                    return hit <= maxHit;
                });
            }
        }

        if (minExp != 0 && maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> {
                StatBlock stats = set.totalForCaps();
                int expertise = stats.expertise();
                return expertise >= minExp && expertise <= maxExp;
            });
        } else if (minExp != 0) {
            setStream = setStream.filter(set -> {
                StatBlock stats = set.totalForCaps();
                int expertise = stats.expertise();
                return expertise >= minExp;
            });
        } else if (maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> {
                StatBlock stats = set.totalForCaps();
                int expertise = stats.expertise();
                return expertise <= maxExp;
            });
        }

        return setStream;
    }

    @Override
    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> setStream) {
        final int maxHit = hitMax;
        final int maxExp = expertiseMax;

        if (combineHitLike) {
            if (maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.totalForCaps();
                    int hit = stats.hit() + stats.expertise() + stats.spirit();
                    return hit <= maxHit;
                });
            }
        } else {
            if (maxHit != Integer.MAX_VALUE) {
                setStream = setStream.filter(set -> {
                    StatBlock stats = set.totalForCaps();
                    int hit = stats.hit();
                    return hit <= maxHit;
                });
            }
        }

        if (maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> {
                StatBlock stats = set.totalForCaps();
                int expertise = stats.expertise();
                return expertise <= maxExp;
            });
        }

        return setStream;
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsSkinny(Stream<SkinnyItemSet> setStream) {
        final int minHit = hitMin, maxHit = hitMax;
        final int minExp = expertiseMin, maxExp = expertiseMax;

        if (minExp != 0 && maxExp != Integer.MAX_VALUE && minHit != 0 && maxHit != Integer.MAX_VALUE) {
            return setStream.filter(set -> set.totalOne() >= minHit && set.totalOne() <= maxHit
                    && set.totalTwo() >= minExp && set.totalTwo() <= maxExp);
        }

        if (minHit != 0 && maxHit != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> set.totalOne() >= minHit && set.totalOne() <= maxHit);
        } else if (minHit != 0) {
            setStream = setStream.filter(set -> set.totalOne() >= minHit);
        } else if (maxHit != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> set.totalOne() <= maxHit);
        }

        if (minExp != 0 && maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> set.totalTwo() >= minExp && set.totalTwo() <= maxExp);
        } else if (minExp != 0) {
            setStream = setStream.filter(set -> set.totalTwo() >= minExp);
        } else if (maxExp != Integer.MAX_VALUE) {
            setStream = setStream.filter(set -> set.totalTwo() <= maxExp);
        }

        return setStream;
    }

    @Override
    public Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> setStream) {
        final int maxHit = hitMax;
        final int maxExp = expertiseMax;

        if (maxExp != Integer.MAX_VALUE && maxHit != Integer.MAX_VALUE) {
            return setStream.filter(set -> set.totalOne() <= maxHit && set.totalTwo() <= maxExp);
        } else if (maxHit != Integer.MAX_VALUE) {
            return setStream.filter(set -> set.totalOne() <= maxHit);
        } else if (maxExp != Integer.MAX_VALUE) {
            return setStream.filter(set -> set.totalTwo() <= maxExp);
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
