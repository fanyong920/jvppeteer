package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.CdpRequest;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.cdp.entities.ResourceType;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.util.Helper;
import org.junit.Test;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class M_ResponseTest {

    /**
     * 拦截请求
     */
    @Test
    public void test4() throws Exception {
        //打开开发者工具
        LAUNCHOPTIONS.setDevtools(true);
        try (Browser cdpBrowser = Puppeteer.launch(LAUNCHOPTIONS)) {
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

    @Test
    public void test5() throws Exception {
        // 正常UTF-8内容
        String normalContent = "Hello 世界";
        byte[] normalBytes = normalContent.getBytes(StandardCharsets.UTF_8);
        String result = Helper.decodeUtf8(normalBytes);
        System.out.println("正常解码结果: " + result);

        // 尝试解码无效的字节序列（会导致抛出异常）
        byte[] invalidBytes = {(byte) 0xFF, (byte) 0xFE, (byte) 0xFD}; // 无效的UTF-8序列

        try {
            String invalidResult = Helper.decodeUtf8(invalidBytes);
            System.out.println("无效字节解码结果: " + invalidResult);
        } catch (CharacterCodingException e) {
            System.out.println("正确捕获了致命错误: " + e.getMessage());
        }

    }
}
