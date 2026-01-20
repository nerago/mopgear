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
        StatRatings statMix = StatRatingsWeights.mix(statMitigation, 121, statDps, 41);

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
        GemChoice gemChoice = GemChoice.protMitigationGems();

//        SetBonus setBonus = SetBonus.named("White Tiger Plate", "Plate of the Lightning Emperor");
//        SetBonus setBonus = SetBonus.named("White Tiger Plate", "White Tiger Battlegear Prot Mitigation", "Plate of the Lightning Emperor Prot Mitigation");
        SetBonus setBonus = SetBonus.named(AllowedMeta.Tank, "White Tiger Battlegear Prot Mitigation", "Plate of the Lightning Emperor Prot Mitigation");

        return new ModelCombined(statMix, combinedRequire, reforge, enchants, setBonus, SpecType.PaladinProtMitigation, gemChoice);
    }

    public static ModelCombined pallyProtDpsModel() {
        StatRatingsWeights statMitigation = new StatRatingsWeights(specToWeightFile(SpecType.PaladinProtDps), false, true, false);
        StatRatingsWeights statDps = new StatRatingsWeights(specToWeightFile(SpecType.PaladinProtDps), false, true, false);
        StatRatings statMix = StatRatingsWeights.mix(statMitigation, 15, statDps, 185);

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
        GemChoice gemChoice = GemChoice.protDpsGems();

        //        SetBonus setBonus = SetBonus.empty();
        SetBonus setBonus = SetBonus.named(AllowedMeta.Melee, "Plate of the Lightning Emperor Prot Damage");

        return new ModelCombined(statMix, combinedRequire, reforge, enchants, setBonus, SpecType.PaladinProtDps, gemChoice);
    }

    public static ModelCombined pallyProtVariableModel(StatRatings stats, boolean mitigationSetBonuses) {
        StatRequirements combinedRequire = StatRequirementsHitExpertise.protFlexibleParry();

        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProtDps, true);
        ReforgeRules reforge = ReforgeRules.tank();
        SetBonus setBonus = mitigationSetBonuses
            ? SetBonus.named(AllowedMeta.Tank, "White Tiger Battlegear Prot Mitigation", "Plate of the Lightning Emperor Prot Mitigation")
            : SetBonus.named(AllowedMeta.Melee, "Plate of the Lightning Emperor Prot Damage");
        GemChoice gemChoice = mitigationSetBonuses ? GemChoice.protMitigationGems() : GemChoice.protDpsGems();
        return new ModelCombined(stats, combinedRequire, reforge, enchants, setBonus, SpecType.PaladinProtMitigation, gemChoice);
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
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Melee, null);
        ReforgeRules reforge = ReforgeRules.melee();
        SetBonus setBonus = SetBonus.named(AllowedMeta.Melee, "White Tiger Battlegear", "Battlegear of the Lightning Emperor");
        return new ModelCombined(statRatings, combinedRequire, reforge, enchants, setBonus, SpecType.PaladinRet, gemChoice);
    }

    private static ModelCombined standardTankModel(SpecType spec) {
        StatRatings statRatings = new StatRatingsWeights(specToWeightFile(spec));
        StatRequirements statRequirements = StatRequirementsHitExpertise.protFlexibleParry();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProtMitigation, false);
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Tank, null);
        ReforgeRules reforge = ReforgeRules.tank();
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus, spec, gemChoice);
    }

    private static ModelCombined standardMeleeModel(SpecType spec) {
        StatRatingsWeights statRatings = new StatRatingsWeights(specToWeightFile(spec));
        StatRequirements statRequirements = StatRequirementsHitExpertise.retWideCapRange();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet, true);
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Melee, null);
        ReforgeRules reforge = ReforgeRules.melee();
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus, spec, gemChoice);
    }

    private static ModelCombined standardHybridCasterModel(SpecType spec) {
        StatRatings statRatings = new StatRatingsWeights(specToWeightFile(spec));
        StatRequirements statRequirements = new StatRequirementsHitCombined(
                StatRequirements.TARGET_RATING_CAST, StatRequirements.DEFAULT_CAP_ALLOW_EXCEED);
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Caster, null);
        ReforgeRules reforge = ReforgeRules.casterHybrid();
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus, spec, gemChoice);
    }

    private static ModelCombined standardCasterModel(SpecType spec) {
        StatRatings statRatings = new StatRatingsWeights(specToWeightFile(spec));
        StatRequirements statRequirements = new StatRequirementsHitOnly(
                StatRequirements.TARGET_RATING_CAST, StatRequirements.DEFAULT_CAP_ALLOW_EXCEED);
        DefaultEnchants enchants = new DefaultEnchants(SpecType.Warlock, false);
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Caster, null);
        ReforgeRules reforge = ReforgeRules.casterPure();
        SetBonus setBonus = SetBonus.forSpec(spec);
        return new ModelCombined(statRatings, statRequirements, reforge, enchants, setBonus, spec, gemChoice);
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
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Heal, null);
        SetBonus setBonus = SetBonus.forSpec(SpecType.DruidTree);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.DruidTree, gemChoice);
    }

    private static ModelCombined paladinHolyModel() {
        StatRatings statRatings = new StatRatingsPriority(
                new StatType[]{Primary, Spirit, Mastery, Haste, Crit}
        );
        StatRequirements statRequirements = new StatRequirementsNull();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Heal, null);
        SetBonus setBonus = SetBonus.forSpec(SpecType.PaladinHoly);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.PaladinHoly, gemChoice);
    }

    private static ModelCombined shamanRestoration() {
        StatRatings statRatings = new StatRatingsPriorityBreaksMultiple(
                Haste,
                new int[]{1345, 2017, 3379, 8085, 8785, 10118, 14170, 14846, 18207, 19557, 21596, 26308},
                new StatType[]{Primary, Spirit, Crit, Mastery}
        );
        StatRequirements statRequirements = new StatRequirementsNull();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Heal, null);
        SetBonus setBonus = SetBonus.forSpec(SpecType.ShamanRestoration);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.ShamanRestoration, gemChoice);
    }

    private static ModelCombined priestHoly() {
        StatRatings statRatings = new StatRatingsPriority(
                new StatType[]{Primary, Spirit, Crit, Mastery, Haste}
        );
        StatRequirements statRequirements = new StatRequirementsNull();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom, false);
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Heal, null);
        SetBonus setBonus = SetBonus.forSpec(SpecType.PriestHoly);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.PriestHoly, gemChoice);
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
        GemChoice gemChoice = new GemChoice(statRatings, AllowedMeta.Heal, null);
        SetBonus setBonus = SetBonus.forSpec(SpecType.MonkMistweaver);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.casterHybrid(), enchants, setBonus, SpecType.MonkMistweaver, gemChoice);
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
