package com.ruiyun.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.common.WebPermission;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.BrowserContext;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Target;
import org.junit.Test;

import java.util.List;

public class T_BroswerContextApiTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        BrowserContext defaultBrowserContext = browser.defaultBrowserContext();
        Page page = defaultBrowserContext.newPage();
        new Thread(() -> {
            try {
                page.evaluate("() => window.open('https://www.example.com/')");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).start();
        Target target1 = defaultBrowserContext.waitForTarget(target -> target.url().equals("https://www.example.com/"));
        System.out.println("target1:" + target1.url());
        List<Page> pages = defaultBrowserContext.pages();
        System.out.println("size1:" + pages.size());
        defaultBrowserContext.newPage();
        System.out.println("size2:" + defaultBrowserContext.pages().size());
        List<Target> targets = defaultBrowserContext.targets();

        for (Target target : targets) {
            System.out.println("all target forEach:(" + target.type() + ":" + target.url() + ")");
        }
        BrowserContext browserContext = browser.createBrowserContext();
        Page page1 = browserContext.newPage();
        Browser browser1 = browserContext.browser();
        System.out.println("broswer equals:" + (browser1 == browser));
        browserContext.overridePermissions("https://www.baidu.com", WebPermission.GEOLOCATION);
        page1.goTo("https://www.baidu.com");
        browserContext.close();
        System.out.println("close: " + browserContext.closed());
        //默认浏览器不能关闭
        defaultBrowserContext.close();
        Thread.sleep(15000);
        browser.close();
    }

}
