package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemSharedCache {
    private static final Map<ItemRef, List<ItemShared>> map = new HashMap<>();

    public static ItemShared get(@NotNull ItemRef ref, @NotNull SlotItem slot, @NotNull String name,
                                 @NotNull PrimaryStatType primaryStatType, @NotNull ArmorType armorType,
                                 @NotNull SocketType[] socketSlots, @Nullable StatBlock socketBonus, int phase) {
        List<ItemShared> options = map.computeIfAbsent(ref, k -> new ArrayList<>());

        // TODO are multiple even valid?
        for (ItemShared share : options) {
            if (equalsShare(share, slot, name, primaryStatType, armorType, socketSlots, socketBonus)) {
                return share;
            }
        }

        ItemShared result = new ItemShared(ref, slot, name, primaryStatType, armorType, socketSlots, socketBonus, phase);
        options.add(result);
        return result;
    }

    public static ItemShared get(ItemRef ref, ItemShared old) {
        return get(ref, old.slot(), old.name(), old.primaryStatType(), old.armorType(), old.socketSlots(), old.socketBonus(), old.phase());
    }

    private static boolean equalsShare(ItemShared share, SlotItem slot, String name, PrimaryStatType primaryStatType, ArmorType armorType, SocketType[] socketSlots, StatBlock socketBonus) {
        return share.slot() == slot &&
                share.name().equals(name) &&
                share.primaryStatType() == primaryStatType &&
                share.armorType() == armorType &&
                Arrays.equals(share.socketSlots(), socketSlots) &&
                Objects.equals(share.socketBonus(), socketBonus);
    }
}
