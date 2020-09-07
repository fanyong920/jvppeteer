package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Clip;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.options.WaitForSelectorOptions;

import java.util.ArrayList;

public class PagescreenshotExample {

    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
//        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
//        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        page.setDefaultTimeout(1000*6);
        for (int i = 0; i < 100; i++) {
            page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",false);

            // 根据dom判断是否加载完
            WaitForSelectorOptions waitForSelectorOptions = new WaitForSelectorOptions();
            waitForSelectorOptions.setTimeout(1000*15);
            waitForSelectorOptions.setVisible(Boolean.TRUE);
            ElementHandle elementHandle = page.waitForSelector("testdom", waitForSelectorOptions);

            ScreenshotOptions screenshotOptions = new ScreenshotOptions();
            screenshotOptions.setType("png");
            screenshotOptions.setFullPage(Boolean.TRUE);
            String base64Str = page.screenshot(screenshotOptions);
            System.out.println(i +" ===="+base64Str);
        }


    }
}
