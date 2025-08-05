package au.nicholas.hardy.mopgear.util;

public class BestHolder<T> {
    private T bestObject;
    private long bestRating;

    public BestHolder(T object, long rating) {
        bestObject = object;
        bestRating = rating;
    }

    public T get() {
        return bestObject;
    }

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
