package au.nicholas.hardy.mopgear.util;

import au.nicholas.hardy.mopgear.results.JobInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collector;

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

    public static <T> Collector<T, BestCollection<T>, BestCollection<T>> collector(ToDoubleFunction<T> valueFunc) {
        return Collector.of(BestCollection::new,
                (coll, obj) -> coll.add(obj, valueFunc.applyAsDouble(obj)),
                BestCollection::combine,
                Collector.Characteristics.IDENTITY_FINISH);
    }

    private static <T> BestCollection<T> combine(BestCollection<T> a, BestCollection<T> b) {
        a.map.putAll(b.map);
        return a;
    }
}
