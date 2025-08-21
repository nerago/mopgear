package au.nicholas.hardy.mopgear.domain;

public enum SocketType {
    Meta(1, new StatType[]{}),
    Sha(0, new StatType[]{}),

    Red(2, new StatType[]{StatType.Primary, StatType.Expertise}),
    Yellow(3, new StatType[]{StatType.Crit, StatType.Haste,StatType.Mastery}),
    Blue(4, new StatType[]{StatType.Hit /*, StatType.Spirit*/}),
    General(0, new StatType[]{}),
    Engineer(6, new StatType[]{}),;

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
