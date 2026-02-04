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
}
