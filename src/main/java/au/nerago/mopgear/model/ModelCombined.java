package au.nerago.mopgear.model;

import au.nerago.mopgear.ServiceEntry;
import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.StreamNeedClose;

import java.nio.file.Path;
import java.util.stream.Stream;

public record ModelCombined(StatRatings statRatings, StatRequirements statRequirements, ReforgeRules reforgeRules,
                            DefaultEnchants enchants, SetBonus setBonus, SpecType spec, GemChoice gemChoice) {

    public long calcRating(FullItemSet set) {
        long value = statRatings.calcRating(set.totalForRating());
        value = setBonus.calcAndMultiply(set, value);
        return value;
    }

    public long calcRating(SolvableItemSet set) {
        long value = statRatings.calcRating(set.totalForRating());
        value = setBonus.calcAndMultiply(set, value);
        return value;
    }

    public long calcRating(SolvableItem it) {
        return statRatings.calcRating(it.totalRated());
    }

    public long calcRating(FullItemData it) {
        return statRatings.calcRating(it.totalRated());
    }

    public boolean filterOneSet(SolvableItemSet set) {
        return statRequirements.filterOneSet(set) && set.validate();
    }

    public StreamNeedClose<SolvableItemSet> filterSets(StreamNeedClose<SolvableItemSet> stream, boolean isFinal) {
        StreamNeedClose<SolvableItemSet> filtered = statRequirements.filterSets(stream);
        if (isFinal) {
            filtered = filtered.filter(SolvableItemSet::validate);
        }
        return filtered;
    }

    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> stream) {
        return statRequirements.filterSetsMax(stream);
    }

    public ModelCombined withNoRequirements() {
        return new ModelCombined(statRatings, new StatRequirementsNull(), reforgeRules, enchants, setBonus, spec, gemChoice);
    }

    public ModelCombined withChangedRequirements(StatRequirements require) {
        return new ModelCombined(statRatings, require, reforgeRules, enchants, setBonus, spec, gemChoice);
    }

    public GemInfo gemChoice(SocketType socket) {
        return gemChoice.gemChoice(socket);
    }

    public GemInfo gemChoiceBestAlternate() {
        return gemChoice.gemChoiceBestAlternate();
    }

    public StatBlock standardEnchant(SlotItem slot) {
        return enchants.standardEnchant(slot);
    }

    public static ModelCombined load(ServiceEntry.ServiceModel modelParam) {
        StatRatingsWeights rating;
        if (modelParam.weight().size() == 1) {
            ServiceEntry.ServiceWeightStats a = modelParam.weight().getFirst();
            rating = new StatRatingsWeights(Path.of(a.file()));
            rating = StatRatingsWeights.multiplied(rating, a.scale());
        } else if (modelParam.weight().size() == 2) {
            ServiceEntry.ServiceWeightStats a = modelParam.weight().getFirst();
            StatRatingsWeights ratingA = new StatRatingsWeights(Path.of(a.file()));

            ServiceEntry.ServiceWeightStats b = modelParam.weight().get(1);
            StatRatingsWeights ratingB = new StatRatingsWeights(Path.of(b.file()));

            rating = StatRatingsWeights.mix(ratingA, a.scale(), ratingB, b.scale());
        } else {
            throw new IllegalArgumentException("expected one or two weights only");
        }

        SpecType spec = modelParam.spec();
        StatRequirements statRequirements = StatRequirementsOriginal.load(modelParam.required());
        DefaultEnchants enchants = new DefaultEnchants(modelParam.defaultEnchants(), modelParam.blacksmith());
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(rating, statRequirements, ReforgeRules.casterPure(), enchants, setBonus, spec, null);
    }
}
