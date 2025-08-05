package au.nicholas.hardy.mopgear;

public class ReforgeRules {
    private static final StatType[] reforgeSource = new StatType[]{StatType.Mastery, StatType.Crit, StatType.Hit, StatType.Haste, StatType.Expertise, StatType.Dodge, StatType.Parry};

    private static final StatType[] reforgeTargetsRet = new StatType[]{StatType.Hit, StatType.Expertise, StatType.Haste}; // StatType.Mastery
//    private static final StatType[] reforgeTargetsProt = new StatType[]{StatType.Hit, StatType.Expertise, StatType.Mastery};
    private static final StatType[] reforgeTargetsProt = new StatType[]{StatType.Hit, StatType.Expertise, StatType.Mastery, StatType.Haste};

    private final StatType[] reforgeTargets;

    private ReforgeRules(StatType[] reforgeTargets) {
        this.reforgeTargets = reforgeTargets;
    }

    public static ReforgeRules prot() {
        return new ReforgeRules(reforgeTargetsProt);
    }

    public static ReforgeRules ret() {
        return new ReforgeRules(reforgeTargetsRet);
    }

    public StatType[] source() {
        return reforgeSource;
    }

    public StatType[] target() {
        return reforgeTargets;
    }
}
