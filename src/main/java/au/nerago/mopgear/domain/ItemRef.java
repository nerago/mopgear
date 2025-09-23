package au.nerago.mopgear.domain;

import java.util.Objects;

public record ItemRef(int itemId, int itemLevel, int duplicateNum) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemRef itemRef = (ItemRef) o;
        return equalsTyped(itemRef);
    }

    public boolean equalsTyped(ItemRef itemRef) {
        return itemId == itemRef.itemId && itemLevel == itemRef.itemLevel && duplicateNum == itemRef.duplicateNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, itemLevel, duplicateNum);
    }
}
