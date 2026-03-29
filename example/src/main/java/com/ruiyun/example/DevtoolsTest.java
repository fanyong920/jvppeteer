package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import org.junit.Test;

import static com.ruiyun.example.LaunchTest.LAUNCHOPTIONS;
import static org.junit.Assert.assertTrue;

public class DevtoolsTest {

    /**
     * Page.openDevTools()
     *
     * @throws Exception 异常
     */
    @Test
    public void test1() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        page.goTo("about:blank");
        Page openDevTools = page.openDevTools();
        openDevTools.waitForFunction("() => {\n" +
                "      // @ts-expect-error wrong context.\n" +
                "      return Boolean(window.DevToolsAPI);\n" +
                "    }");
        Thread.sleep(5000);
        browser.close();
    }

    /**
     * Page.hasDevTools()
     *
     * @throws Exception 异常
     */
    @Test
    public void test2() throws Exception {
        LAUNCHOPTIONS.setDevtools(false);
        //should report correctly after DevTools is opened
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        page.goTo("about:blank");
        assertTrue(false == page.hasDevTools());
        Page openDevTools = page.openDevTools();
        assertTrue(true == page.hasDevTools());
        openDevTools.waitForFunction("() => {\n" +
                "      // @ts-expect-error wrong context.\n" +
                "      return Boolean(window.DevToolsAPI);\n" +
                "    }");
        browser.close();
    }

    /**
     * Page.hasDevTools()
     *
     * @throws Exception 异常
     */
    @Test
    public void test3() throws Exception {
        //should report when DevTools is attached by default
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        page.goTo("about:blank");
        assertTrue(false == page.hasDevTools());
        browser.close();
    }

    @Test
    public void test4() throws Exception {
        LAUNCHOPTIONS.setDevtools(false);
        //should report when DevTools is attached by default
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        page.goTo("about:blank");
        page.openDevTools();
        assertTrue(true == page.hasDevTools());
        browser.close();
    }
}
