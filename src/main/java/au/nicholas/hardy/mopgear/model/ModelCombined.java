package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.io.DataLocation;

import java.io.IOException;
import java.util.stream.Stream;

public record ModelCombined(StatRatings statRatings, StatRequirements statRequirements, ReforgeRules reforgeRules,
                            DefaultEnchants enchants) {

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

    public static ModelCombined standardProtModel() throws IOException {
        StatRatings statMitigation = new StatRatingsWeights(DataLocation.weightProtMitigationFile, false);
        StatRatings statDps = new StatRatingsWeights(DataLocation.weightProtDpsFile, false);
        StatRatings statMix = new StatRatingsWeightsMix(statMitigation, 9, statDps, 13);
        StatRequirements statRequirements = StatRequirements.prot();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProt);
        ReforgeRules reforge = ReforgeRules.prot();
        return new ModelCombined(statMix, statRequirements, reforge, enchants);
    }

    public static ModelCombined standardRetModel() throws IOException {
//        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightRetFile, false, gem);
        StatRatings statRatings = StatRatingsWeights.hardCodeRetWeight();
        statRatings = new StatRatingsWeightsMix(statRatings, 22, null, 0);
        StatRequirements statRequirements = StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.ret(), enchants);
    }

    public static ModelCombined extendedRetModel(boolean wideHitRange, boolean extraReforge) throws IOException {
//        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightRetFile, false, gem);
        StatRatings statRatings = StatRatingsWeights.hardCodeRetWeight();
        statRatings = new StatRatingsWeightsMix(statRatings, 22, null, 0);
        StatRequirements statRequirements = wideHitRange ? StatRequirements.retWideCapRange() : StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        ReforgeRules reforge = extraReforge ? ReforgeRules.retExtended() : ReforgeRules.ret();
        return new ModelCombined(statRatings, statRequirements, reforge, enchants);
    }

    public static ModelCombined priorityRetModel() throws IOException {
        StatRatings statRatings = new StatRatingsPriority(new StatType[]{StatType.Primary, StatType.Haste, StatType.Mastery, StatType.Crit});
        StatRequirements statRequirements = StatRequirements.retWideCapRange();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        ReforgeRules reforge = ReforgeRules.retExtended();
        return new ModelCombined(statRatings, statRequirements, reforge, enchants);
    }

    public static ModelCombined standardBoomModel() throws IOException {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightBoomFile, false);
        StatRequirements statRequirements = StatRequirements.boom();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.boom(), enchants);
    }

    public static ModelCombined nullMixedModel() {
        return new ModelCombined(null, StatRequirements.zero(), ReforgeRules.common(), null);
    }
}
