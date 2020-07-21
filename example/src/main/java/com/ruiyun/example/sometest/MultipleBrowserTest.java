package com.ruiyun.example.sometest;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class MultipleBrowserTest {

    @Test
    public void test() throws Exception {
        ArrayList<String> argList = new ArrayList<>();
       // String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        //.withPipe(true) 不可用，切记不要加上这个参数
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(argList).withHeadless(false).build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        for (int i = 0; i < 5; i++) {
            Browser browser = Puppeteer.launch(options);
            Page page = browser.newPage();
        }
    }
}
