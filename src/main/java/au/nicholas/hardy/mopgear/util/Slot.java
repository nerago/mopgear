package au.nicholas.hardy.mopgear.util;

public enum Slot {
    Head(1),
    Neck(2),
    Shoulder(3),
    Back(16),
    Chest(5),
    Wrist(9),
    Hand(10),
    Belt(6),
    Leg(7),
    Foot(8),
    Ring(11),
    Trinket(12),
    Weapon(17),
    Offhand(14);

    private final int wowHeadSlotNum;

    Slot(int wowHeadSlotNum) {
        this.wowHeadSlotNum = wowHeadSlotNum;
    }

    public static Slot withNum(int wowHeadSlotNum) {
        for (Slot slot : values()) {
            if (slot.wowHeadSlotNum == wowHeadSlotNum)
                return slot;
        }
        throw new IllegalArgumentException("unknown slot " + wowHeadSlotNum);
    }
}
