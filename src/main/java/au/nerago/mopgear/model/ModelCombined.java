package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.ServiceEntry;
import au.nerago.mopgear.io.DataLocation;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.stream.Stream;

public record ModelCombined(StatRatings statRatings, StatRequirements statRequirements, ReforgeRules reforgeRules,
                            DefaultEnchants enchants) {

    public long calcRating(ItemSet set) {
        return statRatings.calcRating(set.getTotals());
    }

    public long calcRating(ItemData it) {
        return statRatings.calcRating(it.stat, it.statFixed);
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

    public StatBlock gemChoice(SocketType socket) {
        StatBlock choice = statRatings.gemChoice(socket);
        if (choice == null && socket == SocketType.Meta)
            return StatBlock.empty;
        if (choice == null && socket == SocketType.Engineer)
            return new StatBlock(0, 0, 0, 0, 0, 600, 0, 0, 0, 0);
        else if (choice == null)
            throw new RuntimeException("no gem choice for " + socket);
        return choice;
    }

    public StatBlock standardEnchant(SlotItem slot) {
        return enchants.standardEnchant(slot);
    }

    private static EnumMap<SocketType, StatBlock> protGems() {
        EnumMap<SocketType, StatBlock> gems = new EnumMap<>(SocketType.class);
        gems.put(SocketType.Red, StatBlock.of(StatType.Haste, 160, StatType.Expertise, 160));
        gems.put(SocketType.Blue, StatBlock.of(StatType.Haste, 160, StatType.Hit, 160));
        gems.put(SocketType.Yellow, StatBlock.of(StatType.Haste, 320));
        gems.put(SocketType.General, StatBlock.of(StatType.Haste, 320));
        gems.put(SocketType.Meta, StatBlock.of(StatType.Primary, 216));
        return gems;
    }

    public static ModelCombined standardProtModel() {
        StatRatings statMitigation = new StatRatingsWeights(DataLocation.weightProtMitigationFile);
        StatRatings statDps = new StatRatingsWeights(DataLocation.weightProtDpsFile);
        StatRatings statMix = new StatRatingsWeightsMix(statMitigation, 9, statDps, 13, protGems());
        StatRequirements statRequirements = StatRequirements.prot();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProt);
        ReforgeRules reforge = ReforgeRules.prot();
        return new ModelCombined(statMix, statRequirements, reforge, enchants);
    }

    public static ModelCombined uncappedProtModel() {
        StatRatings statMitigation = new StatRatingsWeights(DataLocation.weightProtMitigationFile, false, true, false);
        StatRatings statDps = new StatRatingsWeights(DataLocation.weightProtDpsFile, false, true, false);
        StatRatings statMix = new StatRatingsWeightsMix(statMitigation, 9, statDps, 13, protGems());
        StatRequirements statRequirements = StatRequirements.protFlexibleParry();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProt);
        ReforgeRules reforge = ReforgeRules.prot();
        return new ModelCombined(statMix, statRequirements, reforge, enchants);
    }

    public static ModelCombined standardRetModel() {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightRetFile);
//        StatRatings statRatings = StatRatingsWeights.hardCodeRetWeight();
        statRatings = new StatRatingsWeightsMix(statRatings, 22, null, 0);
        StatRequirements statRequirements = StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.ret(), enchants);
    }

    public static ModelCombined extendedRetModel(boolean wideHitRange, boolean extraReforge) {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightRetFile);
//        StatRatings statRatings = StatRatingsWeights.hardCodeRetWeight();
        statRatings = new StatRatingsWeightsMix(statRatings, 18, null, 0);
        StatRequirements statRequirements = wideHitRange ? StatRequirements.retWideCapRange() : StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        ReforgeRules reforge = extraReforge ? ReforgeRules.retExtended() : ReforgeRules.ret();
        return new ModelCombined(statRatings, statRequirements, reforge, enchants);
    }

    public static ModelCombined priorityRetModel() {
        StatRatings statRatings = new StatRatingsPriority(new StatType[]{StatType.Primary, StatType.Haste, StatType.Mastery, StatType.Crit});
        StatRequirements statRequirements = StatRequirements.retWideCapRange();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        ReforgeRules reforge = ReforgeRules.retExtended();
        return new ModelCombined(statRatings, statRequirements, reforge, enchants);
    }

    public static ModelCombined standardBoomModel() {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightBoomFile);
        StatRequirements statRequirements = StatRequirements.druidBalance();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom); // TODO check same
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.boom(), enchants);
    }

    public static ModelCombined standardWarlockModel() {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightWarlockFile);
        StatRequirements statRequirements = StatRequirements.warlock();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.warlock(), enchants);
    }

    public static ModelCombined load(ServiceEntry.ServiceModel modelParam) {
        StatRatings rating;
        if (modelParam.weight().size() == 1) {
            ServiceEntry.ServiceWeightStats a = modelParam.weight().getFirst();
            rating = new StatRatingsWeights(Path.of(a.file()));
            rating = new StatRatingsWeightsMix(rating, a.scale(), null, 0);
        } else if (modelParam.weight().size() == 2) {
            ServiceEntry.ServiceWeightStats a = modelParam.weight().getFirst();
            StatRatingsWeights ratingA = new StatRatingsWeights(Path.of(a.file()));

            ServiceEntry.ServiceWeightStats b = modelParam.weight().get(1);
            StatRatingsWeights ratingB = new StatRatingsWeights(Path.of(b.file()));

            rating = new StatRatingsWeightsMix(ratingA, a.scale(), ratingB, b.scale());
        } else {
            throw new IllegalArgumentException("expected one or two weights only");
        }

        StatRequirements statRequirements = StatRequirements.load(modelParam.required());
        DefaultEnchants enchants = new DefaultEnchants(modelParam.defaultEnchants());
        return new ModelCombined(rating, statRequirements, ReforgeRules.warlock(), enchants);
    }

    public static ModelCombined nullMixedModel() {
        return new ModelCombined(null, StatRequirements.zero(), ReforgeRules.common(), null);
    }
}
