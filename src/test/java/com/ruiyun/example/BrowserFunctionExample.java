package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.options.BrowserOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserContext;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;

public class BrowserFunctionExample {
    public static void main(String[] args) throws Exception {
//        String  path ="D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
                String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Browser browser1 = Puppeteer.launch(options);

        System.out.println("defaultBrowserContext: "+browser.defaultBrowserContext());
        String wsEndpoint = browser.wsEndpoint();
        System.out.println("browser.wsEndpoint(): "+browser.wsEndpoint());
        System.out.println("browser.version():"+browser.version());
        System.out.println("isConnected: "+ browser.isConnected());

        BrowserContext incognitoBrowserContext = browser.createIncognitoBrowserContext();
        Collection<BrowserContext> browserContexts = browser.browserContexts();
        System.out.println(browserContexts.size());

        for (BrowserContext browserContext : browserContexts) {
            //defaultcontext没有id
            if(StringUtil.isNotEmpty(browserContext.getId()))
            browser.disposeContext(browserContext.getId());
        }
        browser.onDisconnected((s) -> {
            System.out.println("我是浏览器事件监听，现在监听到 disconnected");
        });
        browser.disconnect();
        System.out.println("isConnected: "+ browser.isConnected());

        //重新连接
        Browser connect = Puppeteer.connect(new BrowserOptions(), wsEndpoint, null, null, null);


//        browser.pages()
//        browser.process()
//        browser.target()
//        browser.targets()
//        browser.userAgent()
//
//        browser.waitForTarget(predicate[, options])
    }
}
