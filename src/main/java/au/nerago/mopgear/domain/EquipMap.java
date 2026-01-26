package au.nerago.mopgear.domain;

import au.nerago.mopgear.util.Tuple;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public final class EquipMap /*implements IEquipMap */ {
    private FullItemData head;
    private FullItemData neck;
    private FullItemData shoulder;
    private FullItemData back;
    private FullItemData chest;
    private FullItemData wrist;
    private FullItemData hand;
    private FullItemData belt;
    private FullItemData leg;
    private FullItemData foot;
    private FullItemData ring1;
    private FullItemData ring2;
    private FullItemData trinket1;
    private FullItemData trinket2;
    private FullItemData weapon;
    private FullItemData offhand;

    public static EquipMap empty() {
        return new EquipMap();
    }

    public static EquipMap single(SlotEquip slot, FullItemData item) {
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

    public EquipMap(SolvableEquipMap other, Function<SolvableItem, FullItemData> itemConverter) {
        this.head = itemConverter.apply(other.head);
        this.neck = itemConverter.apply(other.neck);
        this.shoulder = itemConverter.apply(other.shoulder);
        this.back = itemConverter.apply(other.back);
        this.chest = itemConverter.apply(other.chest);
        this.wrist = itemConverter.apply(other.wrist);
        this.hand = itemConverter.apply(other.hand);
        this.belt = itemConverter.apply(other.belt);
        this.leg = itemConverter.apply(other.leg);
        this.foot = itemConverter.apply(other.foot);
        this.ring1 = itemConverter.apply(other.ring1);
        this.ring2 = itemConverter.apply(other.ring2);
        this.trinket1 = itemConverter.apply(other.trinket1);
        this.trinket2 = itemConverter.apply(other.trinket2);
        this.weapon = itemConverter.apply(other.weapon);
        this.offhand = itemConverter.apply(other.offhand);
    }

    public FullItemData get(SlotEquip slot) {
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

    public void put(SlotEquip slot, FullItemData value) {
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

    public EquipMap copyWithReplace(SlotEquip slot, FullItemData replace) {
        EquipMap other = new EquipMap(this);
        other.put(slot, replace);
        return other;
    }

    public void forEachValue(Consumer<FullItemData> func) {
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

    public void forEachPair(BiConsumer<SlotEquip, FullItemData> func) {
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

    public FullItemData getHead() {
        return head;
    }

    public FullItemData getNeck() {
        return neck;
    }

    public FullItemData getShoulder() {
        return shoulder;
    }

    public FullItemData getBack() {
        return back;
    }

    public FullItemData getChest() {
        return chest;
    }

    public FullItemData getWrist() {
        return wrist;
    }

    public FullItemData getHand() {
        return hand;
    }

    public FullItemData getBelt() {
        return belt;
    }

    public FullItemData getLeg() {
        return leg;
    }

    public FullItemData getFoot() {
        return foot;
    }

    public FullItemData getRing1() {
        return ring1;
    }

    public FullItemData getRing2() {
        return ring2;
    }

    public FullItemData getTrinket1() {
        return trinket1;
    }

    public FullItemData getTrinket2() {
        return trinket2;
    }

    public FullItemData getWeapon() {
        return weapon;
    }

    public FullItemData getOffhand() {
        return offhand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equalsTyped((EquipMap) o);
    }

    public boolean equalsTyped(EquipMap o) {
        return Objects.equals(head, o.head) && Objects.equals(neck, o.neck) && Objects.equals(shoulder, o.shoulder) && Objects.equals(back, o.back) && Objects.equals(chest, o.chest) && Objects.equals(wrist, o.wrist) && Objects.equals(hand, o.hand) && Objects.equals(belt, o.belt) && Objects.equals(leg, o.leg) && Objects.equals(foot, o.foot) && Objects.equals(ring1, o.ring1) && Objects.equals(ring2, o.ring2) && Objects.equals(trinket1, o.trinket1) && Objects.equals(trinket2, o.trinket2) && Objects.equals(weapon, o.weapon) && Objects.equals(offhand, o.offhand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, neck, shoulder, back, chest, wrist, hand, belt, leg, foot, ring1, ring2, trinket1, trinket2, weapon, offhand);
    }

    public Stream<Tuple.Tuple2<SlotEquip, FullItemData>> entryStream() {
        return StreamSupport.stream(new ItemsSpliterator(), true);
    }

    private class ItemsSpliterator implements Spliterator<Tuple.Tuple2<SlotEquip, FullItemData>> {
        static final SlotEquip[] slotArray = SlotEquip.values();
        int index = 0;

        @Override
        public boolean tryAdvance(Consumer<? super Tuple.Tuple2<SlotEquip, FullItemData>> action) {
            while (index < slotArray.length) {
                SlotEquip slot = slotArray[index++];
                FullItemData value = EquipMap.this.get(slot);
                if (value != null) {
                    action.accept(Tuple.create(slot, value));
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<Tuple.Tuple2<SlotEquip, FullItemData>> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 16; // might be lower, but good enough
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
        }
    }
}
