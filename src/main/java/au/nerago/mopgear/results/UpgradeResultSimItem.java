package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.FullItemData;
import au.nerago.mopgear.domain.SlotEquip;
import au.nerago.mopgear.io.SimOutputReader;

public record UpgradeResultSimItem(FullItemData item, SlotEquip slot, au.nerago.mopgear.domain.FullItemSet itemSet,
                                   SimOutputReader.SimResultStats simResult) {
}
