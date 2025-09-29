package au.nerago.mopgear.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public class BottomCollectorN<T> implements Collector<T, BottomCollectorN.State<T>, Collection<T>> {
    private final int size;
    private final ToLongFunction<T> getValue;

    public BottomCollectorN(int size, ToLongFunction<T> getValue) {
        this.size = size;
        this.getValue = getValue;
    }

    @Override
    public Supplier<State<T>> supplier() {
        return () -> new State<>(size, getValue);
    }

    @Override
    public BiConsumer<State<T>, T> accumulator() {
        return State::add;
    }

    @Override
    public BinaryOperator<State<T>> combiner() {
        return State::combine;
    }

    @Override
    public Function<State<T>, Collection<T>> finisher() {
        return State::finish;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED);
    }

    public static class State<T> {
        private final int size;
        private final ToLongFunction<T> getValue;
        private final TreeSet<T> sortedSet;
        private boolean isFull;
        private double highestValue;

        public State(int size, ToLongFunction<T> getValue) {
            this.size = size;
            this.getValue = getValue;
            this.sortedSet = new TreeSet<>(Comparator.comparingLong(getValue));
        }

        public void add(T add) {
            double addValue = getValue.applyAsLong(add);

            if (highestValue == 0.0) {
                sortedSet.add(add);
                highestValue = addValue;
            } else if (!isFull) {
                sortedSet.add(add);
                highestValue = getValue.applyAsLong(sortedSet.getLast());
                isFull = sortedSet.size() == size;
            } else if (addValue < highestValue) {
                sortedSet.add(add);
                trim();
            }
        }

        public State<T> combine(State<T> other) {
            sortedSet.addAll(other.sortedSet);
            trim();
            return this;
        }

        private void trim() {
            while (sortedSet.size() > size) {
                sortedSet.removeLast();
            }
            highestValue = getValue.applyAsLong(sortedSet.getLast());
        }

        public Collection<T> finish() {
            return sortedSet;
        }
    }
}
