package au.nerago.mopgear.util;

public class BestHolder<T> {
    private T bestObject;
    private long bestRating;

    public BestHolder() {
        bestObject = null;
        bestRating = Long.MIN_VALUE;
    }

    public BestHolder(T object, long rating) {
        bestObject = object;
        bestRating = rating;
    }

    public T get() {
        return bestObject;
    }

    @SuppressWarnings("unused")
    public long getRating() {
        return bestRating;
    }

    public void add(T object, long rating) {
        if (rating > bestRating) {
            bestObject = object;
            bestRating = rating;
        }
    }
}
