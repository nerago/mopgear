package au.nerago.mopgear.domain;

import java.util.Arrays;
import java.util.Objects;

public record EquippedItem(int itemId, int[] gems, Integer enchant, int upgradeStep, int reforging,
                           Integer randomSuffix) {
    @Override
    public boolean equals(Object o) {
        if (o instanceof EquippedItem(int otherItemId, int[] otherGems, Integer otherEnchant, int otherStep, int otherReforge,
                                      Integer otherRandomSuffix
        )) {
            return itemId == otherItemId && reforging == otherReforge && upgradeStep == otherStep && Objects.deepEquals(gems, otherGems) &&
                    Objects.equals(enchant, otherEnchant) && Objects.equals(randomSuffix, otherRandomSuffix);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, Arrays.hashCode(gems), enchant, upgradeStep, reforging, randomSuffix);
    }

    public EquippedItem changeUpgradeLevel(int changeUpgradeLevel) {
        return new EquippedItem(itemId, gems, enchant, changeUpgradeLevel, reforging, randomSuffix);
    }
}
