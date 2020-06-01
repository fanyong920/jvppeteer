package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class PageExposeFunctionExample {
    public static void main(String[] args) throws Exception {
        String path = new String("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe".getBytes(), "UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        // String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).withDumpio(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.onConsole((msg) -> {
            System.out.println(msg.text());
        });
        page.exposeFunction("md5",(text) -> {
            AtomicReference<String> result= new AtomicReference<>("");
            text.forEach(arg -> result.updateAndGet(v -> v + arg));
            return getMD5(result.get());
        });
        page.evaluate("async () => {\n" +
                "    // use window.md5 to compute hashes\n" +
                "    const myString = 'PUPPETEER';\n" +
                "    const myHash = await window.md5(myString);\n" +
                "    console.log(`md5 of ${myString} is ${myHash}`);\n" +
                "  }", PageEvaluateType.FUNCTION);
    }

    public static String getMD5(String info) {
        try {
            //获取 MessageDigest 对象，参数为 MD5 字符串，表示这是一个 MD5 算法（其他还有 SHA1 算法等）：
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //update(byte[])方法，输入原数据
            //类似StringBuilder对象的append()方法，追加模式，属于一个累计更改的过程
            md5.update(info.getBytes("UTF-8"));
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
        for (int i = 0; i < md5Array.length; i++) {
            int temp = 0xff & md5Array[i];
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
