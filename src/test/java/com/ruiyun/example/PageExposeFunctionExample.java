package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

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
            text.forEach(arg -> {
                result.updateAndGet(v -> v + arg);
            });
            return result.get();
        });
        page.evaluate("async () => {\n" +
                "    // use window.md5 to compute hashes\n" +
                "    const myString = 'PUPPETEER';\n" +
                "    const myHash = await window.md5(myString);\n" +
                "    console.log(`md5 of ${myString} is ${myHash}`);\n" +
                "  }", PageEvaluateType.FUNCTION);
    }
}
