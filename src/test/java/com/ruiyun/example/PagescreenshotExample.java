package com.ruiyun.example;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Clip;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;

import java.io.IOException;
import java.util.ArrayList;

public class PagescreenshotExample {

    public static void main(String[] args) throws IOException {
       // String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath(path).build();
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
