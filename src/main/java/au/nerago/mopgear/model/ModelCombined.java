package au.nerago.mopgear.model;

import au.nerago.mopgear.ServiceEntry;
import au.nerago.mopgear.domain.*;

import java.nio.file.Path;
import java.util.stream.Stream;

public record ModelCombined(StatRatings statRatings, StatRequirements statRequirements, ReforgeRules reforgeRules,
                            DefaultEnchants enchants, SetBonus setBonus, SpecType spec) {

    public long calcRating(FullItemSet set) {
        long value = statRatings.calcRating(set.totalForRating());
        value = value * setBonus.calc(set) / SetBonus.DENOMIATOR;
        return value;
    }

    public long calcRating(SolvableItemSet set) {
        long value = statRatings.calcRating(set.totalForRating());
        value = value * setBonus.calc(set) / SetBonus.DENOMIATOR;
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

    public Stream<SolvableItemSet> filterSets(Stream<SolvableItemSet> stream, boolean isFinal) {
        Stream<SolvableItemSet> filtered = statRequirements.filterSets(stream);
        if (isFinal) {
            filtered = filtered.filter(SolvableItemSet::validate);
        }
        return filtered;
    }

    public Stream<SolvableItemSet> filterSetsMax(Stream<SolvableItemSet> stream) {
        return statRequirements.filterSetsMax(stream);
    }

    public ModelCombined withNoRequirements() {
        return new ModelCombined(statRatings, new StatRequirementsNull(), reforgeRules, enchants, setBonus, spec);
    }

    public StatBlock gemChoice(SocketType socket) {
        StatBlock choice = statRatings.gemChoice(socket);
        if (choice == null && socket == SocketType.Meta)
            return StatBlock.empty;
        else if (choice == null && socket == SocketType.Engineer)
            return new StatBlock(0, 0, 0, 0, 0, 600, 0, 0, 0, 0);
        else if (choice == null && socket == SocketType.Sha)
            return new StatBlock(500, 0, 0, 0, 0, 0, 0, 0, 0, 0);
//            return StatBlock.empty;
        else if (choice == null)
            throw new RuntimeException("no gem choice for " + socket);
        return choice;
    }

    public StatBlock gemChoiceBestAlternate() {
        StatType stat = statRatings.bestNonHit();
        return StatBlock.of(stat, 320);
    }

    public StatBlock standardEnchant(SlotItem slot) {
        return enchants.standardEnchant(slot);
    }

    public static ModelCombined load(ServiceEntry.ServiceModel modelParam) {
        StatRatingsWeights rating;
        if (modelParam.weight().size() == 1) {
            ServiceEntry.ServiceWeightStats a = modelParam.weight().getFirst();
            rating = new StatRatingsWeights(Path.of(a.file()));
            rating = StatRatingsWeights.mix(rating, a.scale(), null, 0, null);
        } else if (modelParam.weight().size() == 2) {
            ServiceEntry.ServiceWeightStats a = modelParam.weight().getFirst();
            StatRatingsWeights ratingA = new StatRatingsWeights(Path.of(a.file()));

            ServiceEntry.ServiceWeightStats b = modelParam.weight().get(1);
            StatRatingsWeights ratingB = new StatRatingsWeights(Path.of(b.file()));

            rating = StatRatingsWeights.mix(ratingA, a.scale(), ratingB, b.scale(), null);
        } else {
            throw new IllegalArgumentException("expected one or two weights only");
        }

        SpecType spec = modelParam.spec();
        StatRequirements statRequirements = StatRequirementsOriginal.load(modelParam.required());
        DefaultEnchants enchants = new DefaultEnchants(modelParam.defaultEnchants(), modelParam.blacksmith());
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(rating, statRequirements, ReforgeRules.casterPure(), enchants, setBonus, spec);
    }
}
