package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Response;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import org.junit.Test;

public class C_PageGoBackTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        //启动浏览器
        try (Browser cdpBrowser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = cdpBrowser.newPage();
            //不添加waitUntil参数，默认是load
            page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
            page.goTo("https://translate.alibaba.com/");
            //返回上一个页面
            Response response = page.goBack();
            System.out.println(response.url()+"  "+response.ok());
            //等待5s看看效果
            Thread.sleep(5000);
        }
    }
}
