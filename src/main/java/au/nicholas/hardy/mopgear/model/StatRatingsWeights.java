package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.SocketType;
import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.domain.StatType;
import au.nicholas.hardy.mopgear.util.BestHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;

import static au.nicholas.hardy.mopgear.domain.StatType.*;

public class StatRatingsWeights extends StatRatings {
    public static final int PROT_MULTIPLY = 17;

    private final StatBlock weight;
    private final boolean includeHit;
    private EnumMap<SocketType, StatBlock> standardGems;
    private StatType bestNonHit;

    public StatRatingsWeights(Path weightFile, boolean includeHit) {
        try (BufferedReader reader = Files.newBufferedReader(weightFile)) {
            weight = parseReader(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        this.includeHit = includeHit;
        chooseGems();
    }

    private StatRatingsWeights(StatBlock weight) {
        this.weight = weight;
        this.includeHit = false;
    }

    // because a sim value doesn't understand breakpoints
    public static StatRatingsWeights hardCodeRetWeight() {
        // ( Pawn: v1: "Retribution WoWSims Weights": Class=Paladin,Strength=1.000,HitRating=0.762,CritRating=0.375,HasteRating=0.561,ExpertiseRating=0.530,MasteryRating=0.369,Ap=0.436,MeleeDps=1.632 )
        // this was initial sim value at approx 6700 haste
         return new StatRatingsWeights(new StatBlock(1000,0,375,369,0,561,0,0,0,0));
        // however haste isn't always that good, so dropped a bit to round number
        // also crit is valued higher than I'd like so moved some value from crit->mastery
//        return new StatRatingsWeights(new StatBlock(1000,0,389,355,0,500,0,0,0,0));
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
        total += hasteValue(value);
        total += value.dodge * weight.dodge;
        if (includeHit) {
            total += value.hit * weight.hit;
            total += value.expertise * weight.expertise;
            total += value.spirit * weight.spirit;
        }
        return total;
    }

    @Override
    public long calcRating(StatType stat, int value) {
        return (long) value * weight.get(stat);
    }

    private int hasteValue(StatBlock value) {
        // TODO breakpoints
        return value.haste * weight.haste;
    }
}
