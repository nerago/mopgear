package au.nicholas.hardy.mopgear;

public class ReforgeRules {
    private static StatType[] reforgeSource = new StatType[]{StatType.Mastery, StatType.Crit, StatType.Hit, StatType.Haste, StatType.Expertise, StatType.Dodge, StatType.Parry};

    //    static final Secondary[] reforgeTargets = new Secondary[]{Secondary.Hit, Secondary.Expertise, Secondary.Haste, Secondary.Mastery};
    private static final StatType[] reforgeTargets = new StatType[]{StatType.Hit, StatType.Expertise, StatType.Haste};

    public StatType[] source() {
        return reforgeSource;
    }

    public StatType[] target() {
        return reforgeTargets;
    }
}
