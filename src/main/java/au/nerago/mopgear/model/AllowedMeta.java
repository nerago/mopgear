package au.nerago.mopgear.model;

public enum AllowedMeta {
    None(-1),
    Tank(95344),
    Melee(95346);

    public final int itemId;

    AllowedMeta(int itemId) {
        this.itemId = itemId;
    }
}
