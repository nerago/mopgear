package au.nicholas.hardy.mopgear.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public class TopCollector1<T> implements Collector<T, TopCollector1.State<T>, Collection<T>> {
    private final int size;
    private final ToLongFunction<T> getValue;

    public TopCollector1(int size, ToLongFunction<T> getValue) {
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
        private double worstValue;

        public State(int size, ToLongFunction<T> getValue) {
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
//                System.out.println("better " + add);
                trim();
            }
        }

        public State<T> combine(State<T> other) {
            sortedSet.addAll(other.sortedSet);
            trim();
            return this;
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

        public Collection<T> finish() {
            return sortedSet;
        }
    }
}
