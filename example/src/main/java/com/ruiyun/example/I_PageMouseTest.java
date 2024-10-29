package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.MouseWheelOptions;
import org.junit.Test;

public class I_PageMouseTest extends A_LaunchTest {
    @Test
    public void test2() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
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
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            page.goTo("https://pptr.nodejs.cn/api/puppeteer.pageevent");
            MouseWheelOptions options = new MouseWheelOptions();
//        options.setDeltaX(100);
            options.setDeltaY(2000);
            //鼠标滚轮事件
            page.mouse().wheel(options);
            //等待5s看看效果
            Thread.sleep(5000);
        }
    }
}
