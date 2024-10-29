package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.Device;
import com.ruiyun.jvppeteer.entities.Viewport;
import org.junit.Test;

import java.util.LinkedHashMap;

public class F_PageViewPortTest extends A_LaunchTest {

    @Test
    public void test2() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            page.goTo("https://www.baidu.com");
            //修改页面大小
            page.setViewport(new Viewport(2000, 1800));
            //等待5s看看效果
            Thread.sleep(5000);
            //关闭浏览器
        }
    }

    @Test
    public void test3() throws Exception {
        //启动浏览器
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();

        LinkedHashMap<String, Integer> scrollDimensions = (LinkedHashMap<String, Integer>) page.mainFrame().isolatedRealm().evaluate("() => {\n" + "              const element = document.documentElement;\n" + "              return {\n" + "                width: element.scrollWidth,\n" + "                height: element.scrollHeight,\n" + "              };\n" + "            }");

        Viewport viewport = new Viewport();
        viewport.setWidth(scrollDimensions.get("width"));
        viewport.setHeight(scrollDimensions.get("height"));
        page.setViewport(viewport);
        page.goTo("https://www.baidu.com");
        browser.close();
    }

    @Test
    public void test4() throws Exception {
        //启动浏览器
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com");
        //模拟手机
        page.emulate(Device.HUAWEI_MATE_30_PRO);
        browser.close();
    }
}
