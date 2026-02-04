package au.nerago.mopgear;

import au.nerago.mopgear.domain.ReforgeRecipe;
import au.nerago.mopgear.io.DataLocation;
import au.nerago.mopgear.io.StandardModels;
import au.nerago.mopgear.process.FindMultiSpec;
import au.nerago.mopgear.process.FindMultiSpecSim;

import java.util.Collection;

import static au.nerago.mopgear.domain.StatType.Crit;
import static au.nerago.mopgear.domain.StatType.Haste;

public class TaskMulti {
    public static void paladinMultiSpecSolve() {
        FindMultiSpec multi = new FindMultiSpec(1);

        multi.addFixedForge(94519, new ReforgeRecipe(Crit, Haste)); // Primordius trinket
        multi.addFixedForge(94526, ReforgeRecipe.empty()); // zandalar trinket
//        multi.addFixedForge(87050, new ReforgeRecipe(Parry, Haste)); // Offhand Steelskin, Qiang's Impervious Shield
//        multi.addFixedForge(87026, new ReforgeRecipe(Expertise, Haste)); // Back Cloak of Peacock Feathers
//        multi.addFixedForge(86957, new ReforgeRecipe(null, null)); // Ring Ring of the Bladed Tempest
//        multi.addFixedForge(86955, new ReforgeRecipe(Mastery, Expertise)); // Belt Waistplate of Overwhelming Assault
//        multi.addFixedForge(86387, new ReforgeRecipe(Hit, Haste)); // Weapon1H Kilrak, Jaws of Terror

        int extraUpgrade = 2;
        boolean preUpgrade = false;

        multi.addSpec(
                "RET",
                DataLocation.gearRetFile,
                StandardModels.pallyRetModel(),
                0.02,
                false,
                new int[]{
                        87026, // heroic peacock cloak
                        94942, // hydra bloodcloak

                        95140, // shado assault band
                        86957, // heroic bladed tempest ring
                        95513, // scaled tyrant normal

                        87015, // heroic clawfeet
                        86979, // heroic impaling treads
                        87024, // null greathelm
                        86955, // heroic overwhelm assault belt
                        94726, // cloudbreaker belt

                        95535, // normal lightning legs
                        94773, // centripetal shoulders normal
                        96468, // talonrender chest heroic

                        85340, // ret tier14 legs
//                        87101, // ret tier14 head [would need to regem, AVOID]
                        85339, // ret tier14 shoulder
                        85343, // ret tier14 chest
                        87100, // ret tier14 hands

                        95910, // ret tier15 chest celestial
                        95911, // ret tier15 gloves celestial
//                        95912, // ret tier15 celestial (don't have yet)
//                        95913, // ret tier15 celestial (don't have yet)
                        95914, // ret tier15 shoulder celestial

                        95142, // striker's battletags
                        95205, // terra-cotta neck

                        87145, // defiled earth
                        89934, // soul bracer
                        94820, // caustic spike bracers
                },
                extraUpgrade,
                preUpgrade
        )
        ;

        multi.addSpec(
                        "PROT-DAMAGE",
                        DataLocation.gearProtDpsFile,
                        StandardModels.pallyProtDpsModel(),
                        0.60,
                        false,
                        new int[]{
                                86957, // heroic bladed tempest ring
                                95140, // shado assault band
                                86946, // ruby signet heroic
                                95513, // scaled tyrant normal

                                87015, // heroic clawfeet
                                86979, // heroic impaling treads
                                94726, // cloudbreaker belt
                                87024, // null greathelm
                                94942, // hydra bloodcloak

                                87026, // heroic peacock cloak
                                86955, // heroic overwhelm assault belt
                                95535, // normal lightning legs
                                94773, // centripetal shoulders normal
                                96468, // talonrender chest heroic

                                85340, // ret tier14 legs
//                                87101, // ret tier14 head (has tank gem currently)
                                85339, // ret tier14 shoulder
                                85343, // ret tier14 chest
                                87100, // ret tier14 hands

                                95910, // ret tier15 chest celestial
                                95911, // ret tier15 gloves celestial
//                                95912, // ret tier15 celestial (don't have yet) Lightning Emperor's Helmet
//                                95913, // ret tier15 celestial (don't have yet)
                                95914, // ret tier15 shoulder celestial

                                95291, // prot tier15 hand normal
                                95920, // prot tier15 chest celestial
                                95292, // prot tier15 head normal
                                96667, // prot tier15 leg heroic
                                95924, // prot tier15 shoulder celestial

                                95142, // striker's battletags
                                95205, // terra-cotta neck
                                87036, // soulgrasp heroic

                                96182, // ultimate prot of the emperor thunder

                                87145, // defiled earth
                                89934, // soul bracer
                                94820, // caustic spike bracers

                                96376, // worldbreaker weapon
                        },
                        extraUpgrade,
                        preUpgrade)
//                .setWorstCommonPenalty(98)
        ;

        multi.addSpec(
                        "PROT-DEFENCE",
                        DataLocation.gearProtDefenceFile,
                        StandardModels.pallyProtMitigationModel(),
                        0.38,
                        false,
                        new int[]{
                                86979, // heroic impaling treads

                                86957, // heroic bladed tempest ring
                                86946, // ruby signet heroic
                                95140, // shado assault band
                                95513, // scaled tyrant normal

                                94726, // cloudbreaker belt
                                86955, // heroic overwhelm assault belt

                                87026, // heroic peacock cloak
                                86325, // daybreak
                                94942, // hydra bloodcloak

                                95535, // normal lightning legs
                                94773, // centripetal shoulders normal
                                96468, // talonrender chest heroic

                                87101, // ret tier14 head
                                87100, // ret tier14 hands

                                95291, // prot tier15 hand normal
                                95920, // prot tier15 chest celestial
                                95292, // prot tier15 head normal
                                96667, // prot tier15 leg heroic
                                95924, // prot tier15 shoulder celestial

                                95142, // striker's battletags
                                95205, // terra-cotta neck
                                95178, // lootraptor amulet

                                96182, // ultimate prot of the emperor thunder

                                87145, // defiled earth
                                89934, // soul bracer
                                94820, // caustic spike bracers

                                96376, // worldbreaker weapon
                        },
                        extraUpgrade,
                        preUpgrade)
//                .setWorstCommonPenalty(98)
        ;

//        multi.multiSetFilter(proposedResults -> {
//            Set<Integer> uniqueItems = proposedResults.resultJobs().stream()
//                    .map(job -> job.resultSet.orElseThrow())
//                    .flatMap(itemSet -> itemSet.items().itemStream())
//                    .map(SolvableItem::itemId)
//                    .collect(Collectors.toSet());
//            return !uniqueItems.contains(87111) || !uniqueItems.contains(87101);
//        });

//        multi.suppressSlotCheck(95513);
        multi.suppressSlotCheck(86946); // Vizier's Ruby Signet
        multi.suppressSlotCheck(86957); // Ring of the Bladed Tempest
        multi.suppressSlotCheck(95140); // Band of the Shado-Pan Assault

//        multi.overrideEnchant(86905, StatBlock.of(StatType.Primary, 500));

//        multi.solve(1000);
        multi.suggestCulls(60000);
//        multi.solve(10000);
//        multi.solve(50000);
//        multi.solve(120000);
//        multi.solve(220000);
//        multi.solve(490000);
//        multi.solve(1490000);
//        multi.solve(4000000);

//        Collection<FindMultiSpec.ProposedResults> select = multi.solveBestSelection(2000, 10);
//        Collection<FindMultiSpec.ProposedResults> select = multi.solveBestSelection(400000, 16);
//        new FindMultiSpecSim(multi).process(select);
    }
}
