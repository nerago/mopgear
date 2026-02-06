package au.nerago.mopgear.domain;

public enum SlotEquip {
    Head,
    Neck,
    Shoulder,
    Back,
    Chest,
    Wrist,
    Hand,
    Belt,
    Leg,
    Foot,
    Ring1,
    Ring2,
    Trinket1,
    Trinket2,
    Weapon,
    Offhand;

    public SlotEquip pairedSlot() {
        switch (this) {
            case Ring1 -> {
                return Ring2;
            }
            case Ring2 -> {
                return Ring1;
            }
            case Trinket1 -> {
                return Trinket2;
            }
            case Trinket2 -> {
                return Trinket1;
            }
            default -> {
                return null;
            }
        }
    }

    public static final int Head_Ordinal = 0;
    public static final int Neck_Ordinal = 1;
    public static final int Shoulder_Ordinal = 2;
    public static final int Back_Ordinal = 3;
    public static final int Chest_Ordinal = 4;
    public static final int Wrist_Ordinal = 5;
    public static final int Hand_Ordinal = 6;
    public static final int Belt_Ordinal = 7;
    public static final int Leg_Ordinal = 8;
    public static final int Foot_Ordinal = 9;
    public static final int Ring1_Ordinal = 10;
    public static final int Ring2_Ordinal = 11;
    public static final int Trinket1_Ordinal = 12;
    public static final int Trinket2_Ordinal = 13;
    public static final int Weapon_Ordinal = 14;
    public static final int Offhand_Ordinal = 15;
}
