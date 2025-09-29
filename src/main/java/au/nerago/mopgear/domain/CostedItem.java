package au.nerago.mopgear.domain;

import java.util.Objects;

public record CostedItem(int itemId, int cost) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CostedItem that = (CostedItem) o;
        return itemId == that.itemId && cost == that.cost;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, cost);
    }
}
