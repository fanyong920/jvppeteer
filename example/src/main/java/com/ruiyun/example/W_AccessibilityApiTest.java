package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.SerializedAXNode;
import com.ruiyun.jvppeteer.cdp.entities.SnapshotOptions;
import com.ruiyun.jvppeteer.common.Constant;
import org.junit.Test;

public class W_AccessibilityApiTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.setContent("<textarea>hi</textarea>");
        page.focus("textarea");
        SerializedAXNode snapshot = page.accessibility().snapshot(new SnapshotOptions(true, null, false));
        System.out.println(snapshot);
    }

}
