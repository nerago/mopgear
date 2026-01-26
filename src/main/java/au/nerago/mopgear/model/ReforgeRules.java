package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.StatType;

import static au.nerago.mopgear.domain.StatType.*;

public class ReforgeRules {
    private static final StatType[] reforgeSource = new StatType[]{Spirit, Hit, Expertise, Mastery, Haste, Crit, Dodge, Parry};

    private static final StatType[] reforgeTargetsTank = new StatType[]{Hit, Expertise, Mastery, Haste, Dodge /* ,Crit, Parry */};
    private static final StatType[] reforgeTargetsCasterPure = new StatType[]{Hit, Mastery, Haste, Crit};
    private static final StatType[] reforgeTargetsCasterHybrid = new StatType[]{Spirit, Hit, Mastery, Haste, Crit};
    private static final StatType[] reforgeTargetsMelee = new StatType[]{Hit, Expertise, Haste, Mastery, Crit};

    private final StatType[] reforgeTargets;

    private ReforgeRules(StatType[] reforgeTargets) {
        this.reforgeTargets = reforgeTargets;
    }

    public static ReforgeRules tank() {
        return new ReforgeRules(reforgeTargetsTank);
    }

    public static ReforgeRules melee() {
        return new ReforgeRules(reforgeTargetsMelee);
    }

    public static ReforgeRules casterHybrid() {
        return new ReforgeRules(reforgeTargetsCasterHybrid);
    }

    public static ReforgeRules casterPure() {
        return new ReforgeRules(reforgeTargetsCasterPure);
    }

    public StatType[] source() {
        return reforgeSource;
    }

    public StatType[] target() {
        return reforgeTargets;
    }
}
