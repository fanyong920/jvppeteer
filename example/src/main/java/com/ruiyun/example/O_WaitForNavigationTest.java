package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Response;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.WaitForOptions;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class O_WaitForNavigationTest {

    @Test
    public void test4() throws Exception {
        //打开开发者工具
        LAUNCHOPTIONS.setDevtools(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        CDPSession session = page.target().createCDPSession();

        //方式1：只是发送reload命令，不等待完成才可以用到waitForNavigation
        session.send("Page.reload", null, null, false);
        WaitForOptions options = new WaitForOptions();
        options.setIgnoreSameDocumentNavigation(true);
        Response response = page.waitForNavigation(options);
        session.detach();
        //方式2：
        // page.reload();

        //方式1是方式2的具体实现

        String title = page.title();
        System.out.println("title: " + title);
        System.out.println(response.url());
        Thread.sleep(5000);
        browser.close();
    }


}
