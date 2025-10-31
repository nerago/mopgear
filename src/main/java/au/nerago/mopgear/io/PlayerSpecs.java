package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.LogItemInfo;
import au.nerago.mopgear.domain.SpecType;
import au.nerago.mopgear.model.SetBonus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerSpecs {
    private static final Map<String, List<SpecType>> playerSpecs = makePlayerSpecs();

    private static Map<String, List<SpecType>> makePlayerSpecs() {
        Map<String, List<SpecType>> map = new HashMap<>();
        map.put("Ahakkua", List.of(SpecType.DeathKnightBlood));
        map.put("Badlamp", List.of(SpecType.WarlockDestruction));
        map.put("Ballgazer", List.of(SpecType.MonkBrewmaster));
        map.put("Bigcaluron", List.of(SpecType.ShamanRestoration));
        map.put("Calurolly", List.of(SpecType.MonkBrewmaster));
        map.put("Hesiana", List.of(SpecType.WarlockDestruction));
        map.put("Holydooly", List.of(SpecType.PaladinRet));
        map.put("Iniles", List.of(SpecType.PaladinHoly));
        map.put("Katsu", List.of(SpecType.ShamanElemental));
        map.put("Knackerstars", List.of(SpecType.ShadowPriest));
        map.put("Komui", List.of(SpecType.RogueUnknown));
        map.put("Neravi", List.of(SpecType.PaladinProtDps, SpecType.PaladinProtMitigation, SpecType.PaladinRet));
        map.put("Oogabooguhh", List.of(SpecType.WarriorProt, SpecType.WarriorArms));
        map.put("Ragnoroth", List.of(SpecType.Hunter));
        map.put("Rycidious", List.of(SpecType.Mage));
        map.put("Talnichi", List.of(SpecType.PriestHoly));
        map.put("Talzan", List.of(SpecType.DruidBoom, SpecType.DruidTree));
        map.put("Viiolate", List.of(SpecType.Hunter));
        map.put("Viioodruid", List.of(SpecType.DruidBoom, SpecType.DruidTree));
        map.put("Xanetio", List.of(SpecType.Hunter));
        map.put("Xanshu", List.of(SpecType.MonkMistweaver));
        return map;
    }

    public static SpecType findSpec(String playerName, List<LogItemInfo> itemInfoList) {
        List<SpecType> knownSpecs = playerSpecs.get(playerName);
        SpecType gearSpec = SetBonus.forGear(itemInfoList);
        if (gearSpec == null)
            throw new RuntimeException("gear set unknown for " + playerName);
        else if (knownSpecs.contains(gearSpec))
            throw new RuntimeException("unexpected gear set " + gearSpec + " for " + playerName);
        return gearSpec;
    }
}
