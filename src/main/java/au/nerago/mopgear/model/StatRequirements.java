package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;

import java.util.stream.Stream;

public interface StatRequirements {
    boolean filterOneSet(ItemSet set);

    Stream<ItemSet> filterSets(Stream<ItemSet> stream);

    Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream);

    Stream<SkinnyItemSet> filterSetsSkinny(Stream<SkinnyItemSet> stream);

    Stream<SkinnyItemSet> filterSetsMaxSkinny(Stream<SkinnyItemSet> stream);

    boolean skinnyMatch(SkinnyItem skinny, ItemData item);

    SkinnyItem toSkinny(SlotEquip slot, ItemData item);

    boolean skinnyRecommended();

    interface StatRequirementsWithHitExpertise extends StatRequirements {
        int effectiveHit(StatBlock totals);

        int getMinimumHit();

        int getMinimumExpertise();

        int getMaximumHit();

        int getMaximumExpertise();
    }

    double RATING_PER_PERCENT = 339.9534;
    int TARGET_RATING_CAST_DUNGEON = (int) Math.ceil(RATING_PER_PERCENT * 12); // 4080
    double TARGET_PERCENT_MELEE = 7.5;
    int TARGET_RATING_MELEE = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_MELEE); // 2550
    double TARGET_PERCENT_TANK = 15;
    int TARGET_RATING_TANK = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_TANK); // 5100
    double TARGET_PERCENT_CAST = 15;
    int TARGET_RATING_CAST = (int) Math.ceil(RATING_PER_PERCENT * TARGET_PERCENT_CAST); // 5100
    int DEFAULT_CAP_ALLOW_EXCEED = 400;
}
