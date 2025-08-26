package au.nicholas.hardy.mopgear.domain;

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
}
