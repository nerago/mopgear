package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.SocketType;
import au.nerago.mopgear.domain.SpecType;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.model.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.EnumMap;

import static au.nerago.mopgear.domain.StatType.*;
import static au.nerago.mopgear.domain.StatType.Crit;
import static au.nerago.mopgear.domain.WowClass.Mage;

public class StandardModels {
    public static ModelCombined modelFor(SpecType spec) {
        switch (spec) {
            case PaladinProtMitigation -> {
                return pallyProtMitigationModel();
            }
            case PaladinProtDps -> {
                return pallyProtDpsModel();
            }
            case PaladinRet -> {
                return pallyRetModel();
            }
            case DruidBear, WarriorProt, MonkBrewmaster, DeathKnightBlood -> {
                return standardTankModel(spec);
            }
            case Rogue, WarriorArms, Hunter -> {
                return standardMeleeModel(spec);
            }
            case MageFrost, Warlock -> {
                return standardCasterModel(spec);
            }
            case DruidBoom, PriestShadow, ShamanElemental -> {
                return standardHybridCasterModel(spec);
            }
            case DruidTree -> {
                return druidTreeModel();
            }
            case PaladinHoly -> {
                return paladinHolyModel();
            }
            case ShamanRestoration -> {
                return shamanRestoration();
            }
            case PriestHoly -> {
                return priestHoly();
            }
            case MonkMistweaver -> {
                return monkMistweaver();
            }
            default -> throw new IllegalArgumentException("no model for " + spec);
        }
    }

    private static final boolean useHasteMinimums = false;
    private static final StatBlock hasteGem = StatBlock.of(Haste, 320);

    public static ModelCombined pallyProtMitigationModel() {
        StatRatingsWeights statMitigation = new StatRatingsWeights(specToWeightFile(SpecType.PaladinProtMitigation), false, true, false);
        StatRatingsWeights statDps = new StatRatingsWeights(specToWeightFile(SpecType.PaladinProtDps), false, true, false);
        EnumMap<SocketType, StatBlock> standardGems = protGems();
        StatRatings statMix = StatRatingsWeights.mix(statMitigation, 30, statDps, 35, standardGems);

        StatRequirements combinedRequire;
        if (useHasteMinimums) {
            StatRequirements hitRequire = StatRequirementsHitExpertise.protFlexibleParry();
            StatRequirements hasteRequire = new StatRequirementsGenericOne(Haste, 7000);
            combinedRequire = new StatRequirementsCombined(hitRequire, hasteRequire);
        } else {
            combinedRequire = StatRequirementsHitExpertise.protFlexibleParry();
        }

        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProtMitigation, true);
        ReforgeRules reforge = ReforgeRules.tank();
        SetBonus setBonus = SetBonus.named("White Tiger Plate", "Plate of the Lightning Emperor");
        return new ModelCombined(statMix, combinedRequire, reforge, enchants, setBonus, SpecType.PaladinProtMitigation, hasteGem);
    }

    public static ModelCombined pallyProtDpsModel() {
        StatRatingsWeights statMitigation = new StatRatingsWeights(specToWeightFile(SpecType.PaladinProtDps), false, true, false);
        StatRatingsWeights statDps = new StatRatingsWeights(specToWeightFile(SpecType.PaladinProtDps), false, true, false);
        EnumMap<SocketType, StatBlock> standardGems = protGems();
        StatRatings statMix = StatRatingsWeights.mix(statMitigation, 5, statDps, 200, standardGems);

        StatRequirements combinedRequire;
        if (useHasteMinimums) {
            StatRequirements hitRequire = StatRequirementsHitExpertise.protFlexibleParry();
            StatRequirements hasteRequire = new StatRequirementsGenericOne(Haste, 8000);
            combinedRequire = new StatRequirementsCombined(hitRequire, hasteRequire);
        } else {
            combinedRequire = StatRequirementsHitExpertise.protFlexibleParry();
        }

        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProtDps, true);
        ReforgeRules reforge = ReforgeRules.tank();
        SetBonus setBonus = SetBonus.activateWhiteTigerBattlegearOnly4pc();
//        SetBonus setBonus = SetBonus.activateWhiteTigerBattlegearOnly4pcPlusThunderTank();
//        SetBonus setBonus = SetBonus.activateWhiteTigerBattlegearOnly4pcPlusAll();
//        SetBonus setBonus = SetBonus.empty();
        return new ModelCombined(statMix, combinedRequire, reforge, enchants, setBonus, SpecType.PaladinProtDps, hasteGem);
    }

    public static ModelCombined pallyRetModel() {
//        StatRatings statRatings = new StatRatingsPriority(new StatType[] {Primary, Haste, Crit, Mastery});
        StatRatingsWeights statRatings = new StatRatingsWeights(specToWeightFile(SpecType.PaladinRet));

        StatRequirements combinedRequire;
        if (useHasteMinimums) {
            StatRequirements hitRequire = StatRequirementsHitExpertise.retWideCapRange();
            StatRequirements hasteRequire = new StatRequirementsGenericOne(Haste, 7000);
            combinedRequire = new StatRequirementsCombined(hitRequire, hasteRequire);
        } else {
            combinedRequire = StatRequirementsHitExpertise.retWideCapRange();
        }

        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet, true);
        ReforgeRules reforge = ReforgeRules.melee();
        SetBonus setBonus = SetBonus.named("White Tiger Battlegear", "Battlegear of the Lightning Emperor");
        return new ModelCombined(statRatings, combinedRequire, reforge, enchants, setBonus, SpecType.PaladinRet, hasteGem);
    }

    private static ModelCombined standardTankModel(SpecType spec) {
        StatRatings statRatings = new StatRatingsWeights(specToWeightFile(spec));
        StatRequirements statRequirements = StatRequirementsHitExpertise.protFlexibleParry();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProtMitigation, false);
        ReforgeRules reforge = ReforgeRules.tank();
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus, spec, null);
    }

    private static ModelCombined standardMeleeModel(SpecType spec) {
        StatRatingsWeights statRatings = new StatRatingsWeights(specToWeightFile(spec));
        StatRequirements statRequirements = StatRequirementsHitExpertise.retWideCapRange();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet, true);
        ReforgeRules reforge = ReforgeRules.melee();
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus, spec, null);
    }

    private static ModelCombined standardHybridCasterModel(SpecType spec) {
        StatRatings statRatings = new StatRatingsWeights(specToWeightFile(spec));
        StatRequirements statRequirements = new StatRequirementsHitCombined(
                StatRequirements.TARGET_RATING_CAST, StatRequirements.DEFAULT_CAP_ALLOW_EXCEED);
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        ReforgeRules reforge = ReforgeRules.casterHybrid();
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus, spec, null);
    }

    private static ModelCombined standardCasterModel(SpecType spec) {
        StatRatings statRatings = new StatRatingsWeights(specToWeightFile(spec));
        StatRequirements statRequirements = new StatRequirementsHitOnly(
                StatRequirements.TARGET_RATING_CAST, StatRequirements.DEFAULT_CAP_ALLOW_EXCEED);
        DefaultEnchants enchants = new DefaultEnchants(SpecType.Warlock, false);
        ReforgeRules reforge = ReforgeRules.casterPure();
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus, spec, null);
    }

    private static ModelCombined druidTreeModel() {
        StatRatings statRatings = new StatRatingsPriorityBreaks(
                Haste, 3043,
                new StatType[][]{
                        new StatType[]{Primary},
                        new StatType[]{Spirit, Mastery},
                        new StatType[]{Crit}
                }
        );
        StatRequirements statRequirements = new StatRequirementsNull();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        SetBonus setBonus = SetBonus.forSpec(SpecType.DruidTree);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.DruidTree, null);
    }

    private static ModelCombined paladinHolyModel() {
        StatRatings statRatings = new StatRatingsPriority(
                new StatType[]{Primary, Spirit, Mastery, Haste, Crit}
        );
        StatRequirements statRequirements = new StatRequirementsNull();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        SetBonus setBonus = SetBonus.forSpec(SpecType.PaladinHoly);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.PaladinHoly, null);
    }

    private static ModelCombined shamanRestoration() {
        StatRatings statRatings = new StatRatingsPriorityBreaksMultiple(
                Haste,
                new int[]{1345, 2017, 3379, 8085, 8785, 10118, 14170, 14846, 18207, 19557, 21596, 26308},
                new StatType[]{Primary, Spirit, Crit, Mastery}
        );
        StatRequirements statRequirements = new StatRequirementsNull();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        SetBonus setBonus = SetBonus.forSpec(SpecType.ShamanRestoration);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.ShamanRestoration, null);
    }

    private static ModelCombined priestHoly() {
        StatRatings statRatings = new StatRatingsPriority(
                new StatType[]{Primary, Spirit, Crit, Mastery, Haste}
        );
        StatRequirements statRequirements = new StatRequirementsNull();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        SetBonus setBonus = SetBonus.forSpec(SpecType.PriestHoly);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.PriestHoly, null);
    }

    private static ModelCombined monkMistweaver() {
        StatRatings statRatings = new StatRatingsPriorityBreaksMultiple(
                Haste,
                new int[]{154, 887, 1578, 2348, 3145, 4719, 5376, 6141, 7062, 7864, 9158, 11033},
                new StatType[]{Primary, Crit, Mastery}
        );
        // TODO Haste remaining should value over Mastery
        // https://www.icy-veins.com/mists-of-pandaria-classic/mistweaver-monk-pve-stat-priority
        StatRequirements statRequirements = new StatRequirementsGenericOne(Spirit, 5100);
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        SetBonus setBonus = SetBonus.forSpec(SpecType.MonkMistweaver);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.MonkMistweaver, null);
    }

    private static EnumMap<SocketType, StatBlock> protGems() {
        EnumMap<SocketType, StatBlock> gems = new EnumMap<>(SocketType.class);
        gems.put(SocketType.Red, StatBlock.of(Haste, 160, Expertise, 160));
        gems.put(SocketType.Blue, StatBlock.of(Haste, 160, Stam, 120));
        gems.put(SocketType.Yellow, StatBlock.of(Haste, 320));
        gems.put(SocketType.General, StatBlock.of(Haste, 320));
        gems.put(SocketType.Meta, StatBlock.of(Primary, 216));
        return gems;
    }

    public static Path specToWeightFile(SpecType spec) {
        try {
            URL url = StandardModels.class.getClassLoader().getResource("weight/" + spec + ".txt");
            if (url == null) {
                throw new IllegalArgumentException("no weight file for " + spec);
            }
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
