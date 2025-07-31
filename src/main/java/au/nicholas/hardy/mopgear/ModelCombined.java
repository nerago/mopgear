package au.nicholas.hardy.mopgear;

import java.util.stream.Stream;

public class ModelCombined implements StatRatings {
    private final StatRatings statRatings;
    private final StatRequirements statRequirements;
    private final ReforgeRules reforgeRules;

    public ModelCombined(StatRatings statRatings, StatRequirements statRequirements, ReforgeRules reforgeRules) {
        this.statRatings = statRatings;
        this.statRequirements = statRequirements;
        this.reforgeRules = reforgeRules;
    }

    @Override
    public long calcRating(StatBlock totals) {
        return statRatings.calcRating(totals);
    }

    public Stream<ItemSet> filterSets(Stream<ItemSet> sets) {
        return statRequirements.filterSets(sets);
    }

    public StatRatings getStatRatings() {
        return statRatings;
    }

    public StatRequirements getStatRequirements() {
        return statRequirements;
    }

    public ReforgeRules getReforgeRules() {
        return reforgeRules;
    }
}
