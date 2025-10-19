package au.nerago.mopgear.domain;

import java.util.*;

public class ItemSharedCache {
    private static final Map<ItemRef, List<ItemShared>> map = new HashMap<>();

    public static ItemShared get(ItemRef ref, SlotItem slot, String name, SocketType[] socketSlots, StatBlock socketBonus) {
        List<ItemShared> options = map.computeIfAbsent(ref, k -> new ArrayList<>());

        // TODO are multiple even valid?
        for (ItemShared share : options) {
            if (equalsShare(share, slot, name, socketSlots, socketBonus)) {
                return share;
            }
        }

        ItemShared result = new ItemShared(ref, slot, name, socketSlots, socketBonus);
        options.add(result);
        return result;
    }

    public static ItemShared get(ItemRef ref, ItemShared old) {
        return get(ref, old.slot(), old.name(), old.socketSlots(), old.socketBonus());
    }

    private static boolean equalsShare(ItemShared share, SlotItem slot, String name, SocketType[] socketSlots, StatBlock socketBonus) {
        return share.slot() == slot &&
                share.name().equals(name) &&
                Arrays.equals(share.socketSlots(), socketSlots) &&
                Objects.equals(share.socketBonus(), socketBonus);
    }
}
