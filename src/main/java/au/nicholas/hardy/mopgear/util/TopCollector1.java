package au.nicholas.hardy.mopgear.util;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

public class TopCollector1<T> implements Collector<T, TopCollector1.State<T>, Optional<T>> {
    private final ToLongFunction<T> getValue;

    public TopCollector1(ToLongFunction<T> getValue) {
        this.getValue = getValue;
    }

    @Override
    public Supplier<State<T>> supplier() {
        return () -> new State<>(getValue);
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
    public Function<State<T>, Optional<T>> finisher() {
        return State::finish;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED);
    }

    public static class State<T> {
        private final ToLongFunction<T> getValue;
        private double bestValue;
        private T best;

        public State(ToLongFunction<T> getValue) {
            this.getValue = getValue;
        }

        public void add(T add) {
            double addValue = getValue.applyAsLong(add);

            if (bestValue == 0.0) {
                bestValue = addValue;
                best = add;
            } else if (addValue > bestValue) {
                bestValue = addValue;
                best = add;
            }
        }

        public State<T> combine(State<T> other) {
            if (other.bestValue > this.bestValue) {
                return other;
            } else {
                return this;
            }
        }

        public Optional<T> finish() {
            if (best != null)
                return Optional.of(best);
            else
                return Optional.empty();
        }
    }
}
