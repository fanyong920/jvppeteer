package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

import java.util.ArrayList;

public class PageSomeFunctionExample {
    public static void main(String[] args) throws Exception {

        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false)/*.withExecutablePath(path)*/.withDumpio(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        //关闭缓存，默认是开启的
        page.setCacheEnabled(false);
        //切换绕过页面的内容安全策略，默认是true
        page.setBypassCSP(false);
        page.setGeolocation(30.31667,59.95);

        page.setJavaScriptEnabled(true);
        page.goTo("https://www.meituan.com/");
        String title = page.title();
        System.out.println("title1:"+title);
        //点击 深圳 按钮
        page.tap("#react > div > div.citylist > p > a:nth-child(4)");

         title = page.title();
        System.out.println("title2:"+title);
        //启用离线模式，启用后不能连接互联网
//        page.setOfflineMode(true);

        //在这里将抛错 net::ERR_INTERNET_DISCONNECTED
       // page.goTo("https://item.taobao.com/item.htm?id=541605195654");

    }
}
