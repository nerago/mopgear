package au.nicholas.hardy.mopgear.util;

import au.nicholas.hardy.mopgear.ItemData;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class ArrayUtil {
    public static <T> void map(T[] array, Function<T, T> function) {
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

    public static ItemData rand(ItemData[] itemList, Random random) {
        if (itemList.length > 1)
            return itemList[random.nextInt(itemList.length)];
        else
            return itemList[0];
    }
}
