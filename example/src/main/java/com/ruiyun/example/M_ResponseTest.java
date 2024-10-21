package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Request;
import com.ruiyun.jvppeteer.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.entities.GoToOptions;
import com.ruiyun.jvppeteer.entities.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.entities.ResourceType;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class M_ResponseTest extends A_LaunchTest {

    /**
     * 拦截请求
     */
    @Test
    public void test4() throws Exception {
        //打开开发者工具
        launchOptions.setDevtools(true);
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.setRequestInterception(true);
        page.on(Page.PageEvent.Request, (Consumer<Request>) request -> {
            if (request.resourceType().equals(ResourceType.Image)) {//拦截图片
                request.abort();
            } else {

                Map<String, String> headers = request.headers();
                headers.put("foo", "bar");
                ContinueRequestOverrides overrides = new ContinueRequestOverrides();
                overrides.setHeaders(headers);
                request.continueRequest(overrides);
            }
        });
        GoToOptions options = new GoToOptions();
        //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
        options.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.DOMCONTENT_LOADED));
        page.goTo("https://pptr.nodejs.cn/api/puppeteer.pageevent", options);
        Thread.sleep(5000);
        browser.close();
    }
}
