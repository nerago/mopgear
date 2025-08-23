package au.nicholas.hardy.mopgear.results;

import au.nicholas.hardy.mopgear.domain.ItemData;

public record UpgradeResultItem(ItemData item, double factor, boolean hacked) {
}
