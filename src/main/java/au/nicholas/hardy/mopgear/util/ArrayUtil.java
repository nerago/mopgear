package au.nicholas.hardy.mopgear.util;

import au.nicholas.hardy.mopgear.ItemData;
import au.nicholas.hardy.mopgear.StatType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArrayUtil {
    public static <T> void mapInPlace(T[] array, Function<T, T> function) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = function.apply(array[i]);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[] first, T[] second) {
        int newLen = first.length + second.length;
        T[] result = (T[]) Array.newInstance(first.getClass().getComponentType(), newLen);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
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
}
