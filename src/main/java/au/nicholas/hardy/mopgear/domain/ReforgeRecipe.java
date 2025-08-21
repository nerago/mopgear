package au.nicholas.hardy.mopgear.domain;

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

    @Override
    public int hashCode() {
        return Objects.hash(source, dest);
    }
}
