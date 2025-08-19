package au.nicholas.hardy.mopgear;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class EquipMap {
    private ItemData head;
    private ItemData neck;
    private ItemData shoulder;
    private ItemData back;
    private ItemData chest;
    private ItemData wrist;
    private ItemData hand;
    private ItemData belt;
    private ItemData leg;
    private ItemData foot;
    private ItemData ring1;
    private ItemData ring2;
    private ItemData trinket1;
    private ItemData trinket2;
    private ItemData weapon;
    private ItemData offhand;

    public static EquipMap empty() {
        return new EquipMap();
    }

    public static EquipMap single(SlotEquip slot, ItemData item) {
        EquipMap map = new EquipMap();
        map.put(slot, item);
        return map;
    }

    private EquipMap() {
    }

    private EquipMap(EquipMap other) {
        this.head = other.head;
        this.neck = other.neck;
        this.shoulder = other.shoulder;
        this.back = other.back;
        this.chest = other.chest;
        this.wrist = other.wrist;
        this.hand = other.hand;
        this.belt = other.belt;
        this.leg = other.leg;
        this.foot = other.foot;
        this.ring1 = other.ring1;
        this.ring2 = other.ring2;
        this.trinket1 = other.trinket1;
        this.trinket2 = other.trinket2;
        this.weapon = other.weapon;
        this.offhand = other.offhand;
    }

    public ItemData get(SlotEquip slot) {
        return switch (slot) {
            case Head -> head;
            case Neck -> neck;
            case Shoulder -> shoulder;
            case Back -> back;
            case Chest -> chest;
            case Wrist -> wrist;
            case Hand -> hand;
            case Belt -> belt;
            case Leg -> leg;
            case Foot -> foot;
            case Ring1 -> ring1;
            case Ring2 -> ring2;
            case Trinket1 -> trinket1;
            case Trinket2 -> trinket2;
            case Weapon -> weapon;
            case Offhand -> offhand;
        };
    }

    public boolean has(SlotEquip slot) {
        return switch (slot) {
            case Head -> head != null;
            case Neck -> neck != null;
            case Shoulder -> shoulder != null;
            case Back -> back != null;
            case Chest -> chest != null;
            case Wrist -> wrist != null;
            case Hand -> hand != null;
            case Belt -> belt != null;
            case Leg -> leg != null;
            case Foot -> foot != null;
            case Ring1 -> ring1 != null;
            case Ring2 -> ring2 != null;
            case Trinket1 -> trinket1 != null;
            case Trinket2 -> trinket2 != null;
            case Weapon -> weapon != null;
            case Offhand -> offhand != null;
        };
    }

    public void put(SlotEquip slot, ItemData value) {
        switch (slot) {
            case Head -> head = value;
            case Neck -> neck = value;
            case Shoulder -> shoulder = value;
            case Back -> back = value;
            case Chest -> chest = value;
            case Wrist -> wrist = value;
            case Hand -> hand = value;
            case Belt -> belt = value;
            case Leg -> leg = value;
            case Foot -> foot = value;
            case Ring1 -> ring1 = value;
            case Ring2 -> ring2 = value;
            case Trinket1 -> trinket1 = value;
            case Trinket2 -> trinket2 = value;
            case Weapon -> weapon = value;
            case Offhand -> offhand = value;
            default -> throw new IllegalArgumentException();
        }
    }

    @Deprecated(since = "avoid extra allocation")
    public EquipMap shallowClone() {
        return new EquipMap(this);
    }

    public EquipMap copyWithReplace(SlotEquip slot, ItemData replace) {
        EquipMap other = new EquipMap(this);
        other.put(slot, replace);
        return other;
    }

//    @Deprecated(since = "avoid bad performance")
    public void forEachValue(Consumer<ItemData> func) {
        if (head != null) func.accept(head);
        if (neck != null) func.accept(neck);
        if (shoulder != null) func.accept(shoulder);
        if (back != null) func.accept(back);
        if (chest != null) func.accept(chest);
        if (wrist != null) func.accept(wrist);
        if (hand != null) func.accept(hand);
        if (belt != null) func.accept(belt);
        if (leg != null) func.accept(leg);
        if (foot != null) func.accept(foot);
        if (ring1 != null) func.accept(ring1);
        if (ring2 != null) func.accept(ring2);
        if (trinket1 != null) func.accept(trinket1);
        if (trinket2 != null) func.accept(trinket2);
        if (weapon != null) func.accept(weapon);
        if (offhand != null) func.accept(offhand);
    }

    @Deprecated(since = "avoid bad performance")
    public void forEachPair(BiConsumer<SlotEquip, ItemData> func) {
        if (head != null) func.accept(SlotEquip.Head, head);
        if (neck != null) func.accept(SlotEquip.Neck, neck);
        if (shoulder != null) func.accept(SlotEquip.Shoulder, shoulder);
        if (back != null) func.accept(SlotEquip.Back, back);
        if (chest != null) func.accept(SlotEquip.Chest, chest);
        if (wrist != null) func.accept(SlotEquip.Wrist, wrist);
        if (hand != null) func.accept(SlotEquip.Hand, hand);
        if (belt != null) func.accept(SlotEquip.Belt, belt);
        if (leg != null) func.accept(SlotEquip.Leg, leg);
        if (foot != null) func.accept(SlotEquip.Foot, foot);
        if (ring1 != null) func.accept(SlotEquip.Ring1, ring1);
        if (ring2 != null) func.accept(SlotEquip.Ring2, ring2);
        if (trinket1 != null) func.accept(SlotEquip.Trinket1, trinket1);
        if (trinket2 != null) func.accept(SlotEquip.Trinket2, trinket2);
        if (weapon != null) func.accept(SlotEquip.Weapon, weapon);
        if (offhand != null) func.accept(SlotEquip.Offhand, offhand);
    }

    public ItemData getHead() {
        return head;
    }

    public ItemData getNeck() {
        return neck;
    }

    public ItemData getShoulder() {
        return shoulder;
    }

    public ItemData getBack() {
        return back;
    }

    public ItemData getChest() {
        return chest;
    }

    public ItemData getWrist() {
        return wrist;
    }

    public ItemData getHand() {
        return hand;
    }

    public ItemData getBelt() {
        return belt;
    }

    public ItemData getLeg() {
        return leg;
    }

    public ItemData getFoot() {
        return foot;
    }

    public ItemData getRing1() {
        return ring1;
    }

    public ItemData getRing2() {
        return ring2;
    }

    public ItemData getTrinket1() {
        return trinket1;
    }

    public ItemData getTrinket2() {
        return trinket2;
    }

    public ItemData getWeapon() {
        return weapon;
    }

    public ItemData getOffhand() {
        return offhand;
    }
}
