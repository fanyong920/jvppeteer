package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.MouseWheelOptions;
import com.ruiyun.jvppeteer.util.Helper;
import java.util.Collections;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class I_PageMouseTest {
    @Test
    public void test2() throws Exception {
        //隐身模式启动浏览器
        LAUNCHOPTIONS.setArgs(Collections.singletonList("-private"));
        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            //打开一个页面

            Page page = browser.newPage();
            page.on(PageEvents.PageError, e -> {
                System.out.println("page error:" + e);
                //火狐浏览器在https://pptr.nodejs.cn/api/puppeteer.pageevent页面加载发生错误，刷新页面
                page.reload();
            });
            page.on(PageEvents.Error, e -> {
                System.out.println("error:" + e);
            });
            page.goTo("https://pptr.nodejs.cn/api/puppeteer.pageevent");
            //点击搜索框
            page.click("#__docusaurus > nav > div.navbar__inner > div.navbar__items.navbar__items--right > div.navbarSearchContainer_IP3a > button");
            //等待输入框出现
            page.waitForSelector("#docsearch-input");
            //在弹出的输入框输入文字,一秒输入一个文字
            page.type("#docsearch-input", "jvppeteer测试", 1000);
            //等待5s看看效果
            Thread.sleep(5000);
        }
    }

    @Test
    public void test3() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            //打开一个页面
            Page page = browser.newPage();
            page.on(PageEvents.PageError, e -> {
                System.out.println("page error:" + e);
                //页面加载发生错误，刷新页面
                page.reload();
            });
            page.goTo("https://pptr.nodejs.cn/api/puppeteer.pageevent");
            MouseWheelOptions options = new MouseWheelOptions();
            for (int i = 0; i < 5; i++) {
                Helper.justWait(1000);
                options.setDeltaY(200);
                //鼠标滚轮事件
                page.mouse().wheel(options);
            }
        }
    }
}
