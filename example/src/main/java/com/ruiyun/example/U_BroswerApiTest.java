package com.ruiyun.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.BrowserContext;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Target;
import com.ruiyun.jvppeteer.entities.DebugInfo;
import com.ruiyun.jvppeteer.entities.TargetInfo;
import com.ruiyun.jvppeteer.entities.TargetType;
import org.junit.Test;

import java.util.List;
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

}
