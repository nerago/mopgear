package au.nicholas.hardy.mopgear.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collector;

public class RankedGroupsCollection<T> {
    private final TreeMap<Double, List<T>> map = new TreeMap<>();

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

    public static <T> Collector<T, RankedGroupsCollection<T>, RankedGroupsCollection<T>> collector(ToDoubleFunction<T> valueFunc) {
        return Collector.of(RankedGroupsCollection::new,
                (coll, obj) -> coll.add(obj, valueFunc.applyAsDouble(obj)),
                RankedGroupsCollection::combine,
                Collector.Characteristics.IDENTITY_FINISH);
    }

    private RankedGroupsCollection<T> combine(RankedGroupsCollection<T> other) {
        this.map.putAll(other.map);
        return this;
    }
}
