package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static au.nerago.mopgear.domain.StatType.*;

public class StatRatingsWeights extends StatRatings {
    private final StatBlock weight;

    public StatRatingsWeights(Path weightFile) {
        this(weightFile, false, false, false);
    }

    public StatRatingsWeights(Path weightFile, boolean includeHit, boolean includeExpertise, boolean includeSpirit) {
        try (BufferedReader reader = Files.newBufferedReader(weightFile)) {
            StatBlock proposedWeight = parseReader(reader);
            if (!includeHit)
                proposedWeight = proposedWeight.withChange(Hit, 0);
            if (!includeExpertise)
                proposedWeight = proposedWeight.withChange(Expertise, 0);
            if (!includeSpirit)
                proposedWeight = proposedWeight.withChange(Spirit, 0);
            this.weight = proposedWeight;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        chooseGems();
        chooseBestStats();
    }

    private StatRatingsWeights(StatBlock weight) {
        this.weight = weight;
    }

    public static StatRatingsWeights mix(StatRatingsWeights weightA, int multiplyA, StatRatingsWeights weightB, int multiplyB) {
        StatBlock mixed = weightA.weight.multiply(multiplyA);
        if (weightB != null) {
            mixed = mixed.plus(weightB.weight.multiply(multiplyB));
        }
        return new StatRatingsWeights(mixed);
    }

    // because a sim value doesn't understand breakpoints
//    public static StatRatingsWeights hardCodeRetWeight() {
//        // ( Pawn: v1: "Retribution WoWSims Weights": Class=Paladin,Strength=1.000,HitRating=0.762,CritRating=0.375,HasteRating=0.561,ExpertiseRating=0.530,MasteryRating=0.369,Ap=0.436,MeleeDps=1.632 )
//        // this was initial sim value at approx 6700 haste
//        return new StatRatingsWeights(new StatBlock(1000, 0, 375, 369, 0, 561, 0, 0, 0, 0));
//        // however haste isn't always that good, so dropped a bit to round number
//        // also crit is valued higher than I'd like so moved some value from crit->mastery
////        return new StatRatingsWeights(new StatBlock(1000,0,389,355,0,500,0,0,0,0));
//    }

    private static StatBlock parseReader(BufferedReader reader) throws IOException {
        StringBuilder build = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            build.append(line);
        }

        StatBlock stat = StatBlock.empty;
        String content = build.toString();
        String[] parts = content.split(",");
        for (int i = 1; i < parts.length; ++i) {
            String[] pair = parts[i].split("=");
            stat = switch (pair[0]) {
                case "Intellect", "Strength" -> addNum(stat, Primary, pair[1]);
                case "Stamina" -> addNum(stat, StatType.Stam, pair[1]);
                case "HitRating" -> addNum(stat, Hit, pair[1]);
                case "CritRating" -> addNum(stat, StatType.Crit, pair[1]);
                case "HasteRating" -> addNum(stat, StatType.Haste, pair[1]);
                case "ExpertiseRating" -> addNum(stat, Expertise, pair[1]);
                case "MasteryRating" -> addNum(stat, StatType.Mastery, pair[1]);
                case "DodgeRating" -> addNum(stat, StatType.Dodge, pair[1]);
                case "ParryRating" -> addNum(stat, StatType.Parry, pair[1]);
                case "Spirit" -> addNum(stat, StatType.Spirit, pair[1]);
                default -> stat;
            };
        }
        return stat;
    }

    private static StatBlock addNum(StatBlock block, StatType type, String text) {
        float givenValue = Float.parseFloat(text);
        if (givenValue > 0) {
            int intValue = Math.round(givenValue * 1000f);
            return block.withChange(type, intValue);
        } else {
            return block;
        }
    }

    @Override
    public long calcRating(StatBlock value) {
        int total = 0;
        total += value.primary * weight.primary;
        total += value.stam * weight.stam;
        total += value.mastery * weight.mastery;
        total += value.crit * weight.crit;
        total += value.parry * weight.parry;
        total += value.haste * weight.haste;
        total += value.dodge * weight.dodge;
            total += value.hit * weight.hit;
            total += value.expertise * weight.expertise;
            total += value.spirit * weight.spirit;
        return total;
    }

    @Override
    public long calcRating(StatBlock aaa, StatBlock bbb) {
        int total = 0;
        total += (aaa.primary + bbb.primary) * weight.primary;
        total += (aaa.stam + bbb.stam) * weight.stam;
        total += (aaa.mastery + bbb.mastery) * weight.mastery;
        total += (aaa.crit + bbb.crit) * weight.crit;
        total += (aaa.parry + bbb.parry) * weight.parry;
        total += (aaa.haste + bbb.haste) * weight.haste;
        total += (aaa.dodge + bbb.dodge) * weight.dodge;
            total += (aaa.hit + bbb.hit) * weight.hit;
            total += (aaa.expertise + bbb.expertise) * weight.expertise;
            total += (aaa.spirit + bbb.spirit) * weight.spirit;
        return total;
    }

    @Override
    public long calcRating(StatType stat, int value) {
        return (long) value * weight.get(stat);
    }

}
