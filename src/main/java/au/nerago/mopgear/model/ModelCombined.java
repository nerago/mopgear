package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.ServiceEntry;
import au.nerago.mopgear.io.DataLocation;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.stream.Stream;

import static au.nerago.mopgear.domain.StatType.*;

@SuppressWarnings("unused")
public record ModelCombined(StatRatings statRatings, StatRequirements statRequirements, ReforgeRules reforgeRules,
                            DefaultEnchants enchants, SetBonus setBonus) {

    public long calcRating(ItemSet set) {
        long value = statRatings.calcRating(set.getTotals());
        value = value * setBonus.calc(set.items) / SetBonus.DENOMIATOR;
        return value;
    }

    public long calcRating(ItemData it) {
        return statRatings.calcRating(it.stat, it.statFixed);
    }

    public Stream<ItemSet> filterSets(Stream<ItemSet> stream, boolean isFinal) {
        Stream<ItemSet> filtered = statRequirements.filterSets(stream);
        if (isFinal) {
            filtered = filtered.filter(set -> set.items.validateNoDuplicates());
        }
        return filtered;
    }

    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return statRequirements.filterSetsMax(stream);
    }

    public ModelCombined withNoRequirements() {
        return new ModelCombined(statRatings, StatRequirements.zero(), reforgeRules, enchants, setBonus);
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

    public StatBlock standardEnchant(SlotItem slot) {
        return enchants.standardEnchant(slot);
    }

    private static EnumMap<SocketType, StatBlock> protGems() {
        EnumMap<SocketType, StatBlock> gems = new EnumMap<>(SocketType.class);
        gems.put(SocketType.Red, StatBlock.of(Haste, 160, Expertise, 160));
        gems.put(SocketType.Blue, StatBlock.of(Haste, 160, Hit, 160));
        gems.put(SocketType.Yellow, StatBlock.of(Haste, 320));
        gems.put(SocketType.General, StatBlock.of(Haste, 320));
        gems.put(SocketType.Meta, StatBlock.of(Primary, 216));
        return gems;
    }

    public static ModelCombined defenceProtModel() {
        StatRatingsWeights statMitigation = new StatRatingsWeights(DataLocation.weightProtMitigationFile, false, true, false);
        StatRatingsWeights statDps = new StatRatingsWeights(DataLocation.weightProtDpsFile, false, true, false);
        EnumMap<SocketType, StatBlock> standardGems = protGems();
        StatRatings statMix = StatRatingsWeights.mix(statMitigation, 28, statDps, 1, standardGems);
//        StatRequirements statRequirements = StatRequirements.protFullExpertise();
        StatRequirements statRequirements = StatRequirements.protFlexibleParry();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProt, true);
        ReforgeRules reforge = ReforgeRules.prot();
        SetBonus setBonus = new SetBonus().activateWhiteTigerPlate();
        return new ModelCombined(statMix, statRequirements, reforge, enchants, setBonus);
    }

    public static ModelCombined damageProtModel() {
        StatRatingsWeights statMitigation = new StatRatingsWeights(DataLocation.weightProtMitigationFile, false, true, false);
        StatRatingsWeights statDps = new StatRatingsWeights(DataLocation.weightProtDpsFile, false, true, false);
//        StatRatings statMix = StatRatingsWeightsMix.mix(statMitigation, 3, statDps, 17, protGems()); // 90% dps
        EnumMap<SocketType, StatBlock> standardGems = protGems();
        StatRatings statMix = StatRatingsWeights.mix(statMitigation, 15, statDps, 10, standardGems); // 50% dps
//        StatRatings statMix = new StatRatingsPriority(new StatType[] {Haste, Mastery, Dodge, Crit});
//        StatRatings statMix = new StatRatingsPriority(new StatType[] {Haste, Crit, Mastery, Dodge});
        StatRequirements statRequirements = StatRequirements.protFlexibleParry();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProt, true);
        ReforgeRules reforge = ReforgeRules.prot();
        SetBonus setBonus = new SetBonus().activateWhiteTigerBattlegearOnly4pc();
        return new ModelCombined(statMix, statRequirements, reforge, enchants, setBonus);
    }

    public static ModelCombined standardRetModel() {
        StatRatingsWeights statRatings = new StatRatingsWeights(DataLocation.weightRetFile);
//        StatRatings statRatings = StatRatingsWeights.hardCodeRetWeight();
        statRatings = StatRatingsWeights.mix(statRatings, 22, null, 0, null);
        StatRequirements statRequirements = StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet, true);
        SetBonus setBonus = new SetBonus().activateWhiteTigerBattlegear();
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.ret(), enchants, setBonus);
    }

    public static ModelCombined extendedRetModel(boolean wideHitRange, boolean extraReforge) {
        StatRatingsWeights statRatings = new StatRatingsWeights(DataLocation.weightRetFile);
//        StatRatings statRatings = StatRatingsWeights.hardCodeRetWeight();
        statRatings = StatRatingsWeights.mix(statRatings, 18, null, 0, null);
        StatRequirements statRequirements = wideHitRange ? StatRequirements.retWideCapRange() : StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet, true);
        ReforgeRules reforge = extraReforge ? ReforgeRules.retExtended() : ReforgeRules.ret();
        SetBonus setBonus = new SetBonus().activateWhiteTigerBattlegear();
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus);
    }

    public static ModelCombined priorityRetModel() {
        StatRatings statRatings = new StatRatingsPriority(new StatType[]{Primary, Haste, Mastery, Crit});
        StatRequirements statRequirements = StatRequirements.retWideCapRange();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet, true);
        ReforgeRules reforge = ReforgeRules.retExtended();
        SetBonus setBonus = new SetBonus().activateWhiteTigerBattlegear();
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus);
    }

    public static ModelCombined standardBoomModel() {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightBoomFile);
        StatRequirements statRequirements = StatRequirements.druidBalance();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false); // TODO check same
        SetBonus setBonus = new SetBonus().activateRegaliaEternalBlossom();
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.boom(), enchants, setBonus);
    }

    public static ModelCombined standardTreeModel() {
        StatRatings statRatings = new StatRatingsPriorityBreaks(Haste, 3043,
                new StatType[][]{
                        new StatType[]{Spirit, Mastery},
                        new StatType[]{Crit}
                }
        );
        StatRequirements statRequirements = StatRequirements.zero();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        SetBonus setBonus = new SetBonus().activateVestmentsEternalBlossom();
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.boom(), enchants, setBonus);
    }

    public static ModelCombined standardBearModel() {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightBearFile);
        StatRequirements statRequirements = StatRequirements.druidBear();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet, false); // TODO check same
        SetBonus setBonus = new SetBonus();
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.bear(), enchants, setBonus);
    }

    public static ModelCombined standardWarlockModel() {
        StatRatings statRatings = new StatRatingsWeights(DataLocation.weightWarlockFile);
        StatRequirements statRequirements = StatRequirements.warlock();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        SetBonus setBonus = new SetBonus();
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.warlock(), enchants, setBonus);
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

        StatRequirements statRequirements = StatRequirements.load(modelParam.required());
        DefaultEnchants enchants = new DefaultEnchants(modelParam.defaultEnchants(), modelParam.blacksmith());
        SetBonus setBonus = new SetBonus();
        return new ModelCombined(rating, statRequirements, ReforgeRules.warlock(), enchants, setBonus);
    }

    public static ModelCombined nullMixedModel() {
        return new ModelCombined(null, StatRequirements.zero(), ReforgeRules.common(), null, new SetBonus());
    }
}
