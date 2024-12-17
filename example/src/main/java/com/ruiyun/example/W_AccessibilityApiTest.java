package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.SerializedAXNode;
import org.junit.Test;

public class W_AccessibilityApiTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://example.com");
        SerializedAXNode snapshot = page.accessibility().snapshot();
        System.out.println(snapshot);
    }

}
