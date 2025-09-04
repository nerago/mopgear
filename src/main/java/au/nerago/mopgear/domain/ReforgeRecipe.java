package au.nerago.mopgear.domain;

import java.util.Objects;

public record ReforgeRecipe(StatType source, StatType dest) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReforgeRecipe that = (ReforgeRecipe) o;
        return equalsTyped(that);
    }

    public boolean equalsTyped(ReforgeRecipe that) {
        return that != null && source == that.source && dest == that.dest;
    }

    public static ReforgeRecipe empty() {
        return new ReforgeRecipe(null, null);
    }

    public boolean isNull() {
        return source == null && dest == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, dest);
    }
}
