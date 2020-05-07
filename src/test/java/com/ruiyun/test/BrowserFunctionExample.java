package com.ruiyun.test;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.types.browser.Browser;
import com.ruiyun.jvppeteer.types.browser.BrowserContext;

import java.util.ArrayList;

public class BrowserFunctionExample {
    public static void main(String[] args) {
        String  path ="D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Browser browser1 = Puppeteer.launch(options);
        //  BrowserContext incognitoBrowserContext = browser.createIncognitoBrowserContext();
        System.out.println("defaultBrowserContext: "+browser.defaultBrowserContext());
        String wsEndpoint = browser.wsEndpoint();
        System.out.println("browser.wsEndpoint(): "+browser.wsEndpoint());
        System.out.println("browser.version():"+browser.version());
        System.out.println("isConnected: "+ browser.isConnected());
        browser.onDisconnected((s) -> {
            System.out.println("我是浏览器事件监听，现在监听到 disconnected");
        });
        browser.disconnect();
        System.out.println("isConnected: "+ browser.isConnected());

//        browser.pages()
//        browser.process()
//        browser.target()
//        browser.targets()
//        browser.userAgent()
//
//        browser.waitForTarget(predicate[, options])
    }
}
