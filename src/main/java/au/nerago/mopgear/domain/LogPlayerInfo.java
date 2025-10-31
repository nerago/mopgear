package au.nerago.mopgear.domain;

import java.util.List;

public record LogPlayerInfo(String name, SpecType spec, List<LogItemInfo> itemList) {
}
