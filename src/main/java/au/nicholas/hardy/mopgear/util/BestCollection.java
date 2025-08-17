package au.nicholas.hardy.mopgear.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class BestCollection<T> {
    private TreeMap<Double, List<T>> map = new TreeMap<>();

    public void add(T object, double rating) {
        map.compute(rating, (k, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(object);
            return list;
        });
    }

    public void forEach(BiConsumer<T, Double> func) {
        map.forEach((key, list) ->
                list.forEach(value -> func.accept(value, key))
        );
    }
}
