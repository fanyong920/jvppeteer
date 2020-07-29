package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Clip;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;

import java.util.ArrayList;

public class PagescreenshotExample {

    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true)/*.withExecutablePath(path)*/.build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");


//        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
//        //设置截图范围
//        Clip clip = new Clip(1.0,1.56,400,400);
//        screenshotOptions.setClip(clip);
//        //设置存放的路径
//        screenshotOptions.setPath("test.png");
//        page.screenshot(screenshotOptions);
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        //设置截图范围
        Clip clip = new Clip(1.0,1.56,400,400);
        screenshotOptions.setClip(clip);
        //设置存放的路径
        screenshotOptions.setPath("test.png");
        page.screenshot(screenshotOptions);

    }
}
