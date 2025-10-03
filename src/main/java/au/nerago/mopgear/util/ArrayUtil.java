package au.nerago.mopgear.util;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.StatType;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

@SuppressWarnings("unused")
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

    public static <T, R> R[] mapAsNew(T[] input, Function<T, R> function, IntFunction<R[]> makeArray) {
        R[] result = makeArray.apply(input.length);
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

    public static <T> T[] concatNullSafe(T[] first, T[] second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        } else {
            return concat(first, second);
        }
    }

    public static <T> T[] concat(T[][] components) {
        if (components.length == 0)
            throw new IllegalArgumentException();
        int newLen = 0;
        for (T[] array : components) {
            newLen += array.length;
        }
        T[] result = createGeneric(components[0], newLen);
        int index = 0;
        for (T[] array : components) {
            System.arraycopy(array, 0, result, index, array.length);
            index += array.length;
        }
        return result;
    }

    public static <T> T[] append(T[] array, T item) {
        int newLen = array.length + 1;
        T[] result = createGeneric(array, newLen);
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = item;
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

    public static boolean contains(int[] array, int e) {
        for (int item : array) {
            if (item == e) {
                return true;
            }
        }
        return false;
    }

    private static <T> boolean contains(T[] array, T e) {
        for (T item : array) {
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

    public static ItemData rand(List<ItemData> itemList, Random random) {
        int size = itemList.size();
        if (size > 1)
            return itemList.get(random.nextInt(size));
        else
            return itemList.getFirst();
    }

    public static <T> boolean anyMatch(T[] existing, Predicate<T> predicate) {
        if (existing != null) {
            for (T item : existing) {
                if (predicate.test(item))
                    return true;
            }
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
        if (found)
            return result;
        else
            throw new IllegalStateException("no match found");
    }

    public static <T> T findAny(T[] existing, Predicate<T> predicate) {
        for (T item : existing) {
            if (predicate.test(item)) {
                return item;
            }
        }
        throw new IllegalStateException("no match found");
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
        final Spliterator<T> split = Spliterators.spliterator(initialSets, SIZED | SUBSIZED | DISTINCT | NONNULL | IMMUTABLE);
        return StreamSupport.stream(split, true);
    }

    public static ItemData[] clone(ItemData[] array) {
        if (array != null) {
            return Arrays.copyOf(array, array.length);
        } else {
            return null;
        }
    }

    public static <T> void throwIfHasDuplicates(T[] tempArray) {
        HashSet<T> set = new HashSet<>();
        for (T item : tempArray) {
            if (!set.add(item)) {
                throw new IllegalArgumentException("duplicate item " + item);
            }
        }
    }

    public static String repeat(char c, int count) {
        if (count == 0) {
            return "";
        }
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
