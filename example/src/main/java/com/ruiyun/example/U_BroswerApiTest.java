package com.ruiyun.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.BrowserContext;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Target;
import com.ruiyun.jvppeteer.entities.*;
import com.ruiyun.jvppeteer.events.DownloadProgressEvent;
import com.ruiyun.jvppeteer.events.DownloadWillBeginEvent;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class U_BroswerApiTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        launchOptions.setDebuggingPort(9222);
        Browser browser = Puppeteer.launch(launchOptions);
        browser.on(Browser.BrowserEvent.TargetCreated, (Consumer<Target>) target -> System.out.println("targetCreate: " + target.url()));
        browser.on(Browser.BrowserEvent.TargetChanged, (Consumer<Target>) target -> System.out.println("targetChanged: " + target.url()));
        browser.on(Browser.BrowserEvent.TargetDiscovered, (Consumer<TargetInfo>) targetInfo -> System.out.println("TargetDiscovered: " + targetInfo.getUrl()));
        browser.on(Browser.BrowserEvent.TargetDestroyed, (Consumer<Target>) target -> System.out.println("targetDestroyed: " + target.url()));
        browser.on(Browser.BrowserEvent.Disconnected, target -> System.out.println("disconnected: "));

        browser.defaultBrowserContext();
        BrowserContext browserContext = browser.createBrowserContext();
        List<BrowserContext> browserContexts = browser.browserContexts();
        for (BrowserContext context : browserContexts) {
            System.out.println("context id: " + context.getId());
        }
        String version = browser.version();
        System.out.println("version: " + version);
        Target target1 = browser.target();
        System.out.println("browser target type: " + target1.type());
        browser.targets().forEach(target -> System.out.println("target type: " + target.type() + ", url:" + target.url()));
        Page page = browser.newPage();
        page.goTo("https://example.com");
        page.close();
        System.out.println("browser userAgent1: " + browser.userAgent());
        browserContext.newPage();
        browser.createPageInContext(browserContext.getId());
        browser.pages().forEach(page1 -> {
                    try {
                        System.out.println("page title: " + page1.title());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        DebugInfo debugInfo = browser.debugInfo();
        System.out.println(debugInfo);
        String wsEndpoint = browser.wsEndpoint();
        boolean connected = browser.connected();
        System.out.println("connected1: " + connected);
        browser.disconnect();
        System.out.println("connected2: " + browser.connected());

        //ws连接
        Browser wsBrowser = Puppeteer.connect(wsEndpoint);
        int size = wsBrowser.pages().size();
        System.out.println("size1: " + size);
        wsBrowser.disconnect();

        //url连接 http://host:port  因为启动时候配置DebuggingPort=9222  所以url = localhost:9222
        Browser urlBrowser = Puppeteer.connect("http://localhost:9222");
        int size2 = urlBrowser.pages().size();
        System.out.println("size2: " + size2);


        new Thread(urlBrowser::newPage).start();
        Target target2 = urlBrowser.waitForTarget(target -> target.type().equals(TargetType.PAGE));
        System.out.println("waitForTarget:" + target2.type());
        urlBrowser.close();
    }

    /**
     * 下载测试
     *
     * @throws Exception 异常
     */
    @Test
    public void test5() throws Exception {
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            BrowserContext browserContext = browser.createBrowserContext();
            AtomicBoolean complete = new AtomicBoolean(false);
            browser.on(Browser.BrowserEvent.Browser_downloadProgress, (Consumer<DownloadProgressEvent>) downloadProgressEvent -> {
                System.out.println("downloadProgressEvent: " + downloadProgressEvent);
                if (downloadProgressEvent.getState().equals(DownloadState.Completed)) {
                    complete.set(true);
                }
            });
            browser.on(Browser.BrowserEvent.Browser_downloadWillBegin, (Consumer<DownloadWillBeginEvent>) downloadWillBeginEvent -> {
                System.out.println("downloadWillBeginEvent: " + downloadWillBeginEvent);
            });
            Page page = browserContext.newPage();
            //配置下载行为，下载的BrowserContextId（不配置就是使用默认的浏览器上下文），下载路径，下载事件是否接受
            browser.setDownloadBehavior(new DownloadOptions(DownloadBehavior.Allow, browserContext.getId(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\127.0.6533.99", true));
            page.goTo("https://mirrors.huaweicloud.com/openjdk/22.0.2/");
            //点击，进行下载
            page.click("body > pre:nth-child(4) > a:nth-child(6)");
            while (true) {
                if (complete.get()) {
                    System.out.println("1下载完成了 complete");
                    break;
                }
            }
            //下载第二次
            //配置下载行为，下载的BrowserContextId（不配置就是使用默认的浏览器上下文），下载路径，下载事件是否接受

            browser.setDownloadBehavior(new DownloadOptions(DownloadBehavior.Allow, browserContext.getId(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\2", true));
            //点击，进行下载
            page.click("body > pre:nth-child(4) > a:nth-child(5)");
            complete.set(false);
            while (true) {
                if (complete.get()) {
                    System.out.println("2下载完成了 complete");
                    break;
                }
            }
        }
    }

    /**
     * 下载测试 ：同时下载
     *
     * @throws Exception 异常
     */
    @Test
    public void test6() throws Exception {
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            BrowserContext browserContext = browser.createBrowserContext();
            Map<String, AtomicBoolean> atomicBooleanMap = new ConcurrentHashMap<>();
            browser.on(Browser.BrowserEvent.Browser_downloadProgress, (Consumer<DownloadProgressEvent>) downloadProgressEvent -> {
                System.out.println("downloadProgressEvent: " + downloadProgressEvent);
                if (downloadProgressEvent.getState().equals(DownloadState.Completed)) {
                    atomicBooleanMap.get(downloadProgressEvent.getGuid()).set(true);
                }
            });
            browser.on(Browser.BrowserEvent.Browser_downloadWillBegin, (Consumer<DownloadWillBeginEvent>) downloadWillBeginEvent -> {
                System.out.println("downloadWillBeginEvent: " + downloadWillBeginEvent);
                atomicBooleanMap.put(downloadWillBeginEvent.getGuid(), new AtomicBoolean(false));
            });
            Page page = browserContext.newPage();
            //配置下载行为，下载的BrowserContextId（不配置就是使用默认的浏览器上下文），下载路径，下载事件是否接受
            browser.setDownloadBehavior(new DownloadOptions(DownloadBehavior.Allow, browserContext.getId(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\127.0.6533.99", true));
            page.goTo("https://mirrors.huaweicloud.com/openjdk/22.0.2/");
            //点击，进行下载
            page.click("body > pre:nth-child(4) > a:nth-child(6)");
            Thread.sleep(1000);
            //点击，进行下载
            page.click("body > pre:nth-child(4) > a:nth-child(5)");
            while (true) {
                boolean complete = true;
                for (Map.Entry<String, AtomicBoolean> entry : atomicBooleanMap.entrySet()) {
                    if (!entry.getValue().get()) {
                        complete = false;
                        break;
                    }
                }
                if (complete) {
                    System.out.println("两个都下载完成了，complete");
                    break;
                }
            }
        }
    }

    /**
     * 取消下载
     *
     * @throws Exception 异常
     */
    @Test
    public void test7() throws Exception {
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            BrowserContext browserContext = browser.createBrowserContext();
            Map<String, AtomicBoolean> atomicBooleanMap = new ConcurrentHashMap<>();
            browser.on(Browser.BrowserEvent.Browser_downloadProgress, (Consumer<DownloadProgressEvent>) downloadProgressEvent -> {
                System.out.println("downloadProgressEvent: " + downloadProgressEvent);
                if (downloadProgressEvent.getState().equals(DownloadState.Completed)) {
                    atomicBooleanMap.get(downloadProgressEvent.getGuid()).set(true);
                }
            });
            browser.on(Browser.BrowserEvent.Browser_downloadWillBegin, (Consumer<DownloadWillBeginEvent>) downloadWillBeginEvent -> {
                System.out.println("downloadWillBeginEvent: " + downloadWillBeginEvent);
                atomicBooleanMap.put(downloadWillBeginEvent.getGuid(), new AtomicBoolean(false));
            });
            Page page = browserContext.newPage();
            //配置下载行为，下载的BrowserContextId（不配置就是使用默认的浏览器上下文），下载路径，下载事件是否接受
            browser.setDownloadBehavior(new DownloadOptions(DownloadBehavior.Allow, browserContext.getId(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\127.0.6533.99", true));
            page.goTo("https://mirrors.huaweicloud.com/openjdk/22.0.2/");
            //点击，进行下载
            page.click("body > pre:nth-child(4) > a:nth-child(6)");
            Thread.sleep(1000);
            //点击，进行下载
            page.click("body > pre:nth-child(4) > a:nth-child(5)");
            Thread.sleep(3000);
            //取消下载
            for (Map.Entry<String, AtomicBoolean> entry : atomicBooleanMap.entrySet()) {
                browser.cancelDownload(entry.getKey(), browserContext.getId());
            }
        }
    }

}
