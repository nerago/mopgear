package au.nerago.mopgear.util;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class SpliteratorIterateBigInteger implements Spliterator<BigInteger> {
    private static final BigInteger FOUR = BigInteger.valueOf(4);

    private BigInteger index;
    private final BigInteger lastIndex;
    private final BigInteger skip;

    public SpliteratorIterateBigInteger(BigInteger start, BigInteger max, BigInteger skip) {
        this.index = start;
        this.lastIndex = max.subtract(BigInteger.ONE);
        this.skip = skip;
    }

    @Override
    public Spliterator<BigInteger> trySplit() {
        BigInteger size = estimateSizeBig();
        if (size.compareTo(FOUR) < 0)
            return null;

        BigInteger skipsUntilHalf = size.divide(BigInteger.TWO);
        BigInteger half = index.add(skip.multiply(skipsUntilHalf));

        SpliteratorIterateBigInteger prefix = new SpliteratorIterateBigInteger(index, half, skip);
        this.index = half;

//        System.out.println("SpliteratorIterateBigInteger.trySplit {"
//                + prefix.index + " " + prefix.lastIndex + " #" + prefix.estimateSizeBig() + "} {"
//                + this.index + " " + this.lastIndex + " #" + this.estimateSizeBig() + "}");

        return prefix;
    }

    @Override
    public boolean tryAdvance(Consumer<? super BigInteger> action) {
        if (index.compareTo(lastIndex) <= 0) {
            action.accept(index);
            index = index.add(skip);
            return true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super BigInteger> action) {
        while (index.compareTo(lastIndex) <= 0) {
            action.accept(index);
            index = index.add(skip);
        }
    }

    private BigInteger estimateSizeBig() {
        return lastIndex.subtract(index).divide(skip).add(BigInteger.ONE);
    }

    @Override
    public long estimateSize() {
        BigInteger size = estimateSizeBig();
        if (BigStreamUtil.fitsMaxLong(size)) {
            return size.longValueExact();
        } else {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.IMMUTABLE;
    }

    @Override
    public Comparator<? super BigInteger> getComparator() {
        return null;
    }
}
