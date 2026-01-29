package au.nerago.mopgear.util;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class SpliteratorIterateBigNum implements Spliterator<BigNum> {
    private BigNum index;
    private final BigNum lastIndex;
    private final BigNum skip;

    public SpliteratorIterateBigNum(BigNum start, BigNum max, BigNum skip) {
        this.index = start;
        this.lastIndex = max.subtract(BigNum.ONE);
        this.skip = skip;
    }

    @Override
    public Spliterator<BigNum> trySplit() {
        BigNum size = estimateSizeBig();
        if (size.compareTo(BigNum.FOUR) < 0)
            return null;

        BigNum skipsUntilHalf = size.divide(BigNum.TWO);
        BigNum half = index.add(skip.multiply(skipsUntilHalf));

        SpliteratorIterateBigNum prefix = new SpliteratorIterateBigNum(index, half, skip);
        this.index = half;

//        System.out.println("SpliteratorIterateBigNum.trySplit {"
//                + prefix.index + " " + prefix.lastIndex + " #" + prefix.estimateSizeBig() + "} {"
//                + this.index + " " + this.lastIndex + " #" + this.estimateSizeBig() + "}");

        return prefix;
    }

    @Override
    public boolean tryAdvance(Consumer<? super BigNum> action) {
        if (index.compareTo(lastIndex) <= 0) {
            action.accept(index);
            index = index.add(skip);
            return true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super BigNum> action) {
        while (index.compareTo(lastIndex) <= 0) {
            action.accept(index);
            index = index.add(skip);
        }
    }

    private BigNum estimateSizeBig() {
        return lastIndex.subtract(index).divide(skip).add(BigNum.ONE);
    }

    @Override
    public long estimateSize() {
        BigNum size = estimateSizeBig();
        if (size.fitsMaxLong()) {
            return size.longValue();
        } else {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.IMMUTABLE;
    }

    @Override
    public Comparator<? super BigNum> getComparator() {
        return null;
    }
}
