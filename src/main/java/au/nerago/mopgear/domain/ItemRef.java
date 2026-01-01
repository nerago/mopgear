package au.nerago.mopgear.domain;

import au.nerago.mopgear.model.ItemLevel;

import java.util.Objects;

public record ItemRef(int itemId, int itemLevel, int itemLevelBase, int duplicateNum) {
    public static ItemRef buildBasic(int itemId, int itemLevel) {
        return new ItemRef(itemId, itemLevel, itemLevel, 0);
    }

    public static ItemRef buildAdvanced(int itemId, int itemLevel, int itemLevelBase) {
        return new ItemRef(itemId, itemLevel, itemLevelBase, 0);
    }

    public ItemRef changeItemLevel(int changedItemLevel) {
        return new ItemRef(itemId, changedItemLevel, itemLevel, duplicateNum);
    }

    public ItemRef changeDuplicate(int changedDuplicateNum) {
        return new ItemRef(itemId, itemLevel, itemLevelBase, changedDuplicateNum);
    }

    public int upgradeLevel() {
        if (itemLevelBase < ItemLevel.LOW_HIGH_MOP_ITEM_LEVELS_THRESHOLD)
            return (itemLevel - itemLevelBase) / ItemLevel.LOW_MOP_ITEM_LEVELS_PER_UPGRADE_LEVEL;
        else
            return (itemLevel - itemLevelBase) / ItemLevel.HIGH_MOP_ITEM_LEVELS_PER_UPGRADE_LEVEL;
    }

    public boolean thunderforged() {
        return itemLevelBase == 528 || itemLevelBase == 541;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemRef itemRef = (ItemRef) o;
        return equalsTyped(itemRef);
    }

    public boolean equalsTyped(ItemRef other) {
        return itemId == other.itemId && itemLevel == other.itemLevel && duplicateNum == other.duplicateNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, itemLevel, duplicateNum);
    }
}
