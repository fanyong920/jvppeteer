package com.ruiyun.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.events.BrowserEvents;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ConnectOptions;
import com.ruiyun.jvppeteer.cdp.entities.DebugInfo;
import com.ruiyun.jvppeteer.cdp.entities.DownloadPolicy;
import com.ruiyun.jvppeteer.cdp.entities.DownloadOptions;
import com.ruiyun.jvppeteer.cdp.entities.DownloadState;
import com.ruiyun.jvppeteer.cdp.entities.Protocol;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.cdp.events.DownloadProgressEvent;
import com.ruiyun.jvppeteer.cdp.events.DownloadWillBeginEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.Test;

public class U_BroswerApiTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        launchOptions.setDebuggingPort(9222);
        Browser browser = Puppeteer.launch(launchOptions);
        browser.on(BrowserEvents.TargetCreated, (Consumer<Target>) target -> System.out.println("targetCreate: " + target.url()));
        browser.on(BrowserEvents.TargetChanged, (Consumer<Target>) target -> System.out.println("targetChanged: " + target.url()));
        browser.on(BrowserEvents.TargetDiscovered, (Consumer<TargetInfo>) targetInfo -> System.out.println("TargetDiscovered: " + targetInfo.getUrl()));
        browser.on(BrowserEvents.TargetDestroyed, (Consumer<Target>) target -> System.out.println("targetDestroyed: " + target.url()));
        browser.on(BrowserEvents.Disconnected, target -> System.out.println("disconnected: "));

        browser.defaultBrowserContext();
        BrowserContext cdpBrowserContext = browser.createBrowserContext();
        List<BrowserContext> cdpBrowserContexts = browser.browserContexts();
        for (BrowserContext context : cdpBrowserContexts) {
            System.out.println("context id: " + context.id());
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
        cdpBrowserContext.newPage();
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
        System.out.println("wsEndpoint: "+wsEndpoint);
        boolean connected = browser.connected();
        System.out.println("connected1: " + connected);
        browser.disconnect();
        System.out.println("connected2: " + browser.connected());

        //ws连接
        ConnectOptions connectOptions = new ConnectOptions();
        connectOptions.setBrowserWSEndpoint(wsEndpoint);
        //用chrome浏览器测试就改成Protocol.CDP
        connectOptions.setProtocolType(Protocol.CDP);
        Browser wsBrowser = Puppeteer.connect(connectOptions);
        int size = wsBrowser.pages().size();
        System.out.println("size1: " + size);
        String version1 = wsBrowser.version();
        System.out.println("version1: " + version1);
        wsBrowser.close();
    }

    /**
     * 下载测试
     *
     * @throws Exception 异常
     */
    @Test
    public void test5() throws Exception {
        try (Browser cdpBrowser = Puppeteer.launch(launchOptions)) {
            BrowserContext cdpBrowserContext = cdpBrowser.createBrowserContext();
            AtomicBoolean complete = new AtomicBoolean(false);
            cdpBrowser.on(BrowserEvents.DownloadProgress, (Consumer<DownloadProgressEvent>) downloadProgressEvent -> {
                System.out.println("downloadProgressEvent: " + downloadProgressEvent);
                if (downloadProgressEvent.getState().equals(DownloadState.Completed)) {
                    complete.set(true);
                }
            });
            cdpBrowser.on(BrowserEvents.DownloadWillBegin, (Consumer<DownloadWillBeginEvent>) downloadWillBeginEvent -> {
                System.out.println("downloadWillBeginEvent: " + downloadWillBeginEvent);
            });
            Page page = cdpBrowserContext.newPage();
            //配置下载行为，下载的BrowserContextId（不配置就是使用默认的浏览器上下文），下载路径，下载事件是否接受
            cdpBrowser.setDownloadBehavior(new DownloadOptions(DownloadPolicy.Allow, cdpBrowserContext.id(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\127.0.6533.99", true));
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

            cdpBrowser.setDownloadBehavior(new DownloadOptions(DownloadPolicy.Allow, cdpBrowserContext.id(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\2", true));
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
        try (Browser cdpBrowser = Puppeteer.launch(launchOptions)) {
            BrowserContext browserContext = cdpBrowser.createBrowserContext();
            Map<String, AtomicBoolean> atomicBooleanMap = new ConcurrentHashMap<>();
            cdpBrowser.on(BrowserEvents.DownloadProgress, (Consumer<DownloadProgressEvent>) downloadProgressEvent -> {
                System.out.println("downloadProgressEvent: " + downloadProgressEvent);
                if (downloadProgressEvent.getState().equals(DownloadState.Completed)) {
                    atomicBooleanMap.get(downloadProgressEvent.getGuid()).set(true);
                }
            });
            cdpBrowser.on(BrowserEvents.DownloadWillBegin, (Consumer<DownloadWillBeginEvent>) downloadWillBeginEvent -> {
                System.out.println("downloadWillBeginEvent: " + downloadWillBeginEvent);
                atomicBooleanMap.put(downloadWillBeginEvent.getGuid(), new AtomicBoolean(false));
            });
            Page page = browserContext.newPage();
            //配置下载行为，下载的BrowserContextId（不配置就是使用默认的浏览器上下文），下载路径，下载事件是否接受
            cdpBrowser.setDownloadBehavior(new DownloadOptions(DownloadPolicy.Allow, browserContext.id(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\127.0.6533.99", true));
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
     * 下载测试
     *
     * @throws Exception 异常
     */
    @Test
    public void test8() throws Exception {
        try (Browser cdpBrowser = Puppeteer.launch(launchOptions)) {
            BrowserContext browserContext = cdpBrowser.createBrowserContext();
            AtomicBoolean complete = new AtomicBoolean(false);
            cdpBrowser.on(BrowserEvents.DownloadProgress, (Consumer<DownloadProgressEvent>) downloadProgressEvent -> {
                System.out.println("下载进度: " + downloadProgressEvent);
                if (downloadProgressEvent.getState().equals(DownloadState.Completed)) {
                    complete.set(true);
                }
            });
            cdpBrowser.on(BrowserEvents.DownloadWillBegin, (Consumer<DownloadWillBeginEvent>) downloadWillBeginEvent -> {
                System.out.println("将要开始下载: " + downloadWillBeginEvent);
            });
            Page page = browserContext.newPage();
            //配置下载行为，下载的BrowserContextId（不配置就是使用默认的浏览器上下文），下载路径，下载事件是否接受
            cdpBrowser.setDownloadBehavior(new DownloadOptions(DownloadPolicy.Allow, browserContext.id(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\127.0.6533.99", true));
            page.goTo("https://archive.mozilla.org/pub/firefox/releases/91.6.0esr/jsshell/");
            //点击，进行下载
            ElementHandle elementHandle = page.$("body > table > tbody > tr:nth-child(7) > td:nth-child(2) > a");
            elementHandle.click();
            elementHandle.dispose();
            while (true) {
                if (complete.get()) {
                    System.out.println("1下载完成了 complete");
                    break;
                }
            }
            page.$("body > table > tbody > tr:nth-child(7) > td:nth-child(2) > a").click();
            System.out.println("程序结束了");
        }
    }

    /**
     * 取消下载
     *
     * @throws Exception 异常
     */
    @Test
    public void test7() throws Exception {
        try (Browser cdpBrowser = Puppeteer.launch(launchOptions)) {
            BrowserContext cdpBrowserContext = cdpBrowser.createBrowserContext();
            Map<String, AtomicBoolean> atomicBooleanMap = new ConcurrentHashMap<>();
            cdpBrowser.on(BrowserEvents.DownloadProgress, (Consumer<DownloadProgressEvent>) downloadProgressEvent -> {
                System.out.println("downloadProgressEvent: " + downloadProgressEvent);
                if (downloadProgressEvent.getState().equals(DownloadState.Completed)) {
                    atomicBooleanMap.get(downloadProgressEvent.getGuid()).set(true);
                }
            });
            cdpBrowser.on(BrowserEvents.DownloadWillBegin, (Consumer<DownloadWillBeginEvent>) downloadWillBeginEvent -> {
                System.out.println("downloadWillBeginEvent: " + downloadWillBeginEvent);
                atomicBooleanMap.put(downloadWillBeginEvent.getGuid(), new AtomicBoolean(false));
            });
            Page page = cdpBrowserContext.newPage();
            //配置下载行为，下载的BrowserContextId（不配置就是使用默认的浏览器上下文），下载路径，下载事件是否接受
            cdpBrowser.setDownloadBehavior(new DownloadOptions(DownloadPolicy.Allow, cdpBrowserContext.id(), "C:\\Users\\fanyong\\Desktop\\typescriptPri\\127.0.6533.99", true));
            page.goTo("https://mirrors.huaweicloud.com/openjdk/22.0.2/");
            //点击，进行下载
            page.click("body > pre:nth-child(4) > a:nth-child(6)");
            Thread.sleep(1000);
            //点击，进行下载
            page.click("body > pre:nth-child(4) > a:nth-child(5)");
            Thread.sleep(3000);
            //取消下载
            for (Map.Entry<String, AtomicBoolean> entry : atomicBooleanMap.entrySet()) {
                cdpBrowser.cancelDownload(entry.getKey(), cdpBrowserContext.id());
            }
        }
    }

}
