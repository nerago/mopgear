package au.nerago.mopgear.domain;

import java.util.Arrays;
import java.util.OptionalInt;

public record LogItemInfo(int itemId, int itemLevel, OptionalInt enchantId, int[] gems) {
    @Override
    public String toString() {
        return "LogItemInfo{" + "itemId=" + itemId +
                ", itemLevel=" + itemLevel +
                ", enchantId=" + enchantId +
                ", gems=" + Arrays.toString(gems) +
                '}';
    }
}
