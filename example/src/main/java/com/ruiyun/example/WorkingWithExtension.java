package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Extension;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.core.WebWorker;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.api.events.WebWorkerEvent;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.util.Helper;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.ruiyun.example.LaunchTest.LAUNCHOPTIONS;

public class WorkingWithExtension {
    private String extensionPath = "E:\\puppeteer\\test\\assets\\simple-extension";
    private String extensionWithPagePath = "E:\\puppeteer\\test\\assets\\extension-with-page";

    @Test
    public void test1() throws Exception {
        ArrayList<String> additionalArgs = new ArrayList<>();
        additionalArgs.add("--no-sandbox");
        additionalArgs.add("--disable-setuid-sandbox");
        //指定插件所在的文件夹,如果手上暂时没有插件，可以我个人的插件https://github.com/fanyong920/crawlItem.git 克隆下来即可
        String pathToExtension = "E:\\puppeteer\\test\\assets\\simple-extension";
        additionalArgs.add("--disable-extensions-except=" + pathToExtension);
        additionalArgs.add("--load-extension=" + pathToExtension);
        //插件的加载在有头模式下才能生效
        Browser cdpBrowser = Puppeteer.launch(LaunchOptions.builder().args(additionalArgs).headless(false).build());
        List<Target> targets = cdpBrowser.targets();
        for (Target target : targets) {
            if (TargetType.BACKGROUND_PAGE.equals(target.type())) {
                System.out.println("目标类型=" + target.type());
            }
        }
        cdpBrowser.close();
    }

    /**
     * service_worker target type should be available
     *
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        LAUNCHOPTIONS.setEnableExtensions(true);
        LAUNCHOPTIONS.setPipe(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        String extensionId = browser.installExtension(extensionPath);
        System.out.println("extensionId=" + extensionId);
        Target serviceWorkerTarget = browser.waitForTarget(target -> target.type().equals(TargetType.SERVICE_WORKER));
        Assert.assertNotNull(serviceWorkerTarget);
        browser.uninstallExtension(extensionId);
        assertNoServiceWorkerReported(browser.targets(), extensionId);
        browser.close();
    }

    /**
     * can evaluate in the service worker
     *
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        LAUNCHOPTIONS.setEnableExtensions(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        String extensionId = browser.installExtension(extensionPath);
        System.out.println("extensionId=" + extensionId);
        Target serviceWorkerTarget = browser.waitForTarget(target -> target.type().equals(TargetType.SERVICE_WORKER));
        Assert.assertNotNull(serviceWorkerTarget);
        WebWorker worker = serviceWorkerTarget.worker();
        String result = "";
        Helper.justWait(50000);
        for (int i = 0; i < 5; i++) {
            try {
                result = worker.evaluate("() => {\n" +
                        "          // @ts-expect-error different context.\n" +
                        "          return globalThis.MAGIC;\n" +
                        "        }").toString();
                break;
            } catch (Exception e) {
                System.out.println("evaluate error=" + e.getMessage());
            }
            Helper.justWait(200);
        }
        System.out.println("result=" + result);
        Assert.assertEquals(result, 42);
        browser.uninstallExtension(extensionId);
        assertNoServiceWorkerReported(browser.targets(), extensionId);
        browser.close();
    }

    @Test
    public void test4() throws Exception {
        LAUNCHOPTIONS.setEnableExtensions(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        String extensionId = browser.installExtension(extensionPath);
        System.out.println("extensionId=" + extensionId);
        Target target = browser.waitForTarget(t -> t.type().equals(TargetType.SERVICE_WORKER) && t.url().contains(extensionId));
        Assert.assertNotNull(target);
        Map<String, Extension> extensions = browser.extensions();
        Extension extension = extensions.get(extensionId);
        Assert.assertNotNull(extension);
        Assert.assertEquals("Simple extension", extension.getName());
        Assert.assertEquals("0.1", extension.getVersion());
        Assert.assertEquals(extensionPath, extension.getPath());
        Assert.assertEquals(true, extension.isEnabled());
        Assert.assertEquals(extensionId, extension.getId());
        browser.uninstallExtension(extensionId);
        assertNoServiceWorkerReported(browser.targets(), extensionId);
        browser.close();
    }

    public void assertNoServiceWorkerReported(List<Target> targets, String id) {
        Optional<Target> first = targets.stream().filter(target -> target.url().contains(id) && target.type().equals(TargetType.SERVICE_WORKER)).findFirst();
        Assert.assertFalse(first.isPresent());
    }

    /**
     * should list extension workers
     *
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        LAUNCHOPTIONS.setEnableExtensions(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        String extensionId = browser.installExtension(extensionPath);
        Map<String, Extension> extensions = browser.extensions();
        Extension extension = extensions.get(extensionId);
        Page page = browser.newPage();
        if (Objects.nonNull(extension)) {
            extension.triggerAction(page);
        }
        browser.waitForTarget(t -> t.type().equals(TargetType.SERVICE_WORKER) && t.url().contains(extensionId));
        Assert.assertNotNull(extension);
        List<WebWorker> workers = extension.workers();
        Assert.assertTrue(!workers.isEmpty());
        browser.uninstallExtension(extensionId);
        assertNoServiceWorkerReported(browser.targets(), extensionId);
        browser.close();
    }

    /**
     * should trigger extension action
     *
     * @throws Exception
     */
    @Test
    public void test6() throws Exception {
        LAUNCHOPTIONS.setEnableExtensions(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        String extensionId = browser.installExtension(extensionPath);
        Map<String, Extension> extensions = browser.extensions();
        Extension extension = extensions.get(extensionId);
        Assert.assertNotNull(extension);
        page.triggerExtensionAction(extension);
        // If it doesn't throw, we consider it successful for this level of testing.
        Target target = browser.waitForTarget(t -> t.type().equals(TargetType.SERVICE_WORKER) && t.url().contains(extensionId));
        Assert.assertNotNull(target);
        browser.uninstallExtension(extensionId);
        assertNoServiceWorkerReported(browser.targets(), extensionId);
        browser.close();
    }

    /**
     * should list extension pages
     *
     * @throws Exception
     */
    @Test
    public void test7() throws Exception {
        LAUNCHOPTIONS.setEnableExtensions(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);

        String extensionId = browser.installExtension(extensionWithPagePath);
        Map<String, Extension> extensions = browser.extensions();
        Extension extension = extensions.get(extensionId);
        Assert.assertNotNull(extension);

        Page page = browser.newPage();
        page.goTo("https://www.example.com");

        page.triggerExtensionAction(extension);

        // If it doesn't throw, we consider it successful for this level of testing.
        Target target = browser.waitForTarget(t -> t.type().equals(TargetType.SERVICE_WORKER) && t.url().contains(extensionId));
        Assert.assertNotNull(target);

        Target optionsPageTarget = browser.waitForTarget(t -> t.url().contains(extensionId) && t.url().contains("popup.html"));
        Page extPage = optionsPageTarget.asPage();
        extPage.on(PageEvents.Console, (ConsoleMessage consoleMessage) -> {
            System.out.println("consoleMessage=" + consoleMessage.text());
            Assert.assertEquals(consoleMessage.text(), "hello from extension page");
        });
        extPage.evaluate("() => {\n" +
                "        console.log('hello from extension page');\n" +
                "      }");

        browser.uninstallExtension(extensionId);
        assertNoServiceWorkerReported(browser.targets(), extensionId);
        browser.close();
    }


    /**
     * should capture console logs from extension pages
     *
     * @throws Exception
     */
    @Test
    public void test8() throws Exception {
        LAUNCHOPTIONS.setEnableExtensions(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        String extensionId = browser.installExtension(extensionWithPagePath);
        Map<String, Extension> extensions = browser.extensions();
        Extension extension = extensions.get(extensionId);
        Assert.assertNotNull(extension);

        Page page = browser.newPage();
        page.goTo("https://www.example.com");
        extension.triggerAction(page);

        // If it doesn't throw, we consider it successful for this level of testing.
        Target target = browser.waitForTarget(t -> t.type().equals(TargetType.SERVICE_WORKER) && t.url().contains(extensionId));
        Assert.assertNotNull(target);
        WebWorker worker = target.worker();
        AwaitableResult<ConsoleMessage> consoleMessageResult = new AwaitableResult<>();
        String messageToLog = "hello from extension worker";
        worker.on(WebWorkerEvent.Console, (ConsoleMessage consoleMessage) -> {

            if (consoleMessage.text().equals(messageToLog)) {
                System.out.println("consoleMessage=" + consoleMessage.text());
                consoleMessageResult.complete();
            }
        });
        worker.evaluate("msg => {\n" +
                "        console.log(msg);\n" +
                "      }", Collections.singletonList(messageToLog));

        consoleMessageResult.waiting();
        browser.uninstallExtension(extensionId);
        assertNoServiceWorkerReported(browser.targets(), extensionId);
        browser.close();
    }

    /**
     * should remove extension from list after uninstall
     *
     * @throws Exception
     */
    @Test
    public void test9() throws Exception {
        LAUNCHOPTIONS.setEnableExtensions(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        String extensionId = browser.installExtension(extensionPath);

        // If it doesn't throw, we consider it successful for this level of testing.
        Target target = browser.waitForTarget(t -> t.type().equals(TargetType.SERVICE_WORKER) && t.url().contains(extensionId));
        Assert.assertNotNull(target);

        Map<String, Extension> extensions = browser.extensions();
        Extension extension = extensions.get(extensionId);
        Assert.assertNotNull(extension);

        browser.uninstallExtension(extensionId);
        assertNoServiceWorkerReported(browser.targets(), extensionId);

        extensions = browser.extensions();
        extension = extensions.get(extensionId);
        Assert.assertNull(extension);
        browser.close();
    }
}
