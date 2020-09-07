package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.JSHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.util.ArrayList;
import java.util.Arrays;

public class QueryobjectsExample {
    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false)/*.withExecutablePath(path)*/.build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://item.taobao.com/item.htm?id=541605195654");
        page.evaluate("() => window.map = new Map()");
        JSHandle mapPrototype  = page.evaluateHandle("() => Map.prototype");
        JSHandle mapInstances  = page.queryObjects(mapPrototype);
        page.evaluate("maps => maps.length");

        mapPrototype.dispose();
        mapInstances.dispose();


        for (int i = 0; i < 5; i++) {
            browser.newPage();
        }
        //看看这个browser有多少个target
        System.out.println(browser.targets().size());
        for (Target target : browser.targets()) {
            System.out.println(target.getTargetInfo());
        }

    }
}
