package au.nicholas.hardy.mopgear;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StatRatingsWeights implements StatRatings {
    public static final int PROT_MULT = 12;
    // ( Pawn: v1: "Retribution WoWSims Weights": Class=Paladin,Strength=1.000,HitRating=0.513,CritRating=0.256,HasteRating=0.448,ExpertiseRating=0.426,MasteryRating=0.260,Ap=0.437,MeleeDps=1.633 )

    private final StatBlock weight;
    private final boolean includeHit;
    private final long numerator;
    private final long denominator;

    public StatRatingsWeights(Path weightFile, boolean includeHit, int numerator, int denominator) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(weightFile)) {
            weight = parseReader(reader);
        }
        this.includeHit = includeHit;
        this.numerator = numerator;
        this.denominator = denominator;
    }

    private StatRatingsWeights(StatBlock weight) {
        this.weight = weight;
        this.includeHit = false;
        this.numerator = 1;
        this.denominator = 1;
    }

//    public static StatRatingsWeights protHardcode() {
//        // artifically inflated mastery, hit/exp. rest from sim
//        return new StatRatingsWeights(new StatBlock(953, 1200, 1, 3000, 637, 3000, 997, 990));
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
                case "Strength" -> addNum(stat, StatType.Strength, pair[1]);
                case "Stamina" -> addNum(stat, StatType.Stam, pair[1]);
                case "HitRating" -> addNum(stat, StatType.Hit, pair[1]);
                case "CritRating" -> addNum(stat, StatType.Crit, pair[1]);
                case "HasteRating" -> addNum(stat, StatType.Haste, pair[1]);
                case "ExpertiseRating" -> addNum(stat, StatType.Expertise, pair[1]);
                case "MasteryRating" -> addNum(stat, StatType.Mastery, pair[1]);
                case "DodgeRating" -> addNum(stat, StatType.Dodge, pair[1]);
                case "ParryRating" -> addNum(stat, StatType.Parry, pair[1]);
                default -> stat;
            };
        }
        return stat;
    }

    private static StatBlock addNum(StatBlock block, StatType type, String text) {
        float givenValue = Float.parseFloat(text);
        int intValue = Math.round(givenValue * 1000f);
        return block.withChange(type, intValue);
    }

    @Override
    public long calcRating(StatBlock value) {
        int total = 0;
        total += value.str * weight.str;
        total += value.mastery * weight.mastery;
        total += value.crit * weight.crit;
        total += value.parry * weight.parry;
        total += value.haste * weight.haste;
        total += value.dodge * weight.dodge;
        if (includeHit) {
            total += value.hit * weight.hit;
            total += value.expertise * weight.expertise;
        }
        return (total * numerator) / denominator;
    }
}
