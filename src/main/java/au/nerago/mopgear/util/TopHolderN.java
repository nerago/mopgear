package au.nerago.mopgear.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.function.ToLongFunction;

public class TopHolderN<T> {
    private final int size;
    private final ToLongFunction<T> getValue;
    private final TreeSet<T> sortedSet;
    private boolean isFull;
    private double worstValue;

    public TopHolderN(int size, ToLongFunction<T> getValue) {
        this.size = size;
        this.getValue = getValue;
        this.sortedSet = new TreeSet<>(Comparator.comparingLong(getValue));
    }

    public void add(T add) {
        double addValue = getValue.applyAsLong(add);

        if (worstValue == 0.0) {
            sortedSet.add(add);
            worstValue = addValue;
        } else if (addValue <= worstValue && !isFull) {
            sortedSet.add(add);
            worstValue = getValue.applyAsLong(sortedSet.getFirst());
            isFull = sortedSet.size() == size;
        } else if (addValue > worstValue || !isFull) {
            sortedSet.add(add);
            trim();
        }
    }

    private void trim() {
        if (sortedSet.size() > size) {
            do {
                sortedSet.removeFirst();
            } while (sortedSet.size() > size);
            worstValue = getValue.applyAsLong(sortedSet.getFirst());
            isFull = true;
        }
    }

    public Collection<T> result() {
        return sortedSet;
    }
}
