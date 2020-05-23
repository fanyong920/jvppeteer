package com.ruiyun.example;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;

import java.io.IOException;
import java.util.ArrayList;

public class BroswerCloseExample {

    public static void main(String[] args) throws IOException {
        String  path ="D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        browser.close();
    }
}
