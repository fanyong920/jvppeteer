package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;

import java.util.ArrayList;

public class BroswerCloseExample {

    public static void main(String[] args) throws Exception {

        ArrayList<Browser> browsers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            //ExecutablePath是指定chrome启动路径
            ArrayList<String> arrayList = new ArrayList<>();
            LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
            arrayList.add("--no-sandbox");
            arrayList.add("--disable-setuid-sandbox");


            Browser browser = Puppeteer.launch(options);
            browsers.add(browser);
            Page page = browser.newPage();
            page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");

        }
        browsers.forEach(Browser::close);
    }
}
