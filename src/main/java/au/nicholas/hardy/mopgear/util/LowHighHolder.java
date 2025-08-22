package au.nicholas.hardy.mopgear.util;

public class LowHighHolder<T> {
    private T lowObject, highObject;
    private long lowRating, highRating;

    public LowHighHolder() {
        lowRating = Long.MAX_VALUE;
        highRating = Long.MIN_VALUE;
    }

    public T getLow() {
        return lowObject;
    }

    public T getHigh() { return highObject; }

    public long getLowRating() {
        return lowRating;
    }

    public long getHighRating() {
        return highRating;
    }

    public void add(T object, long rating) {
        if (rating > highRating) {
            highObject = object;
            highRating = rating;
        }
        if (rating < lowRating) {
            lowObject = object;
            lowRating = rating;
        }
    }
}
