package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.util.ArrayList;

public class PageGotoExample {

    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> argList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(argList).withHeadless(false)./*withExecutablePath(path).*/build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Browser browser2 = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.taobao.com/about/");
        browser.close();

        Page page1 = browser2.newPage();
        page1.goTo("https://www.taobao.com/about/");
        System.out.println(page1.$eval("#content > div.right-info > div > div.inner.taobao-intro > p","p => p.value", PageEvaluateType.FUNCTION,null));
    }
}
