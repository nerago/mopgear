package au.nerago.mopgear.util;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

public class TopNFilter<T> implements Predicate<T> {
    private final int size;
    private final ToLongFunction<T> getValue;
    private final NavigableSet<T> sortedSet;
    private final AtomicLong worstValueAtomic;
    private boolean isFull;

    public TopNFilter(int size, ToLongFunction<T> getValue) {
        this.size = size;
        this.getValue = getValue;
        this.sortedSet = new ConcurrentSkipListSet<>(Comparator.comparingLong(getValue));
        this.worstValueAtomic = new AtomicLong(0);
    }

    @Override
    public boolean test(T add) {
        long addValue = getValue.applyAsLong(add);
        while (true) {
            final long priorWorstValue = worstValueAtomic.getAcquire();

            if (priorWorstValue == 0) {
                if (trySetAtomicWorst(priorWorstValue, addValue)) {
                    sortedSet.add(add);
                    return true;
                }
            } else if (addValue <= priorWorstValue && !isFull) {
                if (trySetAtomicWorst(priorWorstValue, addValue)) {
                    sortedSet.add(add);
                    isFull = sortedSet.size() == size;
                    return true;
                }
            } else if (addValue > priorWorstValue) {
                sortedSet.add(add);
                trim();
                try {
                    long newWorstValue = getValue.applyAsLong(sortedSet.getFirst());
                    if (trySetAtomicWorst(priorWorstValue, newWorstValue))
                        return true;
                } catch (NoSuchElementException e) {
                    // continue;
                }
            } else {
                return false;
            }
        }
    }

    private boolean trySetAtomicWorst(long prior, long replacement) {
        return worstValueAtomic.compareAndExchangeRelease(prior, replacement) == prior;
    }

    private void trim() {
        if (sortedSet.size() > size) {
            do {
                sortedSet.removeFirst();
            } while (sortedSet.size() > size);
            isFull = true;
        }
    }
}
