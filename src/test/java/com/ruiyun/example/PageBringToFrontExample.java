package com.ruiyun.example;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 切换某个页面为当前页面的例子
 */
public class PageBringToFrontExample {

    public static void main(String[] args) throws Exception {
         String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        //String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        //打开第一个页面
        Page page1 = browser.newPage();
        page1.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        //打开第二个页面
        Page page2 = browser.newPage();
        page2.goTo("https://github.com/fanyong920/jvppeteer/tree/master/src/test/java/com/ruiyun/example");

        //让页面切换回来第一个页面
        page1.bringToFront();
    }
}
