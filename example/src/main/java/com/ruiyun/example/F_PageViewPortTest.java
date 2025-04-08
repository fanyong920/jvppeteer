package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.BoundingBox;
import com.ruiyun.jvppeteer.cdp.entities.Device;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class F_PageViewPortTest {

    @Test
    public void test2() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            //打开一个页面
            Page page = browser.newPage();
            page.goTo("https://www.baidu.com");
            Thread.sleep(2000);
            //修改页面大小
            page.setViewport(new Viewport(1200, 900));
            //等待5s看看效果
            Thread.sleep(5000);
            //关闭浏览器
        }
    }

    @Test
    public void test3() throws Exception {
        //启动浏览器
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        //打开一个页面
        Page page = browser.newPage();

        Object response = page.mainFrame().isolatedRealm().evaluate("() => {\n" + "              const element = document.documentElement;\n" + "              return {\n" + "                width: element.scrollWidth,\n" + "                height: element.scrollHeight,\n" + "              };\n" + "            }");
        BoundingBox scrollDimensions = Constant.OBJECTMAPPER.convertValue(response, BoundingBox.class);
        Viewport viewport = new Viewport();
        viewport.setWidth((int) scrollDimensions.getWidth());
        viewport.setHeight((int) scrollDimensions.getHeight());
        page.setViewport(viewport);
        page.goTo("https://www.baidu.com");
        browser.close();
    }

    @Test
    public void test4() throws Exception {
        //启动浏览器
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com");
        //模拟手机
        page.emulate(Device.HUAWEI_MATE_30_PRO);
        browser.close();
    }
}
