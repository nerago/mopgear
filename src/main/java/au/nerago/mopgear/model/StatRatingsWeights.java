package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.SocketType;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;

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
        if (weight.isEmpty())
            throw new IllegalArgumentException("empty weight file " + weightFile);
        chooseGems();
        chooseBestStats();
    }

    private StatRatingsWeights(StatBlock weight, EnumMap<SocketType, StatBlock> standardGems) {
        this.weight = weight;
        if (standardGems != null) {
            this.standardGems = standardGems;
        } else {
            chooseGems();
        }
        chooseBestStats();
    }

    public static StatRatingsWeights mix(StatRatingsWeights weightA, int multiplyA, StatRatingsWeights weightB, int multiplyB, EnumMap<SocketType, StatBlock> standardGems) {
        StatBlock mixed = weightA.weight.multiply(multiplyA);
//        if (weightB != null) {
            mixed = mixed.plus(weightB.weight.multiply(multiplyB));
//        }
        return new StatRatingsWeights(mixed, standardGems);
    }

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
        if (stat.stam() <= 0)
            stat = stat.withChange(Stam, 1);
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
        long total = 0;
        total += (long) value.primary() * (long) weight.primary();
        total += (long) value.stam() * (long) weight.stam();
        total += (long) value.mastery() * (long) weight.mastery();
        total += (long) value.crit() * (long) weight.crit();
        total += (long) value.parry() * (long) weight.parry();
        total += (long) value.haste() * (long) weight.haste();
        total += (long) value.dodge() * (long) weight.dodge();
        total += (long) value.hit() * (long) weight.hit();
        total += (long) value.expertise() * (long) weight.expertise();
        total += (long) value.spirit() * (long) weight.spirit();
        return total;
    }

    @Override
    public long calcRating(StatType stat, int value) {
        return (long) value * (long) weight.get(stat);
    }

}
