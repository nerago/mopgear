package au.nerago.mopgear.domain;

import au.nerago.mopgear.util.Tuple;
import au.nerago.mopgear.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class EquipOptionsMap {
    private ItemData[] head;
    private ItemData[] neck;
    private ItemData[] shoulder;
    private ItemData[] back;
    private ItemData[] chest;
    private ItemData[] wrist;
    private ItemData[] hand;
    private ItemData[] belt;
    private ItemData[] leg;
    private ItemData[] foot;
    private ItemData[] ring1;
    private ItemData[] ring2;
    private ItemData[] trinket1;
    private ItemData[] trinket2;
    private ItemData[] weapon;
    private ItemData[] offhand;

    public static EquipOptionsMap empty() {
        return new EquipOptionsMap();
    }

    private EquipOptionsMap() {
    }

    private EquipOptionsMap(EquipOptionsMap other) {
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

    private EquipOptionsMap(
            ItemData[] head, ItemData[] neck, ItemData[] shoulder, ItemData[] back, ItemData[] chest, ItemData[] wrist,
            ItemData[] hand, ItemData[] belt, ItemData[] leg, ItemData[] foot, ItemData[] ring1, ItemData[] ring2,
            ItemData[] trinket1, ItemData[] trinket2, ItemData[] weapon, ItemData[] offhand) {
        this.head = head;
        this.neck = neck;
        this.shoulder = shoulder;
        this.back = back;
        this.chest = chest;
        this.wrist = wrist;
        this.hand = hand;
        this.belt = belt;
        this.leg = leg;
        this.foot = foot;
        this.ring1 = ring1;
        this.ring2 = ring2;
        this.trinket1 = trinket1;
        this.trinket2 = trinket2;
        this.weapon = weapon;
        this.offhand = offhand;
    }

    public ItemData[] get(SlotEquip slot) {
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

    public void put(SlotEquip slot, ItemData[] value) {
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

    public void put(SlotEquip slot, ItemData item) {
        put(slot, new ItemData[] { item });
    }

    public void replaceWithFirstOption(SlotEquip slot) {
        ItemData[] array = get(slot);
        put(slot, array[0]);
    }

    public void replaceWithSpecificForge(SlotEquip slot, ReforgeRecipe reforgeRecipe) {
        ItemData[] array = get(slot);
        ItemData choice = ArrayUtil.findOne(array, item -> reforgeRecipe.equalsTyped(item.reforge));
        put(slot, choice);
    }

//    @Deprecated(since = "avoid extra allocation")
    public EquipOptionsMap shallowClone() {
        return new EquipOptionsMap(this);
    }

//    @Deprecated(since = "avoid extra allocation")
    public EquipOptionsMap deepClone() {
        return new EquipOptionsMap(
                ArrayUtil.clone(head),
                ArrayUtil.clone(neck),
                ArrayUtil.clone(shoulder),
                ArrayUtil.clone(back),
                ArrayUtil.clone(chest),
                ArrayUtil.clone(wrist),
                ArrayUtil.clone(hand),
                ArrayUtil.clone(belt),
                ArrayUtil.clone(leg),
                ArrayUtil.clone(foot),
                ArrayUtil.clone(ring1),
                ArrayUtil.clone(ring2),
                ArrayUtil.clone(trinket1),
                ArrayUtil.clone(trinket2),
                ArrayUtil.clone(weapon),
                ArrayUtil.clone(offhand)
        );
    }

    public EquipOptionsMap copyWithReplaceSingle(SlotEquip slot, ItemData replace) {
        EquipOptionsMap other = new EquipOptionsMap(this);
        other.put(slot, new ItemData[] { replace });
        return other;
    }

//    @Deprecated(since = "avoid bad performance")
    public void forEachValue(Consumer<ItemData[]> func) {
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

//    @Deprecated(since = "avoid bad performance")
    public void forEachPair(BiConsumer<SlotEquip, ItemData[]> func) {
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

    @Deprecated(since = "avoid bad performance")
    public List<Tuple.Tuple2<SlotEquip, ItemData[]>> entrySet() {
        ArrayList<Tuple.Tuple2<SlotEquip, ItemData[]>> list = new ArrayList<>();
        forEachPair((slot, array) -> list.add(Tuple.create(slot, array)));
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipOptionsMap that = (EquipOptionsMap) o;
        return Arrays.equals(head, that.head) && Arrays.equals(neck, that.neck) && Arrays.equals(shoulder, that.shoulder) && Arrays.equals(back, that.back) && Arrays.equals(chest, that.chest) && Arrays.equals(wrist, that.wrist) && Arrays.equals(hand, that.hand) && Arrays.equals(belt, that.belt) && Arrays.equals(leg, that.leg) && Arrays.equals(foot, that.foot) && Arrays.equals(ring1, that.ring1) && Arrays.equals(ring2, that.ring2) && Arrays.equals(trinket1, that.trinket1) && Arrays.equals(trinket2, that.trinket2) && Arrays.equals(weapon, that.weapon) && Arrays.equals(offhand, that.offhand);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(head);
        result = 31 * result + Arrays.hashCode(neck);
        result = 31 * result + Arrays.hashCode(shoulder);
        result = 31 * result + Arrays.hashCode(back);
        result = 31 * result + Arrays.hashCode(chest);
        result = 31 * result + Arrays.hashCode(wrist);
        result = 31 * result + Arrays.hashCode(hand);
        result = 31 * result + Arrays.hashCode(belt);
        result = 31 * result + Arrays.hashCode(leg);
        result = 31 * result + Arrays.hashCode(foot);
        result = 31 * result + Arrays.hashCode(ring1);
        result = 31 * result + Arrays.hashCode(ring2);
        result = 31 * result + Arrays.hashCode(trinket1);
        result = 31 * result + Arrays.hashCode(trinket2);
        result = 31 * result + Arrays.hashCode(weapon);
        result = 31 * result + Arrays.hashCode(offhand);
        return result;
    }
}
