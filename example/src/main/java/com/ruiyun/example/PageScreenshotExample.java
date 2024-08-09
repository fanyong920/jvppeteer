package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.*;

import java.util.ArrayList;

public class PageScreenshotExample {

    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
//        BrowserFetcher.downloadIfNotExist(null);
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();


//        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
//        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
//        page.setDefaultTimeout(1000*6);

            page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",true);

            // 根据dom判断是否加载完


            ScreenshotOptions screenshotOptions = new ScreenshotOptions();
            screenshotOptions.setType("png");
//            screenshotOptions.setFullPage(Boolean.TRUE);
            screenshotOptions.setPath("sss.png");
            screenshotOptions.setClip(new ClipOverwrite(100,100,100,100,1));
            page.screenshot(screenshotOptions);


        browser.close();
    }
}
