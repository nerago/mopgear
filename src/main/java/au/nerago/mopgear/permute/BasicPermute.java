package au.nerago.mopgear.permute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BasicPermute {
    public static <T> Stream<List<T>> process(List<List<T>> stageOptions) {
        Stream<List<T>> stream = Stream.of(new ArrayList<>());
        for (List<T> options : stageOptions) {
            stream = permute(stream, options);
        }
        return stream;
    }

    private static <T> Stream<List<T>> permute(Stream<List<T>> stream, List<T> options) {
        return stream.mapMulti((inputList, downstream) -> {
            for (T item : options) {
                List<T> nextList = new ArrayList<>(inputList);
                nextList.add(item);
                downstream.accept(nextList);
            }
        });
    }
}
