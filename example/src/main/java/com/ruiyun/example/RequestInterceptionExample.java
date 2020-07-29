package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Device;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class RequestInterceptionExample {
    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false)/*.withExecutablePath(path)*/.build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.emulate(Device.IPHONE_X);
//        System.out.println("sdaad");
        PageNavigateOptions options1 = new PageNavigateOptions();
        //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
        options1.setWaitUntil(Collections.singletonList("domcontentloaded"));
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",options1);
        //拦截请求
//        page.onRequest(request -> {
//            if ("image".equals(request.resourceType()) || "media".equals(request.resourceType())) {
//                //遇到多媒体或者图片资源请求，拒绝，加载页面加载
//                System.out.println(request.url()+": "+request.resourceType()+": abort");
//                request.abort();
//            } else {//其他资源放行
//                request.continueRequest();
//            }
//        });
//        page.setRequestInterception(true);

        page.goTo("https://item.taobao.com/item.htm?id=541605195654",options1);

    }
}
