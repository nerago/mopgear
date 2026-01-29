package au.nerago.mopgear.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public class TopCollectorN<T> implements Collector<T, TopCollectorN.State<T>, Collection<T>> {
    private final int size;
    private final ToLongFunction<T> getValue;

    public TopCollectorN(int size, ToLongFunction<T> getValue) {
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
        private final int maxSize;
        private final ToLongFunction<T> getValue;
        private final TreeSet<T> sortedSet;
        private boolean isFull;
        private double worstValue;

        public State(int maxSize, ToLongFunction<T> getValue) {
            this.maxSize = maxSize;
            this.getValue = getValue;
            this.sortedSet = new TreeSet<>(Comparator.comparingLong(getValue));
        }

        public void add(T add) {
            if (!isFull) {
                sortedSet.add(add);
                if (sortedSet.size() >= maxSize) {
                    isFull = true;
                    worstValue = getValue.applyAsLong(sortedSet.getFirst());
                }
//                System.out.println("TopCollector " + sortedSet.size() + " / " + maxSize);
                return;
            }

            double addValue = getValue.applyAsLong(add);
            if (addValue > worstValue) {
                sortedSet.add(add);
                trim();
                worstValue = getValue.applyAsLong(sortedSet.getFirst());
            }
        }

        public State<T> combine(State<T> other) {
            sortedSet.addAll(other.sortedSet);

            int setSize = sortedSet.size();
            if (setSize >= maxSize) {
                isFull = true;
                trim();
                worstValue = getValue.applyAsLong(sortedSet.getFirst());
            } else if (setSize > 0) {
                isFull = false;
                worstValue = getValue.applyAsLong(sortedSet.getFirst());
            } else {
                isFull = false;
            }
            return this;
        }

        private void trim() {
            while (sortedSet.size() > maxSize) {
                sortedSet.removeFirst();
            }
        }

        public Collection<T> finish() {
            return sortedSet;
        }
    }
}
