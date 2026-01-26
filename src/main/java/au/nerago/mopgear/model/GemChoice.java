package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.GemInfo;
import au.nerago.mopgear.domain.SocketType;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import static au.nerago.mopgear.domain.StatType.*;

public class GemChoice {
    private static final GemInfo SHA_INFO = new GemInfo(89881, StatBlock.of(Primary, 500));
    private static final GemInfo HASTE_INFO = new GemInfo(76699, StatBlock.of(Haste, 320));
    public static final GemInfo HASTE_EXP_INFO = new GemInfo(76667, StatBlock.of(StatType.Haste, 160, StatType.Expertise, 160));
    public static final GemInfo HASTE_STAM_INFO = new GemInfo(76588, StatBlock.of(StatType.Haste, 160, StatType.Stam, 120));
    public static final GemInfo HASTE_HIT_INFO = new GemInfo(76588, StatBlock.of(StatType.Haste, 160, StatType.Hit, 160));
    public static final GemInfo HASTE_ENG_INFO = new GemInfo(77542, StatBlock.of(StatType.Haste, 600));
    private final GemInfo alternateBestGem;
    protected final EnumMap<SocketType, GemInfo> chosen;

    public GemChoice(StatRatings ratings, AllowedMeta meta, GemInfo alternateBestGem) {
        this.alternateBestGem = alternateBestGem;
        this.chosen = buildChosen(ratings, meta);
    }

    private GemChoice(EnumMap<SocketType, GemInfo> chosen, GemInfo alternateBestGem) {
        this.alternateBestGem = alternateBestGem;
        this.chosen = chosen;
    }

    private static EnumMap<SocketType, GemInfo> buildChosen(StatRatings ratings, AllowedMeta meta) {
        EnumMap<SocketType, GemInfo> chosen = new EnumMap<>(SocketType.class);
        GemData.chooseGem(chosen, SocketType.Red, ratings::calcRating, GemData.standardGems());
        GemData.chooseGem(chosen, SocketType.Blue, ratings::calcRating, GemData.standardGems());
        GemData.chooseGem(chosen, SocketType.Yellow, ratings::calcRating, GemData.standardGems());
        GemData.chooseGem(chosen, SocketType.General, ratings::calcRating, GemData.standardGems());
        GemData.chooseGem(chosen, SocketType.Engineer, ratings::calcRating, GemData.engineerGems());
//        chosen.put(SocketType.Meta, StatBlock.of(StatType.Primary, 216));
        chosen.put(SocketType.Meta, meta.asGemInfo());
        chosen.put(SocketType.Sha, SHA_INFO);
        return chosen;
    }

    public GemInfo gemChoice(SocketType socket) {
        GemInfo choice = chosen.get(socket);
        if (choice == null)
            throw new RuntimeException("no gem choice for " + socket);
        return choice;
    }

    public GemInfo gemChoiceBestAlternate() {
        if (alternateBestGem != null) {
            return alternateBestGem;
        } else {
            return gemChoice(SocketType.General);
        }
    }

    public List<GemInfo> alternateGems() {
        return Arrays.asList(HASTE_INFO, HASTE_STAM_INFO, HASTE_HIT_INFO, HASTE_EXP_INFO);
    }

    public static GemChoice protMitigationGems() {
        EnumMap<SocketType, GemInfo> gems = new EnumMap<>(SocketType.class);
        gems.put(SocketType.Red, HASTE_EXP_INFO);
        gems.put(SocketType.Blue, HASTE_STAM_INFO);
        gems.put(SocketType.Yellow, HASTE_INFO);
        gems.put(SocketType.General, HASTE_INFO);
        gems.put(SocketType.Meta, AllowedMeta.Tank.asGemInfo());
        gems.put(SocketType.Engineer, HASTE_ENG_INFO);
        gems.put(SocketType.Sha, SHA_INFO);
        return new GemChoice(gems, HASTE_INFO);
    }

    public static GemChoice protDpsGems() {
        EnumMap<SocketType, GemInfo> gems = new EnumMap<>(SocketType.class);
        gems.put(SocketType.Red, HASTE_EXP_INFO);
        gems.put(SocketType.Blue, HASTE_STAM_INFO);
        gems.put(SocketType.Yellow, HASTE_INFO);
        gems.put(SocketType.General, HASTE_INFO);
        gems.put(SocketType.Meta, AllowedMeta.Melee.asGemInfo());
        gems.put(SocketType.Engineer, HASTE_ENG_INFO);
        gems.put(SocketType.Sha, SHA_INFO);
        return new GemChoice(gems, HASTE_INFO);
    }
}
