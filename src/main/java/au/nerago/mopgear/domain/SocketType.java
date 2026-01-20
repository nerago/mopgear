package au.nerago.mopgear.domain;

public enum SocketType {
    General(0, new StatType[]{}),
    Meta(1, new StatType[]{}),
    Red(2, new StatType[]{StatType.Primary, StatType.Expertise}),
    Yellow(3, new StatType[]{StatType.Crit, StatType.Haste, StatType.Mastery}),
    Blue(4, new StatType[]{StatType.Hit, StatType.Spirit, StatType.Stam}),
    Sha(5, new StatType[]{}),
    Engineer(6, new StatType[]{StatType.Crit, StatType.Haste, StatType.Mastery, StatType.Expertise, StatType.Hit, StatType.Spirit}),;

    private final int wowHeadType;
    private final StatType[] matchingStats;

    SocketType(int wowHeadType, StatType[] matchingStats) {
        this.wowHeadType = wowHeadType;
        this.matchingStats = matchingStats;
    }

    public static SocketType withNum(int wowHeadType) {
        for (SocketType slot : values()) {
            if (slot.wowHeadType == wowHeadType)
                return slot;
        }
        throw new RuntimeException("unknown socket type " + wowHeadType);
    }

    public int getWowHeadType() {
        return wowHeadType;
    }

    public StatType[] getMatchingStats() {
        return matchingStats;
    }
}
