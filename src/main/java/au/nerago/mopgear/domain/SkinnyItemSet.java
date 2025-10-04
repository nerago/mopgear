package au.nerago.mopgear.domain;

import au.nerago.mopgear.util.CurryQueue;

import java.util.Objects;

public record SkinnyItemSet(int totalOne, int totalTwo, CurryQueue<SkinnyItem> items) {
    public static SkinnyItemSet single(SkinnyItem item) {
        return new SkinnyItemSet(item.one(), item.two(), CurryQueue.single(item));
    }

    public SkinnyItemSet withAddedItem(SkinnyItem item) {
        return new SkinnyItemSet(
                this.totalOne + item.one(),
                this.totalTwo + item.two(),
                this.items.prepend(item));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkinnyItemSet skinnySet = (SkinnyItemSet) o;
        return totalOne == skinnySet.totalOne && totalTwo == skinnySet.totalTwo;
    }

    @Override
    public int hashCode() {
//            return Objects.hash(totalOne, totalTwo, items);
        return Objects.hash(totalOne, totalTwo);
    }
}
