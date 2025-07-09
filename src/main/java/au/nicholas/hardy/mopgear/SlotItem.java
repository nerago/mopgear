package au.nicholas.hardy.mopgear;

public enum SlotItem {
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

    SlotItem(int wowHeadSlotNum) {
        this.wowHeadSlotNum = wowHeadSlotNum;
    }

    public static SlotItem withNum(int wowHeadSlotNum) {
        for (SlotItem slot : values()) {
            if (slot.wowHeadSlotNum == wowHeadSlotNum)
                return slot;
        }
        throw new IllegalArgumentException("unknown slot " + wowHeadSlotNum);
    }
}
