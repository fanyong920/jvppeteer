package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.PageExtend;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PageExtendExample {

    @Test
    public void test1() throws Exception {
      
        LaunchOptions launchOptions = new LaunchOptionsBuilder().withIgnoreDefaultArgs(Collections.singletonList("--enable-automation")).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com");
        ElementHandle ele1 = PageExtend.byId(page, "s_tab");
        ElementHandle ele2 = PageExtend.byClass(page, "s_tab");
        ElementHandle ele3 = PageExtend.byTag(page, "a");
        List<ElementHandle> eleList1 = PageExtend.byTagList(page, "a");
        String html = PageExtend.html(page);
        browser.close();
    }
}
