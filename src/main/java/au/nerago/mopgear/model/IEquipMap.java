package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.SolvableItem;
import au.nerago.mopgear.domain.SlotEquip;

public interface IEquipMap {
    SolvableItem get(SlotEquip slot);
}
