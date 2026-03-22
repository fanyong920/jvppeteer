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
import com.ruiyun.jvppeteer.common.Permission;
import com.ruiyun.jvppeteer.common.PermissionDescriptor;
import com.ruiyun.jvppeteer.common.PermissionState;
import com.ruiyun.jvppeteer.common.WebPermission;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;
import static org.junit.Assert.assertEquals;

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

    /**
     * 测试 setPermission
     *
     * @throws Exception 异常
     */
    @Test
    public void test8() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        BrowserContext context = browser.createBrowserContext();
        Page page = context.newPage();

        // 测试使用 * 作为源站授予地理位置权限
        PermissionDescriptor descriptor = new PermissionDescriptor();
        descriptor.setName("geolocation");
        Permission permission = new Permission();
        permission.setPermission(descriptor);
        permission.setState(PermissionState.Granted);
        context.setPermission("*", Arrays.asList(permission));
        assertEquals("granted", getPermission(page, "geolocation"));

        // 测试使用 * 作为源站拒绝地理位置权限
        PermissionDescriptor descriptor2 = new PermissionDescriptor();
        descriptor2.setName("geolocation");
        Permission permission2 = new Permission();
        permission2.setPermission(descriptor2);
        permission2.setState(PermissionState.Denied);
        context.setPermission("*", Arrays.asList(permission2));
        assertEquals("denied", getPermission(page, "geolocation"));


        // 测试使用 * 作为源站提示地理位置权限
        PermissionDescriptor descriptor3 = new PermissionDescriptor();
        descriptor3.setName("geolocation");
        Permission permission3 = new Permission();
        permission3.setPermission(descriptor3);
        permission3.setState(PermissionState.Prompt);
        context.setPermission("*", Arrays.asList(permission3));
        assertEquals("prompt", getPermission(page, "geolocation"));



        Page page2 = context.newPage();
        page2.goTo("https://www.baidu.com");
        PermissionDescriptor descriptor4 = new PermissionDescriptor();
        descriptor4.setName("geolocation");
        Permission permission4 = new Permission();
        permission4.setPermission(descriptor4);
        permission4.setState(PermissionState.Granted);
        context.setPermission(page2.url(), Arrays.asList(permission4));
        assertEquals("granted", getPermission(page2, "geolocation"));

        PermissionDescriptor descriptor5 = new PermissionDescriptor();
        descriptor5.setName("geolocation");
        Permission permission5 = new Permission();
        permission5.setPermission(descriptor5);
        permission5.setState(PermissionState.Denied);
        context.setPermission(page2.url(), Arrays.asList(permission5));
        assertEquals("denied", getPermission(page2, "geolocation"));


        PermissionDescriptor descriptor6 = new PermissionDescriptor();
        descriptor6.setName("geolocation");
        Permission permission6 = new Permission();
        permission6.setPermission(descriptor6);
        permission6.setState(PermissionState.Prompt);
        context.setPermission(page2.url(), Arrays.asList(permission6));
        assertEquals("prompt", getPermission(page2, "geolocation"));

        // 测试同时设置多个权限为允许状态
        PermissionDescriptor descriptor7 = new PermissionDescriptor();
        descriptor7.setName("geolocation");
        Permission permission7 = new Permission();
        permission7.setPermission(descriptor7);
        permission7.setState(PermissionState.Granted);
        PermissionDescriptor descriptor8 = new PermissionDescriptor();
        descriptor8.setName("midi");
        Permission permission8 = new Permission();
        permission8.setPermission(descriptor8);
        permission8.setState(PermissionState.Granted);
        context.setPermission(page2.url(), Arrays.asList(permission7, permission8));
        assertEquals("granted", getPermission(page2, "geolocation"));
        assertEquals("granted", getPermission(page2, "midi"));

        // 测试同时设置多个权限为拒绝状态
        PermissionDescriptor descriptor9 = new PermissionDescriptor();
        descriptor9.setName("geolocation");
        Permission permission9 = new Permission();
        permission9.setPermission(descriptor9);
        permission9.setState(PermissionState.Denied);
        PermissionDescriptor descriptor10 = new PermissionDescriptor();
        descriptor10.setName("midi");
        Permission permission10 = new Permission();
        permission10.setPermission(descriptor10);
        permission10.setState(PermissionState.Denied);
        context.setPermission(page2.url(), Arrays.asList(permission9, permission10));
        assertEquals("denied", getPermission(page2, "geolocation"));
        assertEquals("denied", getPermission(page2, "midi"));

        // 测试同时设置多个权限为提示状态
        PermissionDescriptor descriptor11 = new PermissionDescriptor();
        descriptor11.setName("geolocation");
        Permission permission11 = new Permission();
        permission11.setPermission(descriptor11);
        permission11.setState(PermissionState.Prompt);
        PermissionDescriptor descriptor12 = new PermissionDescriptor();
        descriptor12.setName("midi");
        Permission permission12 = new Permission();
        permission12.setPermission(descriptor12);
        permission12.setState(PermissionState.Prompt);
        context.setPermission(page2.url(), Arrays.asList(permission11, permission12));
        assertEquals("prompt", getPermission(page2, "geolocation"));
        assertEquals("prompt", getPermission(page2, "midi"));
    }


    /**
     * 模拟获取权限状态的函数
     */
    private Object getPermission(Page page, String name) {
        try {
            // 模拟JavaScript执行：navigator.permissions.query({name}).then(result => result.state)
            return page.evaluate("name => {\n" +
                    "    return navigator.permissions.query({name}).then(result => {\n" +
                    "      return result.state;\n" +
                    "    });\n" +
                    "  }", name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
