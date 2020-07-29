package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Device;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PageEmulateExample {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);


        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        //首先模仿成手机的样子
        page.emulate(Device.IPHONE_7);
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");

    }
}
