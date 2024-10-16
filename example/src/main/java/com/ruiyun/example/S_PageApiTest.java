package com.ruiyun.example;

import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.PredefinedNetworkConditions;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.ScreenRecorder;
import com.ruiyun.jvppeteer.common.WebPermission;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.ElementHandle;
import com.ruiyun.jvppeteer.core.FileChooser;
import com.ruiyun.jvppeteer.core.Frame;
import com.ruiyun.jvppeteer.core.JSHandle;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Request;
import com.ruiyun.jvppeteer.core.Response;
import com.ruiyun.jvppeteer.core.Target;
import com.ruiyun.jvppeteer.core.WebWorker;
import com.ruiyun.jvppeteer.entities.BoundingBox;
import com.ruiyun.jvppeteer.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.entities.Cookie;
import com.ruiyun.jvppeteer.entities.CoverageEntry;
import com.ruiyun.jvppeteer.entities.Credentials;
import com.ruiyun.jvppeteer.entities.FrameAddScriptTagOptions;
import com.ruiyun.jvppeteer.entities.GeolocationOptions;
import com.ruiyun.jvppeteer.entities.IdleOverridesState;
import com.ruiyun.jvppeteer.entities.JSCoverageEntry;
import com.ruiyun.jvppeteer.entities.JSCoverageOptions;
import com.ruiyun.jvppeteer.entities.MediaFeature;
import com.ruiyun.jvppeteer.entities.Metrics;
import com.ruiyun.jvppeteer.entities.NewDocumentScriptEvaluation;
import com.ruiyun.jvppeteer.entities.ScreenCastFormat;
import com.ruiyun.jvppeteer.entities.ScreencastOptions;
import com.ruiyun.jvppeteer.entities.Viewport;
import com.ruiyun.jvppeteer.entities.VisionDeficiency;
import com.ruiyun.jvppeteer.entities.WaitForSelectorOptions;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class S_PageApiTest extends A_LaunchTest {
    /**
     * goto的几种方式
     */
    @Test
    public void test3() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        //使用默认的参数访问url
        //1.等待页面加载完成，默认加载到load阶段算完成，有四个阶段可选择，具体见PuppeteerLifeCycle类，有返回结果
        //2.超时时间和和响应的referrerPolicy referer也采用默认值
        page.goTo("https://pptr.nodejs.cn/api/puppeteer.page._");
        //打开该页面的browser
        Browser browser1 = page.browser();
        System.out.println("browser对比结果：" + (browser1 == browser));
        //获取页面标题
        String title = page.title();
        System.out.println("title：" + title);
        String content = page.content();
        System.out.println("content：" + content);
        String url = page.url();
        System.out.println("url：" + url);
        //页面大小
        Viewport viewport = page.viewport();
        System.out.println("viewport：" + viewport);
        Target target = page.target();
        System.out.println("target：" + target.type());
        //获取页面性能指标
        Metrics metrics = page.metrics();
        System.out.println("metrics：" + metrics);
        List<Cookie> cookies = page.cookies();
        for (Cookie cookie : cookies) {
            System.out.println("cookie: " + cookie);
        }
        List<Cookie> cookies1 = page.cookies("https://pptr.nodejs.cn/");
        for (Cookie cookie : cookies1) {
            System.out.println("cookies1：" + cookie);
        }
        //删除指定名称的cookie
        page.deleteCookie("__51uvsct__3GwiskEACuQk7V0k", "__51vcke__3GwiskEACuQk7V0k");
        List<Cookie> cookies2 = page.cookies();
        for (Cookie cookie : cookies2) {
            System.out.println("cookies2：" + cookie);
        }
        Page page1 = browser.newPage();
        //不等待页面加载完成，就直接返回，单纯是发送访问url的请求
        page1.goTo("https://pptr.nodejs.cn/api/puppeteer.page._", false);
        //这样我们就可以等待某个元素出现了
        ElementHandle elementHandle = page1.waitForSelector("#__docusaurus_skipToContent_fallback > div > div > main > div > div > div.col.docItemCol_nDJs > div > nav > a.pagination-nav__link.pagination-nav__link--prev > div.pagination-nav__label");

        //这个元素是可以点击的，我们点击它
        elementHandle.click();
        elementHandle.dispose();
        // 创建一个Map对象
        page1.evaluate("() => (window.map = new Map())");
        // 获取Map对象原型的句柄
        JSHandle mapPrototype = page.evaluateHandle("() => Map.prototype");
        // 查询所有Map实例并将它们放入数组中
        JSHandle mapInstances = page.queryObjects(mapPrototype);
        // 计算堆中Map对象的数量
        Object count = page.evaluate("maps => maps.length", Collections.singletonList(mapInstances));
        System.out.println("count: " + count);
        mapPrototype.dispose();
        mapInstances.dispose();
        //切换到该页面
        page.bringToFront();
        page.close();
        System.out.println("isClose: " + page.isClosed());
        browser.close();
    }


    @Test
    public void test4() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        //监听浏览器控制台输出
        page.on(Page.PageEvent.Console, (Consumer<ConsoleMessage>) consoleMessage -> System.out.println("consoleMessage: " + consoleMessage.text()));
        page.setContent("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Button Example</title>\n" +
                "    <style>\n" +
                "        .button-class {\n" +
                "            /* 你可以在这里添加CSS样式 */\n" +
                "            background-color: #4CAF50;\n" +
                "            /* Green */\n" +
                "            border: none;\n" +
                "            color: white;\n" +
                "            padding: 15px 32px;\n" +
                "            text-align: center;\n" +
                "            text-decoration: none;\n" +
                "            display: inline-block;\n" +
                "            font-size: 16px;\n" +
                "            margin: 4px 2px;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <div class=\"button\">\n" +
                "        <!-- 使用相同的类名 -->\n" +
                "        <button  draggable=\"true\" class=\"button-class\">按钮1</button>\n" +
                "        <button class=\"button-class2\">按钮2</button>\n" +
                "        <button class=\"button-class3\">按钮3</button>\n" +
                "    </div>\n" +
                "\n" +
                "</div>\n" +
                "</body>\n" +
                "\n" +
                "</html>");
        ElementHandle button1 = page.$(".button-class");
        ElementHandle button2 = page.$(".button-class2");
        ElementHandle button3 = page.$(".button-class3");
        System.out.println("第一次点击...");
        button1.click();
        button2.click();
        button3.click();
        FrameAddScriptTagOptions options = new FrameAddScriptTagOptions();
        options.setId("myjs");

        options.setContent(" // 通过类名选择所有按钮并添加点击事件监听器\n" +
                "        document.querySelectorAll('.button-class').forEach(function (button) {\n" +
                "            button.addEventListener('touchstart', function () {\n" +
                "                console.log(\"按钮1 touchStart...\")\n" +
                "            });\n" +
                "            button.addEventListener('touchmove',function(){\n" +
                "                console.log('按钮1 touchmove...')\n" +
                "            });\n" +
                "            button.addEventListener('touchend',function(){\n" +
                "                console.log('按钮1 touchend...')\n" +
                "            });\n" +
                "            button.addEventListener('click', function () {\n" +
                "                console.log(\"按钮1被点击了\")\n" +
                "            });\n" +
                "        });\n" +
                "        // 通过类名选择所有按钮并添加点击事件监听器\n" +
                "        document.querySelectorAll('.button-class3').forEach(function (button) {\n" +
                "            button.addEventListener('click', function () {\n" +
                "                console.log(\"按钮3被点击了\")\n" +
                "            });\n" +
                "        });\n" +
                "        // 通过类名选择所有按钮并添加点击事件监听器\n" +
                "        document.querySelectorAll('.button-class2').forEach(function (button) {\n" +
                "            button.addEventListener('click', function () {\n" +
                "                console.log(\"按钮2被点击了\")\n" +
                "            });\n" +
                "        });");
        //注入js

        ElementHandle elementHandle = page.addScriptTag(options);
        //注入后，再点击看看
        System.out.println("第二次点击...");
        button1.click();
        button2.click();
        button3.click();

        //注入jquery.js
        FrameAddScriptTagOptions options2 = new FrameAddScriptTagOptions();
        options2.setId("jquery");
        options2.setUrl("https://cdn.bootcss.com/jquery/3.4.1/jquery.js");

        ElementHandle elementHandle2 = page.addScriptTag(options2);


        FrameAddScriptTagOptions options3 = new FrameAddScriptTagOptions();
        options3.setId("jqueryFun");
        //测试.js调用了jquery function
        options3.setPath("C:\\Users\\fanyong\\Desktop\\测试.js");
        ElementHandle elementHandle3 = page.addScriptTag(options3);
        button1.click();

        elementHandle.dispose();
        elementHandle2.dispose();
        elementHandle3.dispose();
        button1.dispose();
        button2.dispose();
        button3.dispose();
        Thread.sleep(20000);
        browser.close();
    }

    @Test
    public void test5() throws Exception {
//        launchOptions.setDevtools(true);
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        //不授予任何权限，不然emulateIdleState()，会让页面触发 你的设备使用情况请求弹窗，导致页面停止
        browser.defaultBrowserContext().overridePermissions("https://www.baidu.com");
        page.on(Page.PageEvent.Console, (Consumer<ConsoleMessage>) message -> System.out.println("ConsoleMessage: " + message.text()));
        //模拟cpu慢速
        page.emulateCPUThrottling(2);
        //模拟3G网络条件
        page.emulateNetworkConditions(PredefinedNetworkConditions.Slow_4G.getNetworkConditions());
        //设置视力缺陷，影响截图效果，截图就可以看到效果
        page.emulateVisionDeficiency(VisionDeficiency.ACHROMATOPSIA);
        //设置空闲状态
        page.emulateIdleState(new IdleOverridesState.Overrides(true, false));
        //清除空闲状态
        page.emulateIdleState(null);

        page.goTo("https://www.baidu.com/");

        page.evaluate("async () => {\n" +
                "  // 检查是否支持空闲（发呆）检测API\n" +
                "  if (\"IdleDetector\" in window) {\n" +
                "    // 加window是解决vue检测报错的问题\n" +
                "    const { IdleDetector } = window;\n" +
                "\n" +
                "    // state状态值：prompt询问（默认）、denied禁止、granted允许\n" +
                "    // 测试步骤：chrome浏览器设置=>隐私设置和安全性=>网站设置=>近期活动：找到制定ip地址=>您的设备使用情况\n" +
                "    // prompt状态会进行浏览器弹框形式权限申请\n" +
                "    let state;\n" +
                "    try {\n" +
                "      state = await IdleDetector.requestPermission();\n" +
                "    } catch {\n" +
                "      state = await Notification.requestPermission();\n" +
                "    }\n" +
                "    if (state === \"denied\") {\n" +
                "      // 业务中需要采用弹框提示用户手动解除权限禁止状态\n" +
                "      return console.log(\"用户禁止获取设备使用情况权限!\");\n" +
                "    }\n" +
                "    try {\n" +
                "      // 浏览器终止请求控制器对象\n" +
                "      const controller = new AbortController();\n" +
                "      const signal = controller.signal;\n" +
                "\n" +
                "      // 创建空闲检测器\n" +
                "      const idleDetector = new IdleDetector();\n" +
                "\n" +
                "      // 设置一个事件侦听器，该侦听器在空闲状态更改时触发。\n" +
                "      // 测试方法：从 Chromium 94 开始，您可以在 DevTools 中模拟空闲事件，而无需实际处于空闲状态。在 DevTools 中，打开Sensors选项卡并查找Emulate Idle Detector state。通过改变select的option即可在console中看到要得到的现象!\n" +
                "      idleDetector.addEventListener(\"change\", () => {\n" +
                "        // 是否活动状态 active or idle\n" +
                "        const userState = idleDetector.userState; //用户在一段时间内没有和浏览器进行交互\n" +
                "        // 是否锁屏 locked or unlocked\n" +
                "        // 测试方法：一边点击按钮，一边执行锁屏动作，在解锁就能看到locked打印状态\n" +
                "        const screenState = idleDetector.screenState; //系统有一个活动的屏幕锁（如屏幕保护程序），禁止与浏览器交互\n" +
                "        console.log('Idle change: ' + userState + ',' + screenState);\n" +
                "      });\n" +
                "\n" +
                "      // 启动空闲检测器。\n" +
                "      await idleDetector.start({\n" +
                "        threshold: 60000, // 最小值为60,000毫秒（1分钟）\n" +
                "        signal,\n" +
                "      });\n" +
                "      console.log(\"IdleDetector is active.\");\n" +
                "      // 中断检测\n" +
                "      // controller.abort();\n" +
                "    } catch (err) {\n" +
                "      console.error(\"[IdleDetector] Error Name: %s\", err.name);\n" +
                "      console.error(\"[IdleDetector] Error Message: %s\", err.message);\n" +
                "    }\n" +
                "  } else {\n" +
                "    console.error(\n" +
                "      \"当前浏览器不支持 IdleDetector 方法，请使用最新版的 Chrome 重新尝试!\"\n" +
                "    );\n" +
                "  }\n" +
                "}");


        page.screenshot("test5.png");
        System.out.println(page.evaluate("() => matchMedia('screen').matches"));// → true
        System.out.println(page.evaluate("() => matchMedia('print').matches"));// → false
        page.emulateMediaType(MediaType.Print);
        System.out.println(page.evaluate("() => matchMedia('screen').matches"));// → false
        System.out.println(page.evaluate("() => matchMedia('print').matches"));// → true
        page.emulateMediaType(MediaType.None);
        System.out.println(page.evaluate("() => matchMedia('screen').matches"));// → true
        System.out.println(page.evaluate("() => matchMedia('print').matches"));// → false
        List<MediaFeature> mediaFeatures = new ArrayList<>();
        mediaFeatures.add(new MediaFeature("prefers-color-scheme", "dark"));
        System.out.println("--------------emulateMediaFeatures---------------");
        page.emulateMediaFeatures(mediaFeatures);
        System.out.println(page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));// → true
        System.out.println(page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));// → false
        page.emulateMediaFeatures(Collections.singletonList(new MediaFeature("prefers-reduced-motion", "reduce")));// → true
        System.out.println(page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));// → false
        System.out.println(page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));
        page.emulateMediaFeatures(Arrays.asList(new MediaFeature("prefers-color-scheme", "dark"), new MediaFeature("prefers-reduced-motion", "reduce")));
        System.out.println(page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));// → true
        System.out.println(page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));// → false
        System.out.println(page.evaluate(" () => matchMedia('(prefers-reduced-motion: reduce)').matches"));// → true
        System.out.println(page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));// → false
        page.emulateMediaFeatures(Collections.singletonList(new MediaFeature("color-gamut", "p3")));

        System.out.println(page.evaluate("() => matchMedia('(color-gamut: srgb)').matches"));// → true
        System.out.println(page.evaluate("() => matchMedia('(color-gamut: p3)').matches"));// → true
        System.out.println(page.evaluate("() => matchMedia('(color-gamut: rec2020)').matches"));// → false

        String[] availableIDs = TimeZone.getAvailableIDs();
        Object evaluate1 = page.evaluate("Intl.DateTimeFormat().resolvedOptions().timeZone");
        System.out.println("java打印变更前时区：" + evaluate1);
        System.out.println("目标时区：" + availableIDs[2]);
        page.emulateTimezone(availableIDs[2]);
        //js打印一下时区
        page.evaluate("() => console.log(\"时区：\"+Intl.DateTimeFormat().resolvedOptions().timeZone)");
        Object evaluate = page.evaluate("Intl.DateTimeFormat().resolvedOptions().timeZone");
        System.out.println("java打印变更后时区时区：" + evaluate);

        browser.close();
    }

    @Test
    public void test6() throws Exception {
//        launchOptions.setDevtools(true);
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.on(Page.PageEvent.Console, (Consumer<ConsoleMessage>) message -> System.out.println("ConsoleMessage: " + message.text()));
        NewDocumentScriptEvaluation newDocumentScriptEvaluation = page.evaluateOnNewDocument("Object.defineProperty(navigator, 'languages', {\n" +
                "  get: function () {\n" +
                "    return ['en-US', 'en', 'bn'];\n" +
                "  },\n" +
                "});\n" +
                "console.log(\"new dom...\")");
        page.goTo("https://www.baidu.com");
        System.out.println("isJavaScriptEnabled: " + page.isJavaScriptEnabled());
        page.setJavaScriptEnabled(false);
        System.out.println("isJavaScriptEnabled: " + page.isJavaScriptEnabled());
        page.removeScriptToEvaluateOnNewDocument(newDocumentScriptEvaluation.getIdentifier());
        //js不能执行后，有的方法会超时
        System.out.println("title: " + page.title());
        ElementHandle elementHandle = page.$("#su");
        elementHandle.dispose();
        List<ElementHandle> $$ = page.$$("#hotsearch-content-wrapper > li");
        System.out.println("$$: " + $$.size());
        for (ElementHandle handle : $$) {
            Object title = handle.$eval(".title-content-title", "(element) => element.innerText");
            System.out.println("title: " + title);
        }
        browser.close();
    }

    @Test
    public void test7() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        //授予百度页面的定位权限
        browser.defaultBrowserContext().overridePermissions("https://www.baidu.com", WebPermission.GEOLOCATION);
        //打开一个页面
        Page page = browser.newPage();

        //凭证
        page.authenticate(new Credentials("guest", "guest"));
        //因为这个网站需要验证
        page.goTo("https://jigsaw.w3.org/HTTP/Basic/");
        //移除凭证,不再验证
        page.authenticate(null);
        page.setGeolocation(new GeolocationOptions(59.95, 30.31667));
        page.setExtraHTTPHeaders(Collections.singletonMap("foo", "bar"));
        page.goTo("https://www.baidu.com");
        //鼠标悬停，第一个新闻标题
        System.out.println("开始悬停...");
        page.hover("#hotsearch-content-wrapper > li:nth-child(1) > a > span.title-content-title");
        Thread.sleep(5000);
        page.type("#kw", "puppeteer");
        Thread.sleep(10000);
        browser.close();
    }

    /**
     * 测试waitForNavigation
     */
    @Test
    public void test8() throws Exception {
        launchOptions.setDevtools(true);
        Browser browser = Puppeteer.launch(launchOptions);
        //授予百度页面的定位权限
        browser.defaultBrowserContext().overridePermissions("https://www.baidu.com", WebPermission.GEOLOCATION);
        //打开一个页面
        Page page = browser.newPage();
        page.setExtraHTTPHeaders(Collections.singletonMap("foo1", "bar"));


        //发送导航命令，不等待，这是方式1
        page.goTo("https://www.baidu.com", false);
        //在这里等待
        Response response = page.waitForNavigation();
        System.out.println("response: " + response.url());


        //或者用新线程等待，这是方式2
//        new Thread(() -> {
//            Response response = page.waitForNavigation();
//            System.out.println("response: " + response.url());
//        }).start();
//        page.goTo("https://www.baidu.com");


        Thread.sleep(50000);
        browser.close();
    }

    /**
     * 测试waitForRequest
     */
    @Test
    public void test9() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();

        page.goTo("https://www.baidu.com", false);
        Request request = page.waitForRequest("https://www.baidu.com/img/flexible/logo/pc/result.png");
        System.out.println("url: " + request.url() + ", type: " + request.resourceType());
        browser.close();
    }

    /**
     * 测试waitForResponse
     */
    @Test
    public void test10() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com", false);
        Response res = page.waitForResponse(response -> response.url().contains("baidu") && response.status() == 200);
        System.out.println("url: " + res.url() + ", type: " + res.ok());
        browser.close();
    }

    /**
     * waitForSelector
     */
    @Test
    public void test11() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://pptr.nodejs.cn/api/puppeteer.waitforselectoroptions/", false);
        WaitForSelectorOptions options = new WaitForSelectorOptions(true, false);
        ElementHandle elementHandle = page.waitForSelector("#__docusaurus_skipToContent_fallback > div > div > main > div > div > div.col.docItemCol_VOVn > div > article > div.theme-doc-markdown.markdown > header > h1", options);
        elementHandle.dispose();
        System.out.println(elementHandle.id());
        browser.close();
    }

    /**
     * waitForSelector
     */
    @Test
    public void test12() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://pptr.nodejs.cn/api/puppeteer.waitforselectoroptions/", false);
        ElementHandle elementHandle = page.waitForSelector("#__docusaurus_skipToContent_fallback > div > div > main > div > div > div.col.docItemCol_VOVn > div > article > div.theme-doc-markdown.markdown > header > h1");
        elementHandle.dispose();
        System.out.println(elementHandle.id());
        browser.close();
    }

    @Test
    public void test13() throws InterruptedException, IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.setBypassCSP(true);
        page.setBypassServiceWorker(true);
        page.setCacheEnabled(true);
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        List<WebWorker> workers = page.workers();
        for (WebWorker worker : workers) {
            System.out.println("worker: " + worker.url());
        }
        page.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36");
        Thread.sleep(2000);
        page.focus("#hotsearch-content-wrapper > li:nth-child(1) > a");
        Thread.sleep(2000);
        page.hover("#hotsearch-content-wrapper > li:nth-child(2) > a");
        Thread.sleep(2000);
        page.tap("#hotsearch-content-wrapper > li:nth-child(1) > a");
        Thread.sleep(2000);
        page.bringToFront();
        page.goTo("https://pptr.nodejs.cn/api/puppeteer.page.setuseragent");
        page.goTo("https://www.geetest.com/demo/slide-en.html");
        Thread.sleep(2000);
        page.goBack();
        Thread.sleep(2000);
        page.goForward();
        Thread.sleep(2000);
        System.out.println("done...");
        browser.close();
    }

    @Test
    public void test14() throws InterruptedException, IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://smallpdf.com/cn/pdf-to-word");
        AwaitableResult<FileChooser> result = page.fileChooserWaitFor();
        page.click("#app > div > div:nth-child(1) > header > div.rh8nkv-3.gcKsMX > div > div > label > form > label > div > div.sc-8s01yt-1.XegmY > div.l3tlg0-5.ekSdZR > button.l3tlg0-0.ggoliT");
        FileChooser fileChooser = result.waitingGetResult();
        //上传文件
        fileChooser.accept(Arrays.asList("C:\\Users\\fanyong\\Pictures\\Saved Pictures\\1.jpg", "C:\\Users\\fanyong\\Pictures\\Saved Pictures\\2.jpg"));
        Thread.sleep(2000);
        //再次上传
        AwaitableResult<FileChooser> result2 = page.fileChooserWaitFor();
        page.click("#app > div > div > div.sc-1lrg9x7-1.llrKYu > div.sc-1lrg9x7-2.WzjRx > div.sc-1lrg9x7-3.yLQpw > div > div.sc-1dnebxo-1.jYoHNJ > div > div.g1gmt0-1.eXUQYI > button");
        result2.waitingGetResult().accept(Collections.singletonList("C:\\Users\\fanyong\\Pictures\\Saved Pictures\\IMG_20180820_174844.jpg"));
        Thread.sleep(10000);
        browser.close();
    }

    /**
     * 列表多选
     */
    @Test
    public void test15() throws InterruptedException, IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.setViewport(new Viewport(1200, 1200));
        page.setContent("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Button Example</title>\n" +
                "    <style>\n" +
                "        .button-class {\n" +
                "            /* 你可以在这里添加CSS样式 */\n" +
                "            background-color: #4CAF50;\n" +
                "            /* Green */\n" +
                "            border: none;\n" +
                "            color: white;\n" +
                "            padding: 15px 32px;\n" +
                "            text-align: center;\n" +
                "            text-decoration: none;\n" +
                "            display: inline-block;\n" +
                "            font-size: 16px;\n" +
                "            margin: 4px 2px;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <div class=\"button\">\n" +
                "        <!-- 使用相同的类名 -->\n" +
                "        <button class=\"button-class\">按钮1</button>\n" +
                "        <button class=\"button-class\">按钮2</button>\n" +
                "        <button class=\"button-class3\">按钮3</button>\n" +
                "        <label for=\"pet-select\">Choose a pet:</label>\n" +
                "        <select name=\"pets\" multiple id=\"pet-select\">\n" +
                "            <option value=\"\">--Please choose an option--</option>\n" +
                "            <option value=\"dog\">dog</option>\n" +
                "            <option value=\"cat\">cat</option>\n" +
                "            <option value=\"hamster\">Hamster</option>\n" +
                "            <option value=\"parrot\">Parrot</option>\n" +
                "            <option value=\"spider\">Spider</option>\n" +
                "            <option value=\"goldfish\">Goldfish</option>\n" +
                "        </select>\n" +
                "    </div>\n" +
                "\n" +
                "</div>\n" +
                "    <script>\n" +
                "        // 通过类名选择所有按钮并添加点击事件监听器\n" +
                "        document.querySelectorAll('.button-class').forEach(function (button) {\n" +
                "            button.addEventListener('click', function () {\n" +
                "                console.log(\"按钮1 或 按钮2被点击了\")\n" +
                "            });\n" +
                "        });\n" +
                "        // 通过类名选择所有按钮并添加点击事件监听器\n" +
                "        document.querySelectorAll('.button-class3').forEach(function (button) {\n" +
                "            button.addEventListener('click', function () {\n" +
                "                console.log(\"按钮3被点击了\")\n" +
                "            });\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "\n" +
                "</html>");
        //可以多选的列表
        List<String> select = page.select("#pet-select", Arrays.asList("dog", "cat"));
        System.out.println(String.join(",", select));
        Thread.sleep(5000);
        browser.close();
    }

    /**
     * 测试$eval和$$eval
     */
    @Test
    public void test16() throws InterruptedException, IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.on(Page.PageEvent.Console, (Consumer<ConsoleMessage>) message -> System.out.println(message.text()));
        //设置html页面，有三个按钮，点击后打印出按钮的文本
        page.setContent("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Button Example</title>\n" +
                "    <style>\n" +
                "        .button-class {\n" +
                "            /* 你可以在这里添加CSS样式 */\n" +
                "            background-color: #4CAF50;\n" +
                "            /* Green */\n" +
                "            border: none;\n" +
                "            color: white;\n" +
                "            padding: 15px 32px;\n" +
                "            text-align: center;\n" +
                "            text-decoration: none;\n" +
                "            display: inline-block;\n" +
                "            font-size: 16px;\n" +
                "            margin: 4px 2px;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <div class=\"button\">\n" +
                "        <!-- 使用相同的类名 -->\n" +
                "        <button class=\"button-class\">按钮1</button>\n" +
                "        <button class=\"button-class\">按钮2</button>\n" +
                "        <button class=\"button-class3\">按钮3</button>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        // 通过类名选择所有按钮并添加点击事件监听器\n" +
                "        document.querySelectorAll('.button-class').forEach(function (button) {\n" +
                "            button.addEventListener('click', function () {\n" +
                "                console.log(\"按钮1 或 按钮2被点击了\")\n" +
                "            });\n" +
                "        });\n" +
                "        // 通过类名选择所有按钮并添加点击事件监听器\n" +
                "        document.querySelectorAll('.button-class3').forEach(function (button) {\n" +
                "            button.addEventListener('click', function () {\n" +
                "                console.log(\"按钮3被点击了\")\n" +
                "            });\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "\n" +
                "</html>");

        Object o1 = page.$eval(".button-class3", "element  => element.innerText = element.innerText + \"魔法按钮\"");
        Object o2 = page.$$eval(".button-class", "async () => {\n" +
                "    array1 => array1.forEach(element => element.innerText = element.innerText);\n" +
                "    return \"按钮1按钮2添加魔法按钮文字\"\n" +
                "}\n");
        System.out.println("1: " + o1);
        System.out.println("2: " + o2);
        Thread.sleep(5000);
        browser.close();
    }

    /**
     * 测试coverage
     */
    @Test
    public void test17() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.coverage().startJSCoverage(new JSCoverageOptions(true, true, true, true));//会导致不能页面不能加载完成
        page.coverage().startCSSCoverage();
        page.goTo("https://www.baidu.com/");

        List<JSCoverageEntry> coverageEntries1 = page.coverage().stopJSCoverage();
        System.out.println("1 = " + coverageEntries1);
        List<CoverageEntry> coverageEntries = page.coverage().stopCSSCoverage();
        System.out.println("2 = " + coverageEntries);
        browser.close();
    }

    /**
     * 测试coverage
     */
    @Test
    public void test18() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        //开启追踪
        page.tracing().start("C:\\Users\\fanyong\\Desktop\\trace.json");
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        page.tracing().stop();
        browser.close();
    }

    /**
     * Page.waitForFunction() ：可用于观察视口大小的变化
     */
    @Test
    public void test19() throws InterruptedException, IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        new Thread(
                () -> {
                    JSHandle jsHandle = page.waitForFunction("window.innerWidth < 100");
                    System.out.println(jsHandle);
                    jsHandle.dispose();
                }
        ).start();
        page.setViewport(new Viewport(50, 50));
//        browser.close();
        Thread.sleep(10000);
    }

    /**
     * Page.waitForFunction():传递一个参数给pageFunction
     */
    @Test
    public void test20() throws InterruptedException, IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/", false);
        //selector => !!document.querySelector(selector) 返回的就是true或者false
        JSHandle jsHandle = page.waitForFunction("selector => !!document.querySelector(selector)", new WaitForSelectorOptions(), "#su");
        System.out.println(jsHandle);
//        browser.close();
        Thread.sleep(10000);
    }

    /**
     * Page.waitForFunction()
     */
    @Test
    public void test21() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/");
        page.on(Page.PageEvent.Console, (Consumer<ConsoleMessage>) message -> System.out.println(message.text()));
        String username = "github-username";
        JSHandle jsHandle = page.waitForFunction("async username => {\n" +
                "    // show the avatar\n" +
                "    const img = document.createElement('img');\n" +
                "    // 创建一个新的 div 元素\n" +
                "    let newDiv = document.createElement(\"div\");\n" +
                "    // 给它一些内容\n" +
                "    let newContent = document.createTextNode(username);\n" +
                "    // 添加文本节点 到这个新的 div 元素\n" +
                "    newDiv.appendChild(newContent);\n" +
                "    // wait 3 seconds\n" +
                "    await new Promise((resolve, reject) => setTimeout(resolve, 3000));\n" +
                "    console.log('username:', username);\n" +
                "}", new WaitForSelectorOptions(), username);
        System.out.println(jsHandle);
        browser.close();
    }

    /**
     * Page.waitForFunction()
     */
    @Test
    public void test22() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://example.com/", false);
        Frame frame = page.waitForFrame("https://example.com/");
        System.out.println("frame: " + frame.url());
        browser.close();
    }

    /**
     * Page.waitForFunction()
     */
    @Test
    public void test23() throws InterruptedException, IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://example.com/", false);
        Frame frame = page.waitForFrame(new Predicate<Frame>() {
            @Override
            public boolean test(Frame frame) {
                return frame.url().contains("example.com");
            }
        });
        System.out.println("frame: " + frame.url());
        browser.close();
    }

    /**
     * 录制屏幕 录制格式webm
     */
    @Test
    public void test24() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://www.geetest.com/demo/slide-en.html");
        ScreencastOptions screencastOptions = new ScreencastOptions();
        screencastOptions.setPath("D:\\test\\test1.webm");
        screencastOptions.setFormat(ScreenCastFormat.WEBM);
        //指定ffmpeg路径，如果配置了系统的环境变量，那么可以不指定
        screencastOptions.setFfmpegPath("D:\\windowsUtil\\ffmpeg-2024-10-10-git-0f5592cfc7-full_build\\ffmpeg-2024-10-10-git-0f5592cfc7-full_build\\bin\\ffmpeg.exe");
        ScreenRecorder screencast = page.screencast(screencastOptions);
        page.type("#username", "123456789", 200);
        page.type("#password", "123456789", 200);
        screencast.stop();
        browser.close();
    }

    /**
     * 录制屏幕某个区域 录制格式webm
     */
    @Test
    public void test25() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://www.geetest.com/demo/slide-en.html");
        ScreencastOptions screencastOptions = new ScreencastOptions();
        screencastOptions.setPath("D:\\test\\test2.webm");
        screencastOptions.setFormat(ScreenCastFormat.WEBM);
        //指定ffmpeg路径，如果配置了系统的环境变量，那么可以不指定
        BoundingBox boundingBox = page.$("#username").boundingBox();
        screencastOptions.setCrop(boundingBox);
        screencastOptions.setFfmpegPath("D:\\windowsUtil\\ffmpeg-2024-10-10-git-0f5592cfc7-full_build\\ffmpeg-2024-10-10-git-0f5592cfc7-full_build\\bin\\ffmpeg.exe");
        ScreenRecorder screencast = page.screencast(screencastOptions);
        page.type("#username", "123456789", 200);
        page.type("#password", "123456789", 200);
        screencast.stop();
        browser.close();
    }

    /**
     * 录制屏幕 录制格式gif
     */
    @Test
    public void test26() throws IOException {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.goTo("https://www.geetest.com/demo/slide-en.html");
        ScreencastOptions screencastOptions = new ScreencastOptions();
        screencastOptions.setPath("D:\\test\\test.gif");
        screencastOptions.setFormat(ScreenCastFormat.GIF);
        screencastOptions.setFfmpegPath("D:\\windowsUtil\\ffmpeg-2024-10-10-git-0f5592cfc7-full_build\\ffmpeg-2024-10-10-git-0f5592cfc7-full_build\\bin\\ffmpeg.exe");
        ScreenRecorder screencast = page.screencast(screencastOptions);
        page.type("#username", "123456789", 200);
        page.type("#password", "123456789", 200);
        screencast.stop();
        browser.close();
    }

}
