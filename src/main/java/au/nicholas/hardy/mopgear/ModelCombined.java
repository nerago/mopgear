package au.nicholas.hardy.mopgear;

import java.util.stream.Stream;

public record ModelCombined(StatRatings statRatings, StatRequirements statRequirements, ReforgeRules reforgeRules, DefaultEnchants enchants) {
    public long calcRating(ItemSet set) {
        return calcRating(set.getTotals());
    }

    public long calcRating(StatBlock totals) {
        return statRatings.calcRating(totals);
    }

    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        return statRequirements.filterSets(stream);
    }

    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return statRequirements.filterSetsMax(stream);
    }

    public ModelCombined withNoRequirements() {
        return new ModelCombined(statRatings, StatRequirements.zero(), reforgeRules, enchants);
    }

    public StatBlock standardGem() {
        return statRatings.standardGem();
    }

    public StatBlock standardEnchant(SlotItem slot) {
        return enchants.standardEnchant(slot);
    }
}
