package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.SlotEquip;

public interface IEquipMap {
    IItem get(SlotEquip slot);
}
