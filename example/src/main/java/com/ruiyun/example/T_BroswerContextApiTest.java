package com.ruiyun.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieData;
import com.ruiyun.jvppeteer.cdp.entities.CookiePriority;
import com.ruiyun.jvppeteer.cdp.entities.CookieSameSite;
import com.ruiyun.jvppeteer.cdp.entities.CookieSourceScheme;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.WebPermission;
import java.util.Collections;
import java.util.List;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class T_BroswerContextApiTest {

    @Test
    public void test3() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        BrowserContext defaultCdpBrowserContext = browser.defaultBrowserContext();
        Page page = defaultCdpBrowserContext.newPage();
        new Thread(() -> {
            try {
                page.evaluate("() => window.open('https://www.example.com/')");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).start();
        Target target1 = defaultCdpBrowserContext.waitForTarget(target -> target.url().equals("https://www.example.com/"));
        System.out.println("target1:" + target1.url());
        List<Page> pages = defaultCdpBrowserContext.pages();
        System.out.println("size1:" + pages.size());
        defaultCdpBrowserContext.newPage();
        System.out.println("size2:" + defaultCdpBrowserContext.pages().size());
        List<Target> targets = defaultCdpBrowserContext.targets();

        for (Target target : targets) {
            System.out.println("all target forEach:(" + target.type() + ":" + target.url() + ")");
        }
        BrowserContext browserContext = browser.createBrowserContext();
        Page page1 = browserContext.newPage();
        Browser cdpBrowser1 = browserContext.browser();
        System.out.println("broswer equals:" + (cdpBrowser1 == browser));
        //overridePermissions方法在webdriver下会报错
        browserContext.overridePermissions("https://www.baidu.com", WebPermission.Persistent_storage);
        browserContext.clearPermissionOverrides();
        page1.goTo("https://www.baidu.com");
        browserContext.close();
        System.out.println("close: " + browserContext.closed());
        //默认浏览器不能关闭
        defaultCdpBrowserContext.close();
        Thread.sleep(5000);
        browser.close();
    }

    /**
     * 获取 该浏览器上下文的所有cookies
     *
     * @throws Exception 异常
     */
    @Test
    public void test4() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        BrowserContext defaultBrowserContext = browser.defaultBrowserContext();
        Page page = defaultBrowserContext.newPage();
        page.goTo("https://www.baidu.com");
        List<Cookie> cookies = defaultBrowserContext.cookies();
        for (Cookie cookie : cookies) {
            System.out.println("context cookie: " + cookie);
        }
    }

    /**
     * 删除该浏览器上下文的某个cookie
     *
     * @throws Exception 异常
     */
    @Test
    public void test5() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        BrowserContext defaultBrowserContext = browser.defaultBrowserContext();
        Page page = defaultBrowserContext.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        List<Cookie> cookies = defaultBrowserContext.cookies();
        for (Cookie cookie : cookies) {
            System.out.println("context cookie: " + cookie);
        }

        defaultBrowserContext.deleteCookie(cookies.get(0), cookies.get(1));
        System.out.println("------------------分割线----------------");
        List<Cookie> cookies2 = defaultBrowserContext.cookies();
        for (Cookie cookie : cookies2) {
            System.out.println("context2 cookie: " + cookie);
        }
    }

    /**
     * 删除该浏览器上下文的某个cookie
     *
     * @throws Exception 异常
     */
    @Test
    public void test6() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        BrowserContext defaultBrowserContext = browser.defaultBrowserContext();
        Page page = defaultBrowserContext.newPage();
        page.goTo("https://www.baidu.com");
        CookieData cookieParam = new CookieData();
        cookieParam.setName("customCookie");
        cookieParam.setValue("coo");
        cookieParam.setDomain(".baidu.com");
        cookieParam.setPath("/");
        //10S后过期
        cookieParam.setExpires(System.currentTimeMillis() / 1000 + 10);
        cookieParam.setPriority(CookiePriority.High);
        cookieParam.setSameSite(CookieSameSite.Strict);
        cookieParam.setSecure(true);
        cookieParam.setHttpOnly(false);
        cookieParam.setSourceScheme(CookieSourceScheme.Secure);
        ObjectNode partitionKey = Constant.OBJECTMAPPER.createObjectNode();
        partitionKey.put("sourceOrigin", "http://localhost");
        defaultBrowserContext.setCookie(cookieParam);
        List<Cookie> cookies = defaultBrowserContext.cookies();
        for (Cookie cookie : cookies) {
            System.out.println("context cookie: " + cookie);
        }
    }

    /**
     * 测试无痕模式
     *
     * @throws Exception 异常
     */
    @Test
    public void test7() throws Exception {
        //开启无痕模式
        LAUNCHOPTIONS.setArgs(Collections.singletonList("--incognito"));
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        BrowserContext defaultBrowserContext = browser.defaultBrowserContext();
        Page page = defaultBrowserContext.newPage();
        page.goTo("https://www.baidu.com");
        page.evaluate("localStorage.setItem(\"1\", \"2\")");
        Object evaluate = page.evaluate("localStorage.getItem(\"1\")");
        System.out.println(evaluate);
    }

}
