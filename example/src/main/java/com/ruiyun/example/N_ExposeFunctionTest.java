package com.ruiyun.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.function.Consumer;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class N_ExposeFunctionTest {

    @Test
    public void test4() throws Exception {
        //打开开发者工具
        LAUNCHOPTIONS.setDevtools(true);
        try (Browser cdpBrowser = Puppeteer.launch(LAUNCHOPTIONS)) {
            //打开一个页面
            Page page = cdpBrowser.newPage();
            page.on(PageEvents.Console, (Consumer<ConsoleMessage>) consoleMessage -> System.out.println(consoleMessage.text()));
            //exposeFunction有两个参数，第一个参数是在页面创建了一个函数名为md5的函数，函数实现逻辑为第二个参数。可以使用page.evaluate()调用md5函数进行测试
            page.exposeFunction("md5", (args) -> {
                try {
                    System.out.println("接收到浏览器的参数args: " + Constant.OBJECTMAPPER.writeValueAsString(args)+"，并在 Jvppeteer 程序内计算 MD5");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                String md5 = getMD5((String) args.get(0));
                System.out.println(" Jvppeteer 程序内计算的md5:" + md5);
                return md5;
            });
            //调用md5函数
            page.evaluate("async () => {\n" +
                    "    // use window.md5 to compute hashes\n" +
                    "    const myString = 'PUPPETEER';\n" +
                    "    const myHash = await window.md5(myString);\n" +
                    "    console.log(`浏览器接收到的md5 of ${myString} is ${myHash}`);\n" +
                    "  }");
            //删除md5函数
            page.removeExposedFunction("md5");
            //再次执行md5函数，报错
            page.evaluate("async () => {\n" +
                    "    // use window.md5 to compute hashes\n" +
                    "    const myString = 'PUPPETEER';\n" +
                    "    const myHash = await window.md5(myString);\n" +
                    "    console.log(`md5 of ${myString} is ${myHash}`);\n" +
                    "  }");
        }
    }

    @Test
    public void test5() throws Exception {
        //打开开发者工具
        LAUNCHOPTIONS.setDevtools(true);
        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            //打开一个页面
            Page page = browser.newPage();
            page.on(PageEvents.Console, (Consumer<ConsoleMessage>) consoleMessage -> System.out.println("浏览器接收到计算结果，并打印: " + consoleMessage.text()));
            page.exposeFunction("readfile", (filePath) -> {
                try {
                    System.out.println("接收到浏览器的参数："+Constant.OBJECTMAPPER.writeValueAsString(filePath)+"，并在 Jvppeteer 程序内进行计算。");
                    List<String> strings = Files.readAllLines(Paths.get((String) filePath.get(0)), StandardCharsets.UTF_8);
                    System.out.println("程序内计算结果: " + String.join("\n", strings));
                    return String.join("\n", strings);
                } catch (IOException e) {
                    return "Jvppeteer 出错啦";
                }
            });
            //调用md5函数
            page.evaluate("async () => {\n" +
                    "    // use window.readfile to read contents of a file\n" +
                    "    const content = await window.readfile('C:/Windows/System32/drivers/etc/hosts');\n" +
                    "    console.log(content);\n" +
                    "  }");
            Thread.sleep(5000);
        }
    }

    public static String getMD5(String info) {
        try {
            //获取 MessageDigest 对象，参数为 MD5 字符串，表示这是一个 MD5 算法（其他还有 SHA1 算法等）：
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //update(byte[])方法，输入原数据
            //类似StringBuilder对象的append()方法，追加模式，属于一个累计更改的过程
            md5.update(info.getBytes(StandardCharsets.UTF_8));
            //digest()被调用后,MessageDigest对象就被重置，即不能连续再次调用该方法计算原数据的MD5值。可以手动调用reset()方法重置输入源。
            //digest()返回值16位长度的哈希值，由byte[]承接
            byte[] md5Array = md5.digest();
            //byte[]通常我们会转化为十六进制的32位长度的字符串来使用,本文会介绍三种常用的转换方法
            return bytesToHex1(md5Array);
        } catch (Exception e) {
            return "";
        }
    }

    public static String bytesToHex1(byte[] md5Array) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte b : md5Array) {
            int temp = 0xff & b;
            String hexString = Integer.toHexString(temp);
            if (hexString.length() == 1) {//如果是十六进制的0f，默认只显示f，此时要补上0
                strBuilder.append("0").append(hexString);
            } else {
                strBuilder.append(hexString);
            }
        }
        return strBuilder.toString();
    }
}
