package au.nicholas.hardy.mopgear;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModelWeights implements Model {
    // ( Pawn: v1: "Retribution WoWSims Weights": Class=Paladin,Strength=1.000,HitRating=0.513,CritRating=0.256,HasteRating=0.448,ExpertiseRating=0.426,MasteryRating=0.260,Ap=0.437,MeleeDps=1.633 )

    public StatBlock weight;

    public void load(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            parseReader(reader);
        }
    }

    private static void parseReader(BufferedReader reader) throws IOException {
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
                case "HitRating" -> addNum(stat, StatType.Hit, pair[1]);
                case "CritRating" -> addNum(stat, StatType.Crit, pair[1]);
                case "HasteRating" -> addNum(stat, StatType.Haste, pair[1]);
                case "ExpertiseRating" -> addNum(stat, StatType.Expertise, pair[1]);
                case "MasteryRating" -> addNum(stat, StatType.Mastery, pair[1]);
                default -> stat;
            };
        }
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
        total += value.hit * weight.hit;
        total += value.parry * weight.parry;
        total += value.haste * weight.haste;
        total += value.expertise * weight.expertise;
        total += value.dodge * weight.dodge;
        return total;
    }
}
