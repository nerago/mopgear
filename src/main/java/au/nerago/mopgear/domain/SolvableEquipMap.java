package au.nerago.mopgear.domain;

import au.nerago.mopgear.util.Tuple;
import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static au.nerago.mopgear.domain.SlotEquip.*;
import static au.nerago.mopgear.domain.SlotEquip.Back;
import static au.nerago.mopgear.domain.SlotEquip.Belt;
import static au.nerago.mopgear.domain.SlotEquip.Chest;
import static au.nerago.mopgear.domain.SlotEquip.Foot;
import static au.nerago.mopgear.domain.SlotEquip.Hand;
import static au.nerago.mopgear.domain.SlotEquip.Leg;
import static au.nerago.mopgear.domain.SlotEquip.Offhand;
import static au.nerago.mopgear.domain.SlotEquip.Ring1;
import static au.nerago.mopgear.domain.SlotEquip.Ring2;
import static au.nerago.mopgear.domain.SlotEquip.Trinket1;
import static au.nerago.mopgear.domain.SlotEquip.Trinket2;
import static au.nerago.mopgear.domain.SlotEquip.Weapon;
import static au.nerago.mopgear.domain.SlotEquip.Wrist;

@SuppressWarnings("unused")
public final class SolvableEquipMap {
    private static final int SLOT_COUNT = 16;
    final SolvableItem[] array = new SolvableItem[SLOT_COUNT];

    public static SolvableEquipMap empty() {
        return new SolvableEquipMap();
    }

    public static SolvableEquipMap single(SlotEquip slot, SolvableItem item) {
        SolvableEquipMap map = new SolvableEquipMap();
        map.array[slot.ordinal()] = item;
        return map;
    }

    private SolvableEquipMap() {
    }

    private SolvableEquipMap(SolvableEquipMap other) {
        System.arraycopy(other.array, 0, this.array, 0, SLOT_COUNT);
    }

    public SolvableItem get(SlotEquip slot) {
        return array[slot.ordinal()];
    }

    public boolean has(SlotEquip slot) {
        return array[slot.ordinal()] != null;
    }

    public void put(SlotEquip slot, SolvableItem value) {
        array[slot.ordinal()] = value;
    }

    @Deprecated(since = "avoid extra allocation")
    public SolvableEquipMap shallowClone() {
        return new SolvableEquipMap(this);
    }

    public SolvableEquipMap copyWithReplace(SlotEquip slot, SolvableItem replace) {
        SolvableEquipMap other = new SolvableEquipMap(this);
        other.array[slot.ordinal()] = replace;
        return other;
    }

    @Deprecated
    public void forEachValue(Consumer<SolvableItem> func) {
        for (SolvableItem item : array) {
            if (item != null)
                func.accept(item);
        }
    }

    @Deprecated
    public void forEachPair(BiConsumer<SlotEquip, SolvableItem> func) {
        SlotEquip[] enumValues = SlotEquip.values();
        for (int i = 0; i < SLOT_COUNT; ++i) {
            SolvableItem item = array[i];
            if (item != null) {
                SlotEquip slot = enumValues[i];
                func.accept(slot, item);
            }
        }
    }

    public SolvableItem getHead() {
        return array[Head_Ordinal];
    }

    public SolvableItem getNeck() {
        return array[Neck_Ordinal];
    }

    public SolvableItem getShoulder() {
        return array[Shoulder_Ordinal];
    }

    public SolvableItem getBack() {
        return array[Back_Ordinal];
    }

    public SolvableItem getChest() {
        return array[Chest_Ordinal];
    }

    public SolvableItem getWrist() {
        return array[Wrist_Ordinal];
    }

    public SolvableItem getHand() {
        return array[Hand_Ordinal];
    }

    public SolvableItem getBelt() {
        return array[Belt_Ordinal];
    }

    public SolvableItem getLeg() {
        return array[Leg_Ordinal];
    }

    public SolvableItem getFoot() {
        return array[Foot_Ordinal];
    }

    public SolvableItem getRing1() {
        return array[Ring1_Ordinal];
    }

    public SolvableItem getRing2() {
        return array[Ring2_Ordinal];
    }

    public SolvableItem getTrinket1() {
        return array[Trinket1_Ordinal];
    }

    public SolvableItem getTrinket2() {
        return array[Trinket2_Ordinal];
    }

    public SolvableItem getWeapon() {
        return array[Weapon_Ordinal];
    }

    public SolvableItem getOffhand() {
        return array[Offhand_Ordinal];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolvableEquipMap equipMap = (SolvableEquipMap) o;
        return equalsTyped(equipMap);
    }

    public boolean equalsTyped(SolvableEquipMap other) {
        for (int i = 0; i < SLOT_COUNT; ++i) {
            if (!SolvableItem.equalsNullSafe(this.array[i], other.array[i]))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Deprecated
    public Stream<Tuple.Tuple2<SlotEquip, SolvableItem>> entryStream() {
        return StreamSupport.stream(new EntrySpliterator(), true);
    }

    @Deprecated
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
