package au.nerago.mopgear.domain;

public enum WowClass {
    Warrior(1),
    Paladin(2),
    Hunter(3),
    Rogue(4),
    Priest(5),
    DeathKnight(6),
    Shaman(7),
    Mage(8),
    Warlock(9),
    Monk(10),
    Druid(11);

    private final int classId;

    WowClass(int classId) {
        this.classId = classId;
    }

    public static WowClass forId(int classId) {
        for (WowClass wowClass : values()) {
            if (wowClass.classId == classId)
                return wowClass;
        }
        return null;
    }
}
