package au.nerago.mopgear.model;

import au.nerago.mopgear.SolverCapPhased;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.domain.StatBlock;

import java.util.stream.Stream;

public interface StatRequirements {
    boolean filter(ItemSet set);

    Stream<ItemSet> filterSets(Stream<ItemSet> setStream);

    Stream<ItemSet> filterSetsMax(Stream<ItemSet> setStream);

    interface StatRequirementsHitExpertise extends StatRequirements {
        int effectiveHit(StatBlock totals);

        int getMinimumHit();

        int getMinimumExpertise();

        int getMaximumHit();

        int getMaximumExpertise();
    }

    interface StatRequirementsSkinnyCompat {
        int effectiveHit(ItemData item);

        int effectiveExpertise(ItemData item);

        Stream<SolverCapPhased.SkinnyItemSet> filterSetsSkinny(Stream<SolverCapPhased.SkinnyItemSet> setStream);

        Stream<SolverCapPhased.SkinnyItemSet> filterSetsMaxSkinny(Stream<SolverCapPhased.SkinnyItemSet> setStream);
    }
}
