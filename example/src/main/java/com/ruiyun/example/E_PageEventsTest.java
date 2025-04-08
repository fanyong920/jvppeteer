package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Request;
import com.ruiyun.jvppeteer.api.core.Response;
import com.ruiyun.jvppeteer.api.core.WebWorker;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.cdp.entities.PageMetrics;
import com.ruiyun.jvppeteer.cdp.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import java.util.Objects;
import java.util.function.Consumer;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class E_PageEventsTest {

    @Test
    public void test3() throws Exception {
        //启动浏览器
        LAUNCHOPTIONS.setDevtools(true);
        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            String version = browser.version();
            System.out.println("version=" + version);
            //打开一个页面
            Page page = browser.newPage();
            //PageEvents.LOAD 事件 没有对应的Object，默认ignored = true
            page.once(PageEvents.Load, ignored -> System.out.println("load"));
            page.once(PageEvents.Domcontentloaded, ignored -> System.out.println("domcontentLoaded"));
            //大多数情况下，event对应EvaluateException,有时候也是Object，具体看Helper.createClientError(event.getExceptionDetails())
            page.once(PageEvents.PageError, event -> System.out.println("pageError:" + event));
            page.once(PageEvents.FrameNavigated, (Consumer<Frame>) frame -> System.out.println("FrameNavigated:" + frame.id()));
            //只监听一次，一直监听的话，会打印很多request,response
            page.once(PageEvents.Request, (Consumer<Request>) event -> System.out.println("request:" + event.url()));
            page.once(PageEvents.Response, (Consumer<Response>) event -> System.out.println("response:" + event.url()));

            page.once(PageEvents.RequestFailed, (Consumer<Request>) event -> System.out.println("requestFailed:" + event.url()));
            page.once(PageEvents.FrameDetached, (Consumer<Frame>) frame -> System.out.println("frameDetached:" + frame.id()));
            page.once(PageEvents.FrameAttached, (Consumer<Frame>) frame -> System.out.println("frameAttached:" + frame.id()));
            page.once(PageEvents.Console, (Consumer<ConsoleMessage>) event -> System.out.println("console:" + event.text()));
            page.once(PageEvents.Metrics, (Consumer<PageMetrics>) event -> System.out.println("metrics:" + event.getTitle()));
            page.once(PageEvents.Close, ignored -> System.out.println("page close"));
            page.once(PageEvents.Error, (Consumer<JvppeteerException>) event -> System.out.println("error:" + event.getMessage()));
            page.once(PageEvents.WorkerCreated, (Consumer<WebWorker>) worker -> System.out.println("workerCreate:" + worker.url()));
            page.once(PageEvents.WorkerDestroyed, (Consumer<WebWorker>) worker -> System.out.println("workerDestroy:" + worker.url()));
            page.goTo("http://www.baidu.com");
            //创建一个id为frame1的frame
            page.evaluate("async (frameId, url) => {\n" +
                    "      const frame = document.createElement('iframe');\n" +
                    "      frame.src = url;\n" +
                    "      frame.id = frameId;\n" +
                    "      document.body.appendChild(frame);\n" +
                    "      await new Promise(x => {\n" +
                    "        return (frame.onload = x);\n" +
                    "      });\n" +
                    "      return frame;\n" +
                    "    }", "frame1", page.url());
            //触发 PageEvents.Metrics,webdriver bidi不支持该事件
            String content = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Console TimeStamp Example</title>\n" +
                    "            <script>\n" +
                    "            document.addEventListener('DOMContentLoaded', function() {\n" +
                    "        setTimeout(function() {\n" +
                    "            console.timeStamp('Page Loaded');\n" +
                    "        }, 1000);\n" +
                    "    });\n" +
                    "    </script>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h1>欢迎来到示例页面</h1>\n" +
                    "</body>\n" +
                    "</html>\n";
            page.setContent(content);

            //触发 PageEvents.Error 和  PageError
//            try {
//                if(version.contains("firefox")){
//                    page.goTo("about:crashcontent");
//                }else {
//                    page.goTo("chrome://crash");
//                }
//            } catch (ExecutionException | InterruptedException e) {

//            }
            //触发 PageEvents.Popup
            page.evaluate("window.open('about:blank')");
            //触发 PageEvents.Console
            page.evaluate("console.log('hello')");
            //触发 page close事件
            page.close();
            //等待5s看看效果
            Thread.sleep(5000);

        }
    }

    @Test
    public void test4() throws Exception {
        //启动浏览器
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        //打开一个页面
        Page page = browser.newPage();
        System.out.println(browser.version());
        page.on(PageEvents.Popup, (Consumer<Page>) page1 -> System.out.println("popup:" + page1.url()));
        page.once(PageEvents.WorkerCreated, (Consumer<WebWorker>) worker -> System.out.println("workerCreate:" + worker.url()));
        page.once(PageEvents.WorkerDestroyed, (Consumer<WebWorker>) worker -> System.out.println("workerDestroy:" + worker.url()));

        page.goTo("https://www.baidu.com/");
        WaitForSelectorOptions waitForSelectorOptions = new WaitForSelectorOptions();
        waitForSelectorOptions.setTimeout(0);
        ElementHandle elementHandle = page.waitForSelector("#su", waitForSelectorOptions);
        if (Objects.nonNull(elementHandle)) {
            System.out.println("wait for selector: " + elementHandle);
        }
        //等待hao123按钮出现
        //点击hao123按钮，打开一个新页面
        page.click("#s-top-left > a:nth-child(3)");
        page.setContent("<!-- 简化展示 -->\n" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <!-- 引入其他必要资源如脚本等 -->\n" +
                "</head>\n" +
                "<body>\n" +
                "    <!-- 示例代码，初始化Worker -->\n" +
                "    <script src=\"worker.js\"></script>\n" +
                "    <script>\n" +
                "        // 创建Worker实例\n" +
                "        var myWorker = new Worker('worker.js');\n" +
                "        \n" +
                "        // 接收Worker的消息事件\n" +
                "        myWorker.onmessage = function(e) {\n" +
                "            console.log('接收到消息: ', e.data);\n" +
                "        };\n" +
                "        \n" +
                "        // 发送消息给Worker\n" +
                "        myWorker.postMessage('你好，Web Worker！');\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>");
        //等待5s看看效果
        Thread.sleep(5000);
        //关闭浏览器
        browser.close();
    }
}
