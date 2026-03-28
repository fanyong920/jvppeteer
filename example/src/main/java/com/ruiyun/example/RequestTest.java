package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Request;
import com.ruiyun.jvppeteer.api.core.Response;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.CdpRequest;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieParam;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.cdp.entities.PostDataEntry;
import com.ruiyun.jvppeteer.cdp.entities.RequestPayload;
import com.ruiyun.jvppeteer.cdp.entities.ResourceTiming;
import com.ruiyun.jvppeteer.cdp.entities.ResourceType;
import com.ruiyun.jvppeteer.cdp.entities.ResponseForRequest;
import com.ruiyun.jvppeteer.cdp.events.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.ruiyun.example.LaunchTest.LAUNCHOPTIONS;
import static org.junit.Assert.assertTrue;

public class RequestTest {
    @Test
    public void test2() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            //打开一个页面
            Page page = browser.newPage();
            String version = browser.version();
            page.on(PageEvents.Request, (Consumer<Request>) request -> {
                        System.out.println("header:" + request.headers());
                        request.redirectChain().forEach(request1 -> {
                            System.out.println("url: " + request1.url() + ", id: " + request1.id());
                        });
                    }
            );
            page.on(PageEvents.Response, (Consumer<Response>) response -> {

                //cdp
                if (!version.contains("firefox")) {
                    List<Request> redirectChain = response.request().redirectChain();
                    if (!redirectChain.isEmpty()) {//不为空，说明response对应的request已经重定向,该集合记录的是重定向链
                        for (Request request : redirectChain) {
                            System.out.println("url: " + request.url() + ", id: " + request.id());
                            try {
                                Response response1 = request.response();
                                System.out.println("response1: " + new String(response1.content()));
                            } catch (Exception e) {
                                System.out.println("request(id:" + request.id() + ")重定向,不能获取response.content()");
                            }
                        }
                    } else {
                        System.out.println("response2: " + new String(response.content()));
                    }
                } else {
                    ResourceTiming timing = response.timing();
                    System.out.println("timing: " + timing);
                }


                //bidi
            });
            //这个页面有重定向请求，你可以按F12打开devtool开发者工具，自己刷新页面看网络请求
            page.goTo("https://cn.bing.com/search?q=%E5%9C%A8%E7%BA%BF%E8%A7%A3%E7%A0%81&qs=n&form=QBRE&sp=-1&lq=0&pq=%E5%9C%A8%E7%BA%BF%E8%A7%A3%E7%A0%81&sc=10-4&sk=&cvid=BD22167E4CA44C1482F65B007AE9B7CA&ghsh=0&ghacc=0&ghpl=");
            //等待5s看看效果
            Thread.sleep(5000);
        }
    }

    /**
     * 拦截请求
     */
    @Test
    public void test4() throws Exception {
        //打开开发者工具
        LAUNCHOPTIONS.setDevtools(true);
        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            //打开一个页面
            Page page = browser.newPage();
            String version = browser.version();
            //拦截请求开关
            page.setRequestInterception(true);
            page.on(PageEvents.Request, (Consumer<Request>) request -> {
                if (version.contains("firefox")) {
                    if (request.url().startsWith("https://www.baidu.com")) {
                        //自定义请求的response
                        ResponseForRequest responseForRequest = new ResponseForRequest();
                        //responseForRequest.setBody("Not Found!");
                        responseForRequest.setBody("百度一下，你就知道");
                        responseForRequest.setStatus(404);
                        responseForRequest.setContentType("text/plain; charset=utf-8");
                        request.respond(responseForRequest);
                    } else {
                        //修改请求头
                        List<HeaderEntry> headers = request.headers();
                        headers.add(new HeaderEntry("foo", "bar"));
                        ContinueRequestOverrides overrides = new ContinueRequestOverrides();
                        overrides.setHeaders(headers);
                        request.continueRequest(overrides);
                    }
                } else {
                    if (request.resourceType().equals(ResourceType.Image)) {//拦截图片
                        request.abort();
                    } else if (request.url().startsWith("https://www.baidu.com")) {
                        //自定义请求的response
                        ResponseForRequest responseForRequest = new ResponseForRequest();
                        //responseForRequest.setBody("Not Found!");
                        responseForRequest.setBody("百度一下，你就知道");
                        responseForRequest.setStatus(404);
                        responseForRequest.setContentType("text/plain; charset=utf-8");
                        responseForRequest.setHeaders(request.headers());
                        request.respond(responseForRequest);
                    } else {
                        //修改请求头
                        List<HeaderEntry> headers = request.headers();
                        headers.add(new HeaderEntry("foo", "bar"));
                        ContinueRequestOverrides overrides = new ContinueRequestOverrides();
                        overrides.setHeaders(headers);
                        request.continueRequest(overrides);
                    }
                }

            });
            GoToOptions options = new GoToOptions();
            //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
            options.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.domcontentloaded));
            page.goTo("https://www.baidu.com/", options);
            Thread.sleep(5000);
        }
    }

    /**
     * 拦截请求
     */
    @Test
    public void test5() throws Exception {
        //打开开发者工具
        LAUNCHOPTIONS.setDevtools(true);
        try (Browser cdpBrowser = Puppeteer.launch(LAUNCHOPTIONS)) {
            //打开一个页面
            Page page = cdpBrowser.newPage();
            //拦截请求开关
            page.setRequestInterception(true);
            page.on(PageEvents.Request, (Consumer<CdpRequest>) request -> {
                if (request.resourceType().equals(ResourceType.Image)) {//拦截图片
                    request.abort();
                } else if (request.url().startsWith("https://www.baidu.com")) {
                    //自定义请求的response
                    ResponseForRequest responseForRequest = new ResponseForRequest();
                    //responseForRequest.setBody("Not Found!");
                    responseForRequest.setBody("百度一下，你就知道");
                    responseForRequest.setStatus(404);
                    responseForRequest.setContentType("text/plain; charset=utf-8");
                    request.respond(responseForRequest, 1);
                } else {
                    //修改请求头
                    List<HeaderEntry> headers = request.headers();
                    headers.add(new HeaderEntry("foo", "bar"));
                    ContinueRequestOverrides overrides = new ContinueRequestOverrides();
                    overrides.setHeaders(headers);
                    request.continueRequest(overrides, 2);
                }
            });
            GoToOptions options = new GoToOptions();
            //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
            options.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.domcontentloaded));
            page.goTo("https://www.baidu.com/", options);
            Thread.sleep(5000);
        }
    }

    @Test
    public void test13() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        List<Cookie> cookies = page.cookies();
        for (Cookie cookie : cookies) {
            cookie.setHttpOnly(true);
        }
        CookieParam cookieParam = new CookieParam();
        cookieParam.setName("username");
        cookieParam.setValue("John Doe");
        cookieParam.setHttpOnly(true);
        cookieParam.setUrl("https://www.baidu.com/");
        page.setCookie(cookieParam);

        Response response = page.goTo("https://www.baidu.com/");
        response.request().headers().forEach(headerEntry -> {
            System.out.println("header:" + headerEntry);
        });
        browser.close();
    }

    @Test
    public void test14() throws Exception {
        // 测试用例1: 应该从postDataEntries重建postData
        CDPSession client = null; // 模拟CDPSession对象
        Frame frame = null; // 模拟Frame对象

        // 创建请求数据
        RequestWillBeSentEvent requestData1 = new RequestWillBeSentEvent();
        requestData1.setRequestId("requestId");

        RequestPayload requestInfo1 = new RequestPayload();
        requestInfo1.setUrl("http://example.com");
        requestInfo1.setMethod("POST");
        requestInfo1.setHeaders(new java.util.HashMap<>());

        List<PostDataEntry> entries1 = new ArrayList<>();
        PostDataEntry entry1 = new PostDataEntry();
        entry1.setBytes(Base64.getEncoder().encodeToString("part1".getBytes())); // 相当于btoa('part1')
        entries1.add(entry1);

        PostDataEntry entry2 = new PostDataEntry();
        entry2.setBytes(Base64.getEncoder().encodeToString("part2".getBytes())); // 相当于btoa('part2')
        entries1.add(entry2);

        requestInfo1.setPostDataEntries(entries1);
        requestData1.setRequest(requestInfo1);

        CdpRequest request1 = new CdpRequest(client, frame, "interceptionId", true, requestData1, new ArrayList<>());
        assertTrue("should be part1part2", request1.postData().equals("part1part2"));


        // 测试用例2: 如果postDataEntries缺失，则回退到postData
        RequestWillBeSentEvent requestData2 = new RequestWillBeSentEvent();
        requestData2.setRequestId("requestId");

        RequestPayload requestInfo2 = new RequestPayload();
        requestInfo2.setUrl("http://example.com");
        requestInfo2.setMethod("POST");
        requestInfo2.setHeaders(new java.util.HashMap<>());
        requestInfo2.setPostData("originalData"); // 设置postData而不是postDataEntries

        requestData2.setRequest(requestInfo2);

        CdpRequest request2 = new CdpRequest(client, frame, "interceptionId", true, requestData2, new ArrayList<>());

        assertTrue("should be originalData", "originalData".equals(request2.postData()));
    }
}
