package com.ruiyun.test;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.util.function.Consumer;

public class ParameterizedTypeTest {

    @Test
    public void test1(){
        Consumer<String> consumer = x -> {
            System.out.println(x);
        };
        Class<? extends Consumer> consumerClass = consumer.getClass();

    }
}
