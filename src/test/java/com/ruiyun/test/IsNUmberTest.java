package com.ruiyun.test;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsNUmberTest {
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        Matcher matcher = pattern.matcher("0.2524");
        while (matcher.find()){
            System.out.println(matcher.group());
        }
        matcher = pattern.matcher("10.2524");
        while (matcher.find()){
            System.out.println(matcher.group());
        }
        matcher = pattern.matcher("-1.2524");
        while (matcher.find()){
            System.out.println(matcher.group());
        }
        matcher = pattern.matcher("1");
        while (matcher.find()){
            System.out.println(matcher.group());
        }
        matcher = pattern.matcher("09.2524");
        while (matcher.find()){
            System.out.println(matcher.group());
        }
        int i = new BigDecimal("-2.36").compareTo(new BigDecimal(0));
        System.out.println(i);
    }
}
