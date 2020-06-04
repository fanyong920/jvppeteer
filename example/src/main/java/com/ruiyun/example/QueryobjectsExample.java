package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.JSHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.util.ArrayList;

public class QueryobjectsExample {
    public static void main(String[] args) throws Exception {
        //String path = new String("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe".getBytes(), "UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://item.taobao.com/item.htm?id=541605195654");
        page.evaluate("() => window.map = new Map()", PageEvaluateType.FUNCTION);
        JSHandle mapPrototype  = page.evaluateHandle("() => Map.prototype", PageEvaluateType.FUNCTION);
        JSHandle mapInstances  = page.queryObjects(mapPrototype);
        page.evaluate("maps => maps.length",PageEvaluateType.FUNCTION, mapInstances);

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
