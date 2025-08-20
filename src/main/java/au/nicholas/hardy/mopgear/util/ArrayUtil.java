package au.nicholas.hardy.mopgear.util;

import au.nicholas.hardy.mopgear.domain.ItemData;
import au.nicholas.hardy.mopgear.domain.StatType;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

public class ArrayUtil {

    public static <T> void forEach(T[] array, Consumer<T> function) {
        for (T t : array) {
            function.accept(t);
        }
    }

    public static <T> void mapInPlace(T[] array, Function<T, T> function) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = function.apply(array[i]);
        }
    }

    public static <T> T[] mapAsNew(T[] input, Function<T, T> function) {
        T[] result = createGeneric(input, input.length);
        for (int i = 0; i < input.length; ++i) {
            result[i] = function.apply(input[i]);
        }
        return result;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        int newLen = first.length + second.length;
        T[] result = createGeneric(first, newLen);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] createGeneric(T[] example, int length) {
        return (T[]) Array.newInstance(example.getClass().getComponentType(), length);
    }

    public static StatType[] common(StatType[] first, StatType[] second) {
        ArrayList<StatType> result = new ArrayList<>();
        for (StatType a : first) {
            if (ArrayUtil.contains(second, a)) {
                result.add(a);
            }
        }
        return result.toArray(StatType[]::new);
    }

    private static boolean contains(StatType[] array, StatType e) {
        for (StatType item : array) {
            if (item == e) {
                return true;
            }
        }
        return false;
    }

    public static ItemData rand(ItemData[] itemList, Random random) {
        if (itemList.length > 1)
            return itemList[random.nextInt(itemList.length)];
        else
            return itemList[0];
    }

    public static <T> boolean anyMatch(T[] existing, Predicate<T> predicate) {
        for (T item : existing) {
            if (predicate.test(item))
                return true;
        }
        return false;
    }

    public static <T> T findOne(T[] existing, Predicate<T> predicate) {
        boolean found = false;
        T result = null;
        for (T item : existing) {
            if (predicate.test(item)) {
                if (found)
                    throw new IllegalStateException("unexpected repeat match");
                found = true;
                result = item;
            }
        }
        return result;
    }

    public static <T> T[] allMatch(T[] existing, Predicate<T> predicate) {
        ArrayList<T> temp = new ArrayList<>();
        for (T item : existing) {
            if (predicate.test(item)) {
                temp.add(item);
            }
        }
        return temp.toArray(createGeneric(existing, temp.size()));
    }

    public static <T> Stream<T> arrayStream(T[] initialSets) {
        final Spliterator<T> split = Spliterators.spliterator(initialSets, SIZED | SUBSIZED | ORDERED | DISTINCT | NONNULL | IMMUTABLE);
        return StreamSupport.stream(split, true);
    }

    public static ItemData[] clone(ItemData[] array) {
        if (array != null) {
            return Arrays.copyOf(array, array.length);
        } else {
            return null;
        }
    }
}
