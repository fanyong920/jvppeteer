package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Request;
import com.ruiyun.jvppeteer.core.Response;
import com.ruiyun.jvppeteer.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.entities.GoToOptions;
import com.ruiyun.jvppeteer.entities.MouseWheelOptions;
import com.ruiyun.jvppeteer.entities.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.entities.ResourceType;
import com.ruiyun.jvppeteer.entities.ResponseForRequest;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class J_RequestTest extends A_LaunchTest {
    @Test
    public void test2() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            page.on(Page.PageEvent.Request, (Consumer<Request>) request -> {
                        boolean hasPostData = request.hasPostData();
                        if (hasPostData) {
                            String postData = request.postData();
                            if (StringUtil.isEmpty(postData)) {
                                postData = request.fetchPostData();
                            }
                            System.out.println("请求体：" + postData);
                        }
                    }
            );
            page.on(Page.PageEvent.Response, (Consumer<Response>) response -> {
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
                }else {
                    System.out.println("response2: " + new String(response.content()));
                }
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
        launchOptions.setDevtools(true);
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            //拦截请求开关
            page.setRequestInterception(true);
            page.on(Page.PageEvent.Request, (Consumer<Request>) request -> {
                if (request.resourceType().equals(ResourceType.Image)) {//拦截图片
                    request.abort();
                } else if (request.url().startsWith("https://www.baidu.com")) {
                    //自定义请求的response
                    ResponseForRequest responseForRequest = new ResponseForRequest();
                    //responseForRequest.setBody("Not Found!");
                    responseForRequest.setBody("百度一下，你就知道");
                    responseForRequest.setStatus(404);
                    responseForRequest.setContentType("text/plain; charset=utf-8");
                    request.continueRequest();
                } else {
                    //修改请求头
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
            page.goTo("https://www.baidu.com/", options);
            Thread.sleep(2000);
        }
    }

    /**
     * 拦截请求
     */
    @Test
    public void test5() throws Exception {
        //打开开发者工具
        launchOptions.setDevtools(true);
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            //拦截请求开关
            page.setRequestInterception(true);
            page.on(Page.PageEvent.Request, (Consumer<Request>) request -> {
                if (request.resourceType().equals(ResourceType.Image)) {//拦截图片
                    request.abort();
                } else if (request.url().startsWith("https://www.baidu.com")) {
                    //自定义请求的response
                    ResponseForRequest responseForRequest = new ResponseForRequest();
                    //responseForRequest.setBody("Not Found!");
                    responseForRequest.setBody("百度一下，你就知道");
                    responseForRequest.setStatus(404);
                    responseForRequest.setContentType("text/plain; charset=utf-8");
                    request.respond(responseForRequest,1);
                } else {
                    //修改请求头
                    Map<String, String> headers = request.headers();
                    headers.put("foo", "bar");
                    ContinueRequestOverrides overrides = new ContinueRequestOverrides();
                    overrides.setHeaders(headers);
                    request.continueRequest(overrides,2);
                }
            });
            GoToOptions options = new GoToOptions();
            //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
            options.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.DOMCONTENT_LOADED));
            page.goTo("https://www.baidu.com/", options);
            Thread.sleep(5000);
        }
    }
}
