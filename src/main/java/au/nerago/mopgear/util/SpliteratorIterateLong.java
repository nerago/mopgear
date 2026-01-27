package au.nerago.mopgear.util;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.LongConsumer;

public class SpliteratorIterateLong implements Spliterator.OfLong {
    private long index;
    private final long lastIndex;
    private final long skip;

    public SpliteratorIterateLong(long start, long max, long skip) {
        this.index = start;
        this.lastIndex = max - 1;
        this.skip = skip;
    }

    @Override
    public OfLong trySplit() {
        long size = estimateSize();
        if (size < 4)
            return null;

        long skipsUntilHalf = size / 2;
        long half = index + (skip * skipsUntilHalf);

        OfLong prefix = new SpliteratorIterateLong(index, half, skip);
        this.index = half;
        return prefix;
    }

    @Override
    public boolean tryAdvance(LongConsumer action) {
        if (index <= lastIndex) {
            action.accept(index);
            index += skip;
            return true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(LongConsumer action) {
        while (index <= lastIndex) {
            action.accept(index);
            index += skip;
        }
    }

    @Override
    public long estimateSize() {
        return ((lastIndex - index) / skip) + 1;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.IMMUTABLE;
    }

    @Override
    public Comparator<? super Long> getComparator() {
        return null;
    }
}
