package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.util.ArrayUtil;

import static au.nerago.mopgear.domain.StatType.*;

public class ReforgeRules {
    private static final StatType[] reforgeSource = new StatType[]{Mastery, Crit, Haste, Hit, Spirit, Expertise, Dodge, Parry};

    private static final StatType[] reforgeTargetsRetExtended = new StatType[]{Hit, Expertise, Haste, Mastery};
    private static final StatType[] reforgeTargetsRet = new StatType[]{Hit, Expertise, Mastery, Haste};
    private static final StatType[] reforgeTargetsProt = new StatType[]{Hit, Expertise, Mastery, Haste, Crit};
    private static final StatType[] reforgeTargetsBoom = new StatType[]{Spirit, Hit, Mastery, Haste, Crit};
    private static final StatType[] reforgeTargetsBear = new StatType[]{Hit, Mastery, Haste, Crit, Dodge};
    private static final StatType[] reforgeTargetsWarlock = new StatType[]{Hit, Mastery, Haste, Crit};

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
    public static ReforgeRules retExtended() {
        return new ReforgeRules(reforgeTargetsRetExtended);
    }

    public static ReforgeRules boom() {
        return new ReforgeRules(reforgeTargetsBoom);
    }
    public static ReforgeRules bear() {
        return new ReforgeRules(reforgeTargetsBear);
    }

    public static ReforgeRules warlock() {
        return new ReforgeRules(reforgeTargetsWarlock);
    }

    public static ReforgeRules common() {
        return new ReforgeRules(ArrayUtil.common(reforgeTargetsRet, reforgeTargetsProt));
    }

    public StatType[] source() {
        return reforgeSource;
    }

    public StatType[] target() {
        return reforgeTargets;
    }
}
