package au.nerago.mopgear.domain;

import java.util.Objects;

public record ItemRef(int itemId, int itemLevel, int itemLevelBase, int duplicateNum) {
    public static ItemRef build(int id, int itemLevel) {
        return new ItemRef(id, itemLevel, itemLevel, 0);
    }

    public ItemRef changeItemLevel(int changedItemLevel) {
        return new ItemRef(itemId, changedItemLevel, itemLevel, duplicateNum);
    }

    public ItemRef changeDuplicate(int changedDuplicateNum) {
        return new ItemRef(itemId, itemLevel, itemLevelBase, changedDuplicateNum);
    }

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
