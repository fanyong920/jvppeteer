package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.core.CdpRequest;
import com.ruiyun.jvppeteer.cdp.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.cdp.entities.ResourceType;
import java.util.List;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Consumer;

public class M_ResponseTest extends A_LaunchTest {

    /**
     * 拦截请求
     */
    @Test
    public void test4() throws Exception {
        //打开开发者工具
        launchOptions.setDevtools(true);
        try (Browser cdpBrowser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = cdpBrowser.newPage();
            page.setRequestInterception(true);
            page.on(PageEvents.Request, (Consumer<CdpRequest>) request -> {
                if (request.resourceType().equals(ResourceType.Image)) {//拦截图片
                    request.abort();
                } else {

                    List<HeaderEntry> headers = request.headers();
                    headers.add(new HeaderEntry("foo", "bar"));
                    ContinueRequestOverrides overrides = new ContinueRequestOverrides();
                    overrides.setHeaders(headers);
                    request.continueRequest(overrides);
                }
            });
            GoToOptions options = new GoToOptions();
            //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
            options.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.domcontentloaded));
            page.goTo("https://pptr.nodejs.cn/api/puppeteer.pageevent", options);
            Thread.sleep(5000);
        }
    }
}
