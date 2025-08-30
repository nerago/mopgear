package au.nicholas.hardy.mopgear.util;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

public class BottomNFilter<T> implements Predicate<T> {
    private final int size;
    private final ToLongFunction<T> getValue;
    private final NavigableSet<T> sortedSet;
    private final AtomicLong highestValueAtomic;
    private boolean isFull;


    public BottomNFilter(int size, ToLongFunction<T> getValue) {
        this.size = size;
        this.getValue = getValue;
        this.sortedSet = new ConcurrentSkipListSet<>(Comparator.comparingLong(getValue));
        this.highestValueAtomic = new AtomicLong(0);
    }

    @Override
    public boolean test(T add) {
        long addValue = getValue.applyAsLong(add);
        while (true) {
            final long priorHighestValue = highestValueAtomic.getAcquire();

            if (priorHighestValue == 0) {
                if (trySetAtomicHighest(priorHighestValue, addValue)) {
                    sortedSet.add(add);
                    return true;
                }
            } else if (addValue >= priorHighestValue && !isFull) {
                if (trySetAtomicHighest(priorHighestValue, addValue)) {
                    sortedSet.add(add);
                    isFull = sortedSet.size() == size;
                    return true;
                }
            } else if (addValue < priorHighestValue) {
                sortedSet.add(add);
                trim();
                try {
                    long newHighest = getValue.applyAsLong(sortedSet.getLast());
                    if (trySetAtomicHighest(priorHighestValue, newHighest))
                        return true;
                } catch (NoSuchElementException e) {
                    // continue;
                }
            } else {
                return false;
            }
        }
    }

    private boolean trySetAtomicHighest(long prior, long replacement) {
        return highestValueAtomic.compareAndExchangeRelease(prior, replacement) == prior;
    }

    private void trim() {
        if (sortedSet.size() > size) {
            do {
                sortedSet.removeLast();
            } while (sortedSet.size() > size);
            isFull = true;
        }
    }
}
