package com.ruiyun.example.sometest;

import org.junit.Test;

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
