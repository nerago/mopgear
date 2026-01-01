package au.nerago.mopgear.domain;

import java.util.Arrays;
import java.util.Objects;

public record EquippedItem(int itemId, int[] gems, Integer enchant, int upgradeStep, int reforging) {
    @Override
    public boolean equals(Object o) {
        if (o instanceof EquippedItem(int otherItemId, int[] otherGems, Integer otherEnchant, int otherStep, int otherReforge)) {
            return itemId == otherItemId && reforging == otherReforge && upgradeStep == otherStep && Objects.deepEquals(gems, otherGems) && Objects.equals(enchant, otherEnchant);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, Arrays.hashCode(gems), enchant, upgradeStep, reforging);
    }
}
