package au.nerago.mopgear.domain;

import au.nerago.mopgear.util.CurryQueue;

import java.util.Objects;

public record SkinnyItemSet(int totalOne, int totalTwo, int totalThree, CurryQueue<SkinnyItem> items) {
    public static SkinnyItemSet single(SkinnyItem item) {
        return new SkinnyItemSet(item.one(), item.two(), item.three(), CurryQueue.single(item));
    }

    public SkinnyItemSet withAddedItem(SkinnyItem item) {
        return new SkinnyItemSet(
                this.totalOne + item.one(),
                this.totalTwo + item.two(),
                this.totalThree + item.three(),
                this.items.prepend(item));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkinnyItemSet skinnySet = (SkinnyItemSet) o;
        return totalOne == skinnySet.totalOne && totalTwo == skinnySet.totalTwo && totalThree == skinnySet.totalThree;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalOne, totalTwo, totalThree, items);
    }
}
