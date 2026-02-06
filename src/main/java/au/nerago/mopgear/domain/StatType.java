package au.nerago.mopgear.domain;

public enum StatType {
    Primary(0),
    Stam(2),
    Mastery(11),
    Crit(6),
    Hit(5),
    Haste(7),
    Expertise(8),
    Dodge(9),
    Parry(10),
    Spirit(4);

    public final int simIndex;

    StatType(int simIndex) {
        this.simIndex = simIndex;
    }

    public static final int Primary_Ordinal = 0;
    public static final int Stam_Ordinal = 1;
    public static final int Mastery_Ordinal = 2;
    public static final int Crit_Ordinal = 3;
    public static final int Hit_Ordinal = 4;
    public static final int Haste_Ordinal = 5;
    public static final int Expertise_Ordinal = 6;
    public static final int Dodge_Ordinal = 7;
    public static final int Parry_Ordinal = 8;
    public static final int Spirit_Ordinal = 9;
}
