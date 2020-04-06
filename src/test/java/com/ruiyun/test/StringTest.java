package com.ruiyun.test;

import org.junit.Test;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

public class StringTest {

    @Test
    public void test1(){
        String test = "ws://localhost:364545/browser/asdsadadsdfad-sdad-sada-dasd";
        test = test.replace("ws://localhost:","");
        int end = test.indexOf("/");
        System.out.println(Integer.parseInt(test.substring(0,end)));

        Properties properties = System.getProperties();
        Set<Object> objects = properties.keySet();
        for (Object object : objects) {
            Object o = properties.get(object);
            System.out.println(object+":"+o);
        }
    }
}
