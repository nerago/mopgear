package au.nerago.mopgear.domain;

public enum SlotItem {
    Head(1, SlotEquip.Head, true),
    Neck(2, SlotEquip.Neck, true),
    Shoulder(3, SlotEquip.Shoulder, true),
    Back(16, SlotEquip.Back, true),
    Chest(5, SlotEquip.Chest, true),
    Wrist(9, SlotEquip.Wrist, true),
    Hand(10, SlotEquip.Hand, true),
    Belt(6, SlotEquip.Belt, true),
    Leg(7, SlotEquip.Leg, true),
    Foot(8, SlotEquip.Foot, true),
    Ring(11, SlotEquip.Ring1, true),
    Trinket(12, SlotEquip.Trinket1, false),
    Weapon2H(17, SlotEquip.Weapon, true),
    Weapon1H(13, SlotEquip.Weapon, true),
    Offhand(14, SlotEquip.Offhand, true);

    public static final SlotEquip[] ALL_RINGS = {SlotEquip.Ring1, SlotEquip.Ring2};
    public static final SlotEquip[] ALL_TRINKETS = {SlotEquip.Trinket1, SlotEquip.Trinket2};

    private final int wowHeadSlotNum;
    private final SlotEquip slotEquip;
    public final boolean addEnchantToCap;

    SlotItem(int wowHeadSlotNum, SlotEquip slotEquip, boolean addEnchantToCap) {
        this.wowHeadSlotNum = wowHeadSlotNum;
        this.slotEquip = slotEquip;
        this.addEnchantToCap = addEnchantToCap;
    }

    public static SlotItem withNum(int wowHeadSlotNum) {
        for (SlotItem slot : values()) {
            if (slot.wowHeadSlotNum == wowHeadSlotNum)
                return slot;
        }
        if (wowHeadSlotNum == 20)
            return SlotItem.Chest;
        else if (wowHeadSlotNum == 23)
            return SlotItem.Offhand;
//        else if (wowHeadSlotNum == 26)
//            return SlotItem.Weapon;
        throw new IllegalArgumentException("unknown slot " + wowHeadSlotNum);
    }

    public SlotEquip toSlotEquip() {
        return slotEquip;
    }

    public SlotEquip[] toSlotEquipOptions() {
        switch (this) {
            case Ring -> {
                return ALL_RINGS;
            }
            case Trinket -> {
                return ALL_TRINKETS;
            }
            default -> {
                return new SlotEquip[] { this.slotEquip };
            }
        }
    }
}
