package au.nerago.mopgear.domain;

public record EquippedItem(int itemId, int[] gems, Integer enchant, int upgradeStep, int reforging) {
}
