package au.nerago.mopgear.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record ItemShared(@NotNull ItemRef ref, @NotNull SlotItem slot, @NotNull String name, @NotNull SocketType[] socketSlots, @Nullable StatBlock socketBonus) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equalsTyped((ItemShared) o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, slot);
    }

    public boolean equalsTyped(ItemShared other) {
        return ref.equalsTyped(other.ref) && slot == other.slot;
    }
}
