package au.nerago.mopgear.model;

import au.nerago.mopgear.SolverCapPhased;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.domain.StatBlock;

import java.util.stream.Stream;

public interface StatRequirements {
    int effectiveHit(StatBlock totals);

    int effectiveHit(ItemData item);

    int effectiveExpertise(ItemData item);

    boolean filter(ItemSet set);

    Stream<ItemSet> filterSets(Stream<ItemSet> setStream);

    Stream<ItemSet> filterSetsMax(Stream<ItemSet> setStream);

    Stream<SolverCapPhased.SkinnyItemSet> filterSetsSkinny(Stream<SolverCapPhased.SkinnyItemSet> setStream);

    Stream<SolverCapPhased.SkinnyItemSet> filterSetsMaxSkinny(Stream<SolverCapPhased.SkinnyItemSet> setStream);

    int getMinimumHit();

    int getMinimumExpertise();

    int getMaximumHit();

    int getMaximumExpertise();
}
