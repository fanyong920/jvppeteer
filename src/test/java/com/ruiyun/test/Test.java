package com.ruiyun.test;

import com.ruiyun.jvppeteer.util.FileUtil;

import java.io.IOException;
import java.io.StringWriter;

abstract class mm
{
    public abstract void play();
}
interface tt
{
    void play();
}
public class Test extends mm implements tt {
    public static void main(String args[]) {
        new Test().play();
        StringWriter writer = new StringWriter();

        writer.write("23456");
        System.out.println(writer.toString());
        writer.flush();
        System.out.println(writer.toString());
        writer.write("6789");

        System.out.println(writer.toString());
    }

    public void play() {
        System.out.println("我正在玩^^制^^^^^^");
    }


}