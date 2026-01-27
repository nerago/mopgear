package au.nerago.mopgear.util;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class StreamNeedClose<T> implements AutoCloseable {
    private final Stream<T> stream;

    public StreamNeedClose(Stream<T> stream) {
        this.stream = stream;
    }

    public StreamNeedClose<T> parallel() {
        return new StreamNeedClose<>(stream.parallel());
    }

    public StreamNeedClose<T> unordered() {
        return new StreamNeedClose<>(stream.unordered());
    }

    public <R> StreamNeedClose<R> map(Function<? super T, ? extends R> mapper) {
        return new StreamNeedClose<>(stream.map(mapper));
    }

    public StreamNeedClose<T> filter(Predicate<T> filter) {
        return new StreamNeedClose<>(stream.filter(filter));
    }

    public <R, A> R collect(Collector<? super T, A, R> collector) {
        R result = stream.collect(collector);
//        stream.close();
        return result;
    }

    public Optional<T> max(Comparator<? super T> comparator) {
        Optional<T> result = stream.max(comparator);
//        stream.close();
        return result;
    }

    @Override
    public void close() {
        stream.close();
    }
}
