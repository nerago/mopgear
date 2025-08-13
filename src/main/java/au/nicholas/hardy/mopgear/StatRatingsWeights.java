package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.BestHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StatRatingsWeights implements StatRatings {
    public static final int PROT_MULTIPLY = 17;

    private final StatBlock weight;
    private final boolean includeHit;
    private StatBlock standardGem;

    public StatRatingsWeights(Path weightFile, boolean includeHit, Integer defaultGem) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(weightFile)) {
            weight = parseReader(reader);
        }
        this.includeHit = includeHit;
        chooseGem(defaultGem);
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
                case "Intellect", "Strength" -> addNum(stat, StatType.Primary, pair[1]);
                case "Stamina" -> addNum(stat, StatType.Stam, pair[1]);
                case "HitRating" -> addNum(stat, StatType.Hit, pair[1]);
                case "CritRating" -> addNum(stat, StatType.Crit, pair[1]);
                case "HasteRating" -> addNum(stat, StatType.Haste, pair[1]);
                case "ExpertiseRating" -> addNum(stat, StatType.Expertise, pair[1]);
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
        int intValue = Math.round(givenValue * 1000f);
        return block.withChange(type, intValue);
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
        if (includeHit) {
            total += value.hit * weight.hit;
            total += value.expertise * weight.expertise;
            total += value.spirit * weight.spirit;
        }
        return total;
    }

    private void chooseGem(Integer defaultGem) {
        if (defaultGem != null) {
            standardGem = GemData.known.get(defaultGem);
        } else {
            StatType bestStat = getBestStat();
            standardGem = StatBlock.empty.withChange(bestStat, GemData.standardValue(bestStat));
        }
    }

    private StatType getBestStat() {
        BestHolder<StatType> best = new BestHolder<>(null, 0);
        for (StatType stat : StatType.values()) {
            int multiply = weight.get(stat);
            if (multiply > 0) {
                long value = GemData.standardValue(stat);
                best.add(stat, multiply * value);
            }
        }

        return best.get();
    }

    @Override
    public StatBlock standardGem() {
        return standardGem;
    }
}
