package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.SerializedAXNode;
import org.junit.Test;

public class W_AccessibilityApiTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        Browser browser = Puppeteer.launch();
        Page page = browser.newPage();
        page.goTo("https://example.com");
        SerializedAXNode snapshot = page.accessibility().snapshot();
        System.out.println(snapshot);
    }

}
