package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import org.junit.Test;

public class D_PageGoForwardTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        //启动浏览器
        try ( Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            //不添加waitUntil参数，默认是load
            page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
            page.goTo("https://translate.alibaba.com/");
            //返回上一个页面
            page.goBack();
            //又往前走一个页面
            page.goForward();
            //等待5s看看效果
            Thread.sleep(5000);
        }
    }
}
