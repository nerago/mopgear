package au.nicholas.hardy.mopgear;

public class DefaultEnchants {
    private final SpecType spec;

    public DefaultEnchants(SpecType spec) {
        this.spec = spec;
    }

    public StatBlock standardEnchant(SlotItem slot) {
        if (spec == SpecType.PaladinRet) {
            switch (slot) {
                case Shoulder -> {
                    return new StatBlock(200, 0, 0, 100, 0, 0, 0, 0, 0, 0);
                }
                case Back -> {
                    return new StatBlock(0, 0, 0, 0, 180, 0, 0, 0, 0, 0);
                }
                case Chest -> {
                    return new StatBlock(80, 80, 0, 0, 0, 0, 0, 0, 0, 0);
                }
                case Wrist -> {
                    return new StatBlock(0, 0, 170, 0, 0, 0, 0, 0, 0, 0);
                }
                case Hand -> {
                    return new StatBlock(170, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                }
                case Leg -> {
                    return new StatBlock(285, 0, 0, 165, 0, 0, 0, 0, 0, 0);
                }
                case Foot -> {
                    return new StatBlock(0, 0, 0, 0, 0, 175, 0, 0, 0, 0);
                }
                default -> {
                    return null;
                }
            }
        } else if (spec == SpecType.PaladinProt) {
            switch (slot) {
                case Shoulder -> {
                    return new StatBlock(0, 300, 0, 0, 0, 0, 0, 100, 0, 0);
                }
                case Back -> {
                    return new StatBlock(0, 200, 0, 0, 0, 0, 0, 0, 0, 0);
                }
                case Chest -> {
                    return new StatBlock(0, 300, 0, 0, 0, 0, 0, 0, 0, 0);
                }
                case Wrist -> {
                    return new StatBlock(0, 0, 170, 0, 0, 0, 0, 0, 0, 0);
                }
                case Hand -> {
                    return new StatBlock(0, 0, 0, 0, 0, 0, 170, 0, 0, 0);
                }
                case Leg -> {
                    return new StatBlock(0, 430, 0, 0, 0, 0, 0, 165, 0, 0);
                }
                case Foot -> {
                    return new StatBlock(0, 0, 0, 0, 175, 0, 0, 0, 0, 0);
                }
                case Offhand -> {
                    return new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 175, 0);
                }
                default -> {
                    return null;
                }
            }
        } else {
            throw new IllegalArgumentException("need enchants");
        }
    }
}
