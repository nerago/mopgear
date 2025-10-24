package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.FullItemData;

public record UpgradeResultItem(FullItemData item, double factor, int hackCount, int cost) {
}
