package au.nerago.mopgear.domain;

import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.Tuple;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public record SolvableEquipOptionsMap(SolvableItem[] head,
                                      SolvableItem[] neck,
                                      SolvableItem[] shoulder,
                                      SolvableItem[] back,
                                      SolvableItem[] chest,
                                      SolvableItem[] wrist,
                                      SolvableItem[] hand,
                                      SolvableItem[] belt,
                                      SolvableItem[] leg,
                                      SolvableItem[] foot,
                                      SolvableItem[] ring1,
                                      SolvableItem[] ring2,
                                      SolvableItem[] trinket1,
                                      SolvableItem[] trinket2,
                                      SolvableItem[] weapon,
                                      SolvableItem[] offhand) {


    public SolvableEquipOptionsMap(EquipOptionsMap other) {
        this(copyAndCast(other.head),
                copyAndCast(other.neck),
                copyAndCast(other.shoulder),
                copyAndCast(other.back),
                copyAndCast(other.chest),
                copyAndCast(other.wrist),
                copyAndCast(other.hand),
                copyAndCast(other.belt),
                copyAndCast(other.leg),
                copyAndCast(other.foot),
                copyAndCast(other.ring1),
                copyAndCast(other.ring2),
                copyAndCast(other.trinket1),
                copyAndCast(other.trinket2),
                copyAndCast(other.weapon),
                copyAndCast(other.offhand));
    }

    private static SolvableItem[] copyAndCast(ItemData[] array) {
        if (array == null)
            return null;
        return ArrayUtil.mapAsNew(array, SolvableItem::of, SolvableItem[]::new);
    }

    public SolvableItem[] get(SlotEquip slot) {
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

    public void forEachValue(Consumer<SolvableItem[]> func) {
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

    public void forEachPair(BiConsumer<SlotEquip, SolvableItem[]> func) {
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

    public Stream<Tuple.Tuple2<SlotEquip, SolvableItem[]>> entryStream() {
        return StreamSupport.stream(new OptionsSpliterator(), true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolvableEquipOptionsMap that = (SolvableEquipOptionsMap) o;
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

    private class OptionsSpliterator implements Spliterator<Tuple.Tuple2<SlotEquip, SolvableItem[]>> {
        static final SlotEquip[] slotArray = SlotEquip.values();
        int index = 0;

        @Override
        public boolean tryAdvance(Consumer<? super Tuple.Tuple2<SlotEquip, SolvableItem[]>> action) {
            while (index < slotArray.length) {
                SlotEquip slot = slotArray[index++];
                SolvableItem[] value = SolvableEquipOptionsMap.this.get(slot);
                if (value != null) {
                    action.accept(Tuple.create(slot, value));
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<Tuple.Tuple2<SlotEquip, SolvableItem[]>> trySplit() {
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
