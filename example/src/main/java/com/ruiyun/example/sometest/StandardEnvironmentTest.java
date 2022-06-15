package com.ruiyun.example.sometest;

import com.ruiyun.jvppeteer.core.Environment;
import com.ruiyun.jvppeteer.core.env.StandardEnvironment;

/**
 * @author sage.xue
 * @date 2022/6/10 20:56
 */
public class StandardEnvironmentTest {

    public static void main(String[] args) {
        Environment environment = new StandardEnvironment();

        String value = environment.getEnv("foo.bar");
        System.out.println(value);
    }

}
