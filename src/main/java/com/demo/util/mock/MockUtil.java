package com.demo.util.mock;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MockUtil {

    public static <T> List<T> createList(int count, IntFunction<T> f){
        return IntStream.rangeClosed(1,count).mapToObj(f).collect(Collectors.toList());
    }

    public static void forEach(int count, IntConsumer f){
        IntStream.rangeClosed(1,count).forEach(f);
    }
}
