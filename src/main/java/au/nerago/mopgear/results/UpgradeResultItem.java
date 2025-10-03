package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.ItemData;

public record UpgradeResultItem(ItemData item, double factor, int hackCount, int cost) {
}
