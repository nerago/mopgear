package au.nicholas.hardy.mopgear.domain;

public enum SlotItem {
    Head(1, SlotEquip.Head),
    Neck(2, SlotEquip.Neck),
    Shoulder(3, SlotEquip.Shoulder),
    Back(16, SlotEquip.Back),
    Chest(5, SlotEquip.Chest),
    Wrist(9, SlotEquip.Wrist),
    Hand(10, SlotEquip.Hand),
    Belt(6, SlotEquip.Belt),
    Leg(7, SlotEquip.Leg),
    Foot(8, SlotEquip.Foot),
    Ring(11, SlotEquip.Ring1),
    Trinket(12, SlotEquip.Trinket1),
//    Weapon(17, SlotEquip.Weapon), // 2H
    Weapon(13, SlotEquip.Weapon), // 1H
    Offhand(14, SlotEquip.Offhand);

    private final int wowHeadSlotNum;
    private final SlotEquip slotEquip;

    SlotItem(int wowHeadSlotNum, SlotEquip slotEquip) {
        this.wowHeadSlotNum = wowHeadSlotNum;
        this.slotEquip = slotEquip;
    }

    public static SlotItem withNum(int wowHeadSlotNum) {
        for (SlotItem slot : values()) {
            if (slot.wowHeadSlotNum == wowHeadSlotNum)
                return slot;
        }
        if (wowHeadSlotNum == 17)
            return SlotItem.Weapon;
        else if (wowHeadSlotNum == 20)
            return SlotItem.Chest;
        else if (wowHeadSlotNum == 23)
            return SlotItem.Offhand;
        else if (wowHeadSlotNum == 26)
            return SlotItem.Weapon;
        throw new IllegalArgumentException("unknown slot " + wowHeadSlotNum);
    }

    public SlotEquip toSlotEquip() {
        return slotEquip;
    }
}
