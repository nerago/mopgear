package au.nerago.mopgear.domain;

import java.util.Objects;

public record SkinnyItem(SlotEquip slot, int one, int two, int three) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkinnyItem that = (SkinnyItem) o;
        return one == that.one && two == that.two && three == that.three && slot == that.slot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, one, two, three);
    }
}
