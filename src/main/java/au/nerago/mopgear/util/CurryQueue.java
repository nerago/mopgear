package au.nerago.mopgear.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class CurryQueue<T> {
    private final T item;
    private CurryQueue<T> right;

    private CurryQueue(T item, CurryQueue<T> right) {
        this.item = item;
        this.right = right;
    }

    public T item() {
        return item;
    }

    public CurryQueue<T> tail() {
        return right;
    }

    public CurryQueue<T> prepend(T v) {
        return new CurryQueue<>(v, this);
    }

    public static <R> CurryQueue<R> prepend(R v, CurryQueue<R> queue) {
        if (queue != null) {
            return queue.prepend(v);
        } else {
            return single(v);
        }
    }

    public static <R> CurryQueue<R> single(R v) {
        return new CurryQueue<>(v, null);
    }

//    @Deprecated
    public static <T> CurryQueue<T> build(Collection<T> coll) {
        Iterator<T> iterator = coll.iterator();
        if (!iterator.hasNext()) {
            return null;
        }

        CurryQueue<T> first = new CurryQueue<>(iterator.next(), null);

        CurryQueue<T> prev = first;
        while (iterator.hasNext()) {
            CurryQueue<T> entry = new CurryQueue<>(iterator.next(), null);
            prev.right = entry;
            prev = entry;
        }

        return first;
    }

    @Deprecated
    public Iterator<T> iterator() {
        return new QIterator<>(this);
    }

    @Deprecated
    public Stream<T> stream() {
        return StreamSupport.stream(new QSpliterator<>(this), false);
    }

    public long size() {
        int z = 1;
        CurryQueue<T> n = right;
        while (n != null) {
            z++;
            n = n.right;
        }
        return z;
    }

    public T[] toArrayForward(IntFunction<T[]> makeArray) {
        int size = 1;
        CurryQueue<T> node = right;
        while (node != null) {
            size++;
            node = node.right;
        }

        T[] array = makeArray.apply(size);
        array[0] = item;

        int index = 1;
        node = right;
        while (node != null) {
            array[index++] = node.item;
            node = node.right;
        }

        return array;
    }

    public T[] toArrayReverse(IntFunction<T[]> makeArray) {
        int size = 1;
        CurryQueue<T> node = right;
        while (node != null) {
            size++;
            node = node.right;
        }

        T[] array = makeArray.apply(size);
        int index = size - 1;
        array[index--] = item;

        node = right;
        while (node != null) {
            array[index--] = node.item;
            node = node.right;
        }

        return array;
    }

    public void toArrayForward(T[] outputArray) {
        outputArray[0] = item;

        int index = 1;
        CurryQueue<T> node = right;
        while (node != null) {
            outputArray[index++] = node.item;
            node = node.right;
        }

        if (index != outputArray.length)
            throw new RuntimeException("array not expected length");
    }

    private static class QIterator<T> implements Iterator<T> {
        private CurryQueue<T> q;

        public QIterator(CurryQueue<T> q) {
            this.q = q;
        }

        @Override
        public boolean hasNext() {
            return q != null;
        }

        @Override
        public T next() {
            T x = q.item;
            q = q.right;
            return x;
        }
    }

    private static class QSpliterator<T> implements Spliterator<T> {
        private CurryQueue<T> q;

        public QSpliterator(CurryQueue<T> q) {
            this.q = q;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (q != null) {
                action.accept(q.item);
                q = q.right;
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equalsOther((CurryQueue<?>) o);
    }

    public boolean equalsOther(CurryQueue<?> that) {
        if (!item.equals(that.item))
            return false;
        CurryQueue<?> n = right, x = that.right;
        while (n != null && x != null) {
            if (!n.item.equals(x.item))
                return false;
            n = n.right;
            x = x.right;
        }
        return x == n;
    }

    @Override
    public int hashCode() {
        int hc = item.hashCode();
        CurryQueue<T> n = right;
        while (n != null) {
            hc = 31 * hc + n.item.hashCode();
            n = n.right;
        }
        return hc;
    }
}
