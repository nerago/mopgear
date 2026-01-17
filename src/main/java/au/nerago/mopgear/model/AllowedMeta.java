package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.GemInfo;
import au.nerago.mopgear.domain.SpecType;
import au.nerago.mopgear.domain.StatBlock;

import static au.nerago.mopgear.domain.StatType.*;

public enum AllowedMeta {
    None(-1, null),
    Tank(95344, StatBlock.of(Stam, 324)),
    Melee(95346, StatBlock.of(Crit, 324)),
    Heal(95345, StatBlock.of(Primary, 324)),
    Caster(95347, StatBlock.of(Crit, 324));

    public final int gemId;
    public final StatBlock statBlock;

    AllowedMeta(int gemId, StatBlock statBlock) {
        this.gemId = gemId;
        this.statBlock = statBlock;
    }

    public static AllowedMeta forSpec(SpecType spec) {
        switch (spec) {
            case PaladinProtMitigation, WarriorProt -> {
                return Tank;
            }
            case PaladinProtDps, PaladinRet, WarriorArms -> {
                return Melee;
            }
            default -> throw new IllegalArgumentException("missing gem choice for " + spec);
        }
    }

    public static AllowedMeta forId(int gemId) {
        return switch (gemId) {
            case 95344 -> Tank;
            case 95346 -> Melee;
            case 95345 -> Heal;
            case 95347 -> Caster;
            default -> null;
        };
    }

    public GemInfo asGemInfo() {
        return new GemInfo(gemId, statBlock);
    }
}
