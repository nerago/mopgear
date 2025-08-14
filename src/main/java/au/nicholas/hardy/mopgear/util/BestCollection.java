package au.nicholas.hardy.mopgear.util;

import java.util.TreeMap;
import java.util.function.BiConsumer;

public class BestCollection<T> {
    private TreeMap<Double, T> map = new TreeMap<>();

    public void add(T object, double rating) {
        map.put(rating, object);
    }

    public void forEach(BiConsumer<T, Double> func) {
        map.forEach((key, value) -> func.accept(value, key));
    }
}
