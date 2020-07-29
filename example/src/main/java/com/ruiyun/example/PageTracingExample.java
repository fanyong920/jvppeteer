package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

import java.util.ArrayList;

public class PageTracingExample {

    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);


        ArrayList<String> argList = new ArrayList<>();

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(argList).withHeadless(true)/*.withExecutablePath(path)*/.build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        //开启追踪
        page.tracing().start("C:\\Users\\howay\\Desktop\\trace.json");
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        page.tracing().stop();
        //waifor tracingComplete

    }
}
