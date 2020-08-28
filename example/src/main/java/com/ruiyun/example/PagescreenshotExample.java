package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Clip;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.options.Viewport;

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
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        

//        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        //设置截图范围
//        Clip clip = new Clip(1.0,1.56,400,400);
//        screenshotOptions.setClip(clip);
        //设置存放的路径
//        screenshotOptions.setPath();
        page.screenshot("test.png");

//        Viewport viewport = new Viewport();
//        viewport.setHeight(1080);
//        viewport.setWidth(1920);
//        page.setViewport(viewport);
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setQuality(100);
        screenshotOptions.setFullPage(true);
        screenshotOptions.setPath("test2.jpg");
        page.screenshot(screenshotOptions);
        page.close();
    }
}
