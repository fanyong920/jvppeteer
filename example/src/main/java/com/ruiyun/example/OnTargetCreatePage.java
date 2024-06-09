package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

import java.util.ArrayList;

public class OnTargetCreatePage {
    public static void main(String[] args) throws Exception {
        BrowserFetcher.downloadIfNotExist(null);
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        browser.onTargetcreated(target -> {
            if("page".equals(target.type())){
                Page page = target.page();
                page.setRequestInterception(true);
                page.onRequest(reqest -> {
                    System.out.println("请求："+reqest.url());
                    reqest.continueRequest();
                });
            }
        });
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        page.click("#s-top-left > a:nth-child(2)");
        String content = page.content();
        System.out.println("完成了");
        Thread.sleep(2000L);
        browser.newPage();
    }
}
