package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import java.util.ArrayList;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class Z_ProxyTest {
    @Test
    public void test1() throws Exception {
        ArrayList<String> args = new ArrayList<>();//添加一些额外的启动参数
        // Launch chromium using a proxy server on port 9876.
        // More on proxying:
        //    https://www.chromium.org/developers/design-documents/network-settings
        args.add("--proxy-server=127.0.0.1:9876");
        // Use proxy for localhost URLs
        args.add("--proxy-bypass-list=<-loopback>");
        LAUNCHOPTIONS.setProtocolTimeout(180_000);
        LAUNCHOPTIONS.setTimeout(180_000);
        LAUNCHOPTIONS.setArgs(args);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com");
        browser.close();

    }
}
