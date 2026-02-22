package au.nerago.mopgear.domain;

import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.Tuple;

import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static au.nerago.mopgear.domain.SlotEquip.*;

@SuppressWarnings("unused")
public final class EquipMap {
    private static final int SLOT_COUNT = 16;
    final FullItemData[] array = new FullItemData[SLOT_COUNT];

    public static EquipMap empty() {
        return new EquipMap();
    }

    public static EquipMap single(SlotEquip slot, FullItemData item) {
        EquipMap map = new EquipMap();
        map.array[slot.ordinal()] = item;
        return map;
    }

    private EquipMap() {
    }

    private EquipMap(EquipMap other) {
        System.arraycopy(other.array, 0, this.array, 0, SLOT_COUNT);
    }

    public EquipMap(SolvableEquipMap other, Function<SolvableItem, FullItemData> itemConverter) {
        for (int i = 0; i < SLOT_COUNT; ++i)
            this.array[i] = itemConverter.apply(other.array[i]);
    }

    public FullItemData get(SlotEquip slot) {
        return array[slot.ordinal()];
    }

    public boolean has(SlotEquip slot) {
        return array[slot.ordinal()] != null;
    }

    public void put(SlotEquip slot, FullItemData value) {
        array[slot.ordinal()] = value;
    }

    @Deprecated(since = "avoid extra allocation")
    public EquipMap shallowClone() {
        return new EquipMap(this);
    }

    public EquipMap copyWithReplace(SlotEquip slot, FullItemData replace) {
        EquipMap other = new EquipMap(this);
        other.array[slot.ordinal()] = replace;
        return other;
    }

    @Deprecated
    public void forEachValue(Consumer<FullItemData> func) {
        for (FullItemData item : array) {
            if (item != null)
                func.accept(item);
        }
    }

    public void forEachValueIncludeNulls(Consumer<FullItemData> func) {
        for (FullItemData item : array) {
            func.accept(item);
        }
    }

    @Deprecated
    public void forEachPair(BiConsumer<SlotEquip, FullItemData> func) {
        SlotEquip[] enumValues = SlotEquip.values();
        for (int i = 0; i < SLOT_COUNT; ++i) {
            FullItemData item = array[i];
            if (item != null) {
                SlotEquip slot = enumValues[i];
                func.accept(slot, item);
            }
        }
    }

    public FullItemData getHead() {
        return array[Head_Ordinal];
    }

    public FullItemData getNeck() {
        return array[Neck_Ordinal];
    }

    public FullItemData getShoulder() {
        return array[Shoulder_Ordinal];
    }

    public FullItemData getBack() {
        return array[Back_Ordinal];
    }

    public FullItemData getChest() {
        return array[Chest_Ordinal];
    }

    public FullItemData getWrist() {
        return array[Wrist_Ordinal];
    }

    public FullItemData getHand() {
        return array[Hand_Ordinal];
    }

    public FullItemData getBelt() {
        return array[Belt_Ordinal];
    }

    public FullItemData getLeg() {
        return array[Leg_Ordinal];
    }

    public FullItemData getFoot() {
        return array[Foot_Ordinal];
    }

    public FullItemData getRing1() {
        return array[Ring1_Ordinal];
    }

    public FullItemData getRing2() {
        return array[Ring2_Ordinal];
    }

    public FullItemData getTrinket1() {
        return array[Trinket1_Ordinal];
    }

    public FullItemData getTrinket2() {
        return array[Trinket2_Ordinal];
    }

    public FullItemData getWeapon() {
        return array[Weapon_Ordinal];
    }

    public FullItemData getOffhand() {
        return array[Offhand_Ordinal];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equalsTyped((EquipMap) o);
    }

    public boolean equalsTyped(EquipMap other) {
        for (int i = 0; i < SLOT_COUNT; ++i) {
            if (!FullItemData.equalsNullSafe(this.array[i], other.array[i]))
                return false;
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean equalsTypedSwappable(EquipMap other) {
        for (int i = Head_Ordinal; i < Ring1_Ordinal; ++i) {
            if (!FullItemData.equalsNullSafe(this.array[i], other.array[i]))
                return false;
        }

        FullItemData ring1 = array[SlotEquip.Ring1_Ordinal], ring1o = other.array[SlotEquip.Ring1_Ordinal];
        FullItemData ring2 = array[SlotEquip.Ring2_Ordinal], ring2o = other.array[SlotEquip.Ring2_Ordinal];
        if (!(FullItemData.equalsNullSafe(ring1, ring1o) && FullItemData.equalsNullSafe(ring2, ring2o))
                && !(FullItemData.equalsNullSafe(ring1, ring2o) && FullItemData.equalsNullSafe(ring2, ring1o))) {
            return false;
        }

        FullItemData trinket1 = array[SlotEquip.Trinket1_Ordinal], trinket1o = other.array[SlotEquip.Trinket1_Ordinal];
        FullItemData trinket2 = array[SlotEquip.Trinket2_Ordinal], trinket2o = other.array[SlotEquip.Trinket2_Ordinal];
        if (!(FullItemData.equalsNullSafe(trinket1, trinket1o) && FullItemData.equalsNullSafe(trinket2, trinket2o))
                && !(FullItemData.equalsNullSafe(trinket1, trinket2o) && FullItemData.equalsNullSafe(trinket2, trinket1o))) {
            return false;
        }

        return FullItemData.equalsNullSafe(array[SlotEquip.Weapon_Ordinal], other.array[SlotEquip.Weapon_Ordinal]) 
                && FullItemData.equalsNullSafe(array[SlotEquip.Offhand_Ordinal], other.array[SlotEquip.Offhand_Ordinal]);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Deprecated
    public Stream<Tuple.Tuple2<SlotEquip, FullItemData>> entryStream() {
        return StreamSupport.stream(new ItemsSpliterator(), false);
    }

    public Stream<FullItemData> itemStream() {
        return Arrays.stream(array).filter(Objects::nonNull);
    }

    private class ItemsSpliterator implements Spliterator<Tuple.Tuple2<SlotEquip, FullItemData>> {
        static final SlotEquip[] slotArray = SlotEquip.values();
        int index = 0;

        @Override
        public boolean tryAdvance(Consumer<? super Tuple.Tuple2<SlotEquip, FullItemData>> action) {
            while (index < slotArray.length) {
                SlotEquip slot = slotArray[index];
                FullItemData value = array[index];
                index++;
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
