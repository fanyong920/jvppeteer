package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Response;
import com.ruiyun.jvppeteer.entities.WaitForOptions;
import com.ruiyun.jvppeteer.transport.CDPSession;
import org.junit.Test;

public class O_WaitForNavigationTest extends A_LaunchTest {

    @Test
    public void test4() throws Exception {
        //打开开发者工具
        launchOptions.setDevtools(true);
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        CDPSession cdpSession = page.target().createCDPSession();

        //方式1：只是发送reload命令，不等待完成才可以用到waitForNavigation
        cdpSession.send("Page.reload", null, null, false);
        WaitForOptions options = new WaitForOptions();
        options.setIgnoreSameDocumentNavigation(true);
        Response response = page.waitForNavigation(options);
        cdpSession.detach();
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
