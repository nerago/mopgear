package au.nicholas.hardy.mopgear.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public class TopCollectorReporting<T> implements Collector<T, TopCollectorReporting.State<T>, Collection<T>> {
    private final ToLongFunction<T> getValue;
    private final Consumer<T> reportBetter;

    public TopCollectorReporting(ToLongFunction<T> getValue, Consumer<T> reportBetter) {
        this.getValue = getValue;
        this.reportBetter = reportBetter;
    }

    @Override
    public Supplier<State<T>> supplier() {
        return () -> new State<>(getValue, reportBetter);
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
        return EnumSet.of(Characteristics.UNORDERED, Characteristics.CONCURRENT);
    }

    public static class State<T> {
        private final ToLongFunction<T> getValue;
        private final Consumer<T> reportBetter;
        private double bestValue;
        private T best;

        public State(ToLongFunction<T> getValue, Consumer<T> reportBetter) {
            this.getValue = getValue;
            this.reportBetter = reportBetter;
        }

        public void add(T add) {
            double addValue = getValue.applyAsLong(add);

            if (bestValue == 0.0) {
                bestValue = addValue;
                best = add;
                reportBetter.accept(add);
            } else if (addValue > bestValue) {
                bestValue = addValue;
                best = add;
                reportBetter.accept(add);
            }
        }

        public State<T> combine(State<T> other) {
            if (other.bestValue > this.bestValue) {
                return other;
            } else {
                return this;
            }
        }

        public Collection<T> finish() {
            if (best != null)
                return Collections.singletonList(best);
            else
                return Collections.emptyList();
        }
    }
}
