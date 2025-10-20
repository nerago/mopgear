package au.nerago.mopgear.domain;

import au.nerago.mopgear.model.IEquipMap;
import au.nerago.mopgear.util.Tuple;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public final class SolvableEquipMap implements IEquipMap {
    SolvableItem head;
    SolvableItem neck;
    SolvableItem shoulder;
    SolvableItem back;
    SolvableItem chest;
    SolvableItem wrist;
    SolvableItem hand;
    SolvableItem belt;
    SolvableItem leg;
    SolvableItem foot;
    SolvableItem ring1;
    SolvableItem ring2;
    SolvableItem trinket1;
    SolvableItem trinket2;
    SolvableItem weapon;
    SolvableItem offhand;

    public static SolvableEquipMap empty() {
        return new SolvableEquipMap();
    }

    public static SolvableEquipMap single(SlotEquip slot, SolvableItem item) {
        SolvableEquipMap map = new SolvableEquipMap();
        map.put(slot, item);
        return map;
    }

    private SolvableEquipMap() {
    }

    private SolvableEquipMap(SolvableEquipMap other) {
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

    public SolvableItem get(SlotEquip slot) {
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

    public void put(SlotEquip slot, SolvableItem value) {
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
    public SolvableEquipMap shallowClone() {
        return new SolvableEquipMap(this);
    }

    public SolvableEquipMap copyWithReplace(SlotEquip slot, SolvableItem replace) {
        SolvableEquipMap other = new SolvableEquipMap(this);
        other.put(slot, replace);
        return other;
    }

    public void forEachValue(Consumer<SolvableItem> func) {
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

    public void forEachPair(BiConsumer<SlotEquip, SolvableItem> func) {
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

    public SolvableItem getHead() {
        return head;
    }

    public SolvableItem getNeck() {
        return neck;
    }

    public SolvableItem getShoulder() {
        return shoulder;
    }

    public SolvableItem getBack() {
        return back;
    }

    public SolvableItem getChest() {
        return chest;
    }

    public SolvableItem getWrist() {
        return wrist;
    }

    public SolvableItem getHand() {
        return hand;
    }

    public SolvableItem getBelt() {
        return belt;
    }

    public SolvableItem getLeg() {
        return leg;
    }

    public SolvableItem getFoot() {
        return foot;
    }

    public SolvableItem getRing1() {
        return ring1;
    }

    public SolvableItem getRing2() {
        return ring2;
    }

    public SolvableItem getTrinket1() {
        return trinket1;
    }

    public SolvableItem getTrinket2() {
        return trinket2;
    }

    public SolvableItem getWeapon() {
        return weapon;
    }

    public SolvableItem getOffhand() {
        return offhand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolvableEquipMap equipMap = (SolvableEquipMap) o;
        return Objects.equals(head, equipMap.head) && Objects.equals(neck, equipMap.neck) && Objects.equals(shoulder, equipMap.shoulder) && Objects.equals(back, equipMap.back) && Objects.equals(chest, equipMap.chest) && Objects.equals(wrist, equipMap.wrist) && Objects.equals(hand, equipMap.hand) && Objects.equals(belt, equipMap.belt) && Objects.equals(leg, equipMap.leg) && Objects.equals(foot, equipMap.foot) && Objects.equals(ring1, equipMap.ring1) && Objects.equals(ring2, equipMap.ring2) && Objects.equals(trinket1, equipMap.trinket1) && Objects.equals(trinket2, equipMap.trinket2) && Objects.equals(weapon, equipMap.weapon) && Objects.equals(offhand, equipMap.offhand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, neck, shoulder, back, chest, wrist, hand, belt, leg, foot, ring1, ring2, trinket1, trinket2, weapon, offhand);
    }

    public Stream<Tuple.Tuple2<SlotEquip, SolvableItem>> entryStream() {
        return StreamSupport.stream(new EntrySpliterator(), true);
    }

    public Stream<SolvableItem> itemStream() {
        return StreamSupport.stream(new ItemSpliterator(), true);
    }

    private class EntrySpliterator implements Spliterator<Tuple.Tuple2<SlotEquip, SolvableItem>> {
        static final SlotEquip[] slotArray = SlotEquip.values();
        int index = 0;

        @Override
        public boolean tryAdvance(Consumer<? super Tuple.Tuple2<SlotEquip, SolvableItem>> action) {
            while (index < slotArray.length) {
                SlotEquip slot = slotArray[index++];
                SolvableItem value = SolvableEquipMap.this.get(slot);
                if (value != null) {
                    action.accept(Tuple.create(slot, value));
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<Tuple.Tuple2<SlotEquip, SolvableItem>> trySplit() {
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

    private class ItemSpliterator implements Spliterator<SolvableItem> {
        static final SlotEquip[] slotArray = SlotEquip.values();
        int index = 0;

        @Override
        public boolean tryAdvance(Consumer<? super SolvableItem> action) {
            while (index < slotArray.length) {
                SlotEquip slot = slotArray[index++];
                SolvableItem value = SolvableEquipMap.this.get(slot);
                if (value != null) {
                    action.accept(value);
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<SolvableItem> trySplit() {
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
