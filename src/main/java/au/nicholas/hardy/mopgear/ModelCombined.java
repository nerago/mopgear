package au.nicholas.hardy.mopgear;

import java.io.IOException;
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

    public static ModelCombined standardProtModel() throws IOException {
        Integer gem = 76633;
        StatRatings statMitigation = new StatRatingsWeights(DataLocation.weightProtMitigationFile, false, gem);
        StatRatings statDps = new StatRatingsWeights(DataLocation.weightProtDpsFile, false, gem);
        StatRatings statMix = new StatRatingsWeightsMix(statMitigation, 9, statDps, 13, statDps.standardGem());
        StatRequirements statRequirements = StatRequirements.prot();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProt);
        return new ModelCombined(statMix, statRequirements, ReforgeRules.prot(), enchants);
    }

    public static ModelCombined standardRetModel() throws IOException {
//        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightRetFile, false, gem);
        StatRatings statRatings = StatRatingsWeights.hardCodeRetWeight();
        statRatings = new StatRatingsWeightsMix(statRatings, 22, null, 0, statRatings.standardGem());
        StatRequirements statRequirements = StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.ret(), enchants);
    }

    public static ModelCombined extendedRetModel(boolean wideHitRange, boolean extraReforge) throws IOException {
//        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightRetFile, false, gem);
        StatRatings statRatings = StatRatingsWeights.hardCodeRetWeight();
        statRatings = new StatRatingsWeightsMix(statRatings, 22, null, 0, statRatings.standardGem());
        StatRequirements statRequirements = wideHitRange ? StatRequirements.retWideCapRange() : StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        ReforgeRules reforge = extraReforge ? ReforgeRules.retExtended() : ReforgeRules.ret();
        return new ModelCombined(statRatings, statRequirements, reforge, enchants);
    }

    public static ModelCombined priorityRetModel() throws IOException {
        StatRatings statRatings = new StatRatingsPriority(new StatType[] {StatType.Primary, StatType.Haste, StatType.Mastery, StatType.Crit});
        StatRequirements statRequirements = StatRequirements.retWideCapRange();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        ReforgeRules reforge = ReforgeRules.retExtended();
        return new ModelCombined(statRatings, statRequirements, reforge, enchants);
    }

    public static ModelCombined standardBoomModel() throws IOException {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightBoomFile, false, null);
        StatRequirements statRequirements = StatRequirements.boom();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.boom(), enchants);
    }

    public static ModelCombined nullMixedModel() {
        return new ModelCombined(null, StatRequirements.zero(), ReforgeRules.common(), null);
    }
}
