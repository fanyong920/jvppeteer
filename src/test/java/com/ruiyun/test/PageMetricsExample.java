package com.ruiyun.test;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.protocol.performance.Metrics;

import java.beans.IntrospectionException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

public class PageMetricsExample {

    public static void main(String[] args) throws UnsupportedEncodingException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");

       // String  path ="D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");

        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();

        PageNavigateOptions navigateOptions = new PageNavigateOptions();
        //dom加载完毕就算导航完成
        navigateOptions.setWaitUntil(Arrays.asList("domcontentloaded"));
        //page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",navigateOptions);
        page.goTo("https://detail.tmall.com/item.htm?id=616839388072",navigateOptions);

        Metrics metrics = page.metrics();
        System.out.println(metrics);
        browser.close();
    }
}
