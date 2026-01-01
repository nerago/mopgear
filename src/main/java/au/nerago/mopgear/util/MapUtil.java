package au.nerago.mopgear.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {
    public static <K, V> Map<V, K> inverse(Map<K, V> input) {
        Map<V, K> output = new HashMap<>();
        input.forEach((k, v) -> output.put(v, k));
        return output;
    }
}
