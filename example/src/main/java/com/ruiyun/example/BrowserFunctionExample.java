package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserContext;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;

public class BrowserFunctionExample {
    public static void main(String[] args) throws Exception {
        //ExecutablePath是指定chrome启动路径
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        System.out.println("defaultBrowserContext: "+browser.defaultBrowserContext());
        String wsEndpoint = browser.wsEndpoint();
        System.out.println("browser.wsEndpoint(): "+browser.wsEndpoint());
        System.out.println("browser.version():"+browser.version());
        System.out.println("isConnected: "+ browser.connected());
//
//        BrowserContext incognitoBrowserContext = browser.createBrowserContext();
//        Collection<BrowserContext> browserContexts = browser.browserContexts();
//        System.out.println(browserContexts.size());
//
//        for (BrowserContext browserContext : browserContexts) {
//            //defaultcontext没有id
//            if(StringUtil.isNotEmpty(browserContext.getId()))
//                browser.disposeContext(browserContext.getId());
//        }
//        browser.disconnect();
//        System.out.println("isConnected: "+ browser.connected());
//
//        //重新连接
//        Browser connect = Puppeteer.connect(wsEndpoint);


//        browser.pages()
//        browser.process()
//        browser.target()
//        browser.targets()
//        browser.userAgent()
//
//        browser.waitForTarget(predicate[, options])
    }
}
