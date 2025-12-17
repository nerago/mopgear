package au.nerago.mopgear;

public enum Difficulty {
    Celestial(502), Normal(522), Heroic(535);

    public final int itemLevel;

    Difficulty(int itemLevel) {
        this.itemLevel = itemLevel;
    }
}
