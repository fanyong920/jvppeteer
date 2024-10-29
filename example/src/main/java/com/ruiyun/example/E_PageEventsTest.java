package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Frame;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Request;
import com.ruiyun.jvppeteer.core.Response;
import com.ruiyun.jvppeteer.core.WebWorker;
import com.ruiyun.jvppeteer.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.entities.PageMetrics;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import org.junit.Test;

import java.util.function.Consumer;

public class E_PageEventsTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            String version = browser.version();
            System.out.println("version=" + version);
            //打开一个页面
            Page page = browser.newPage();
            //Page.PageEvent.LOAD 事件 没有对应的Object，默认ignored = true
            page.on(Page.PageEvent.Load, ignored -> System.out.println("load"));
            page.on(Page.PageEvent.Domcontentloaded, ignored -> System.out.println("domcontentloaded"));
            //大多数情况下，event对应EvaluateException,有时候也是Object，具体看Helper.createClientError(event.getExceptionDetails())
            page.on(Page.PageEvent.PageError, event -> System.out.println("pageerror:" + event));
            page.on(Page.PageEvent.FrameNavigated, (Consumer<Frame>) frame -> System.out.println("frame:" + frame.id()));
            //只监听一次，一直监听的话，会打印很多request,response
            page.once(Page.PageEvent.Request, (Consumer<Request>) event -> System.out.println("request:" + event.url()));
            page.once(Page.PageEvent.Response, (Consumer<Response>) event -> System.out.println("response:" + event.url()));

            page.on(Page.PageEvent.RequestFailed, (Consumer<Request>) event -> System.out.println("requestfailed:" + event.url()));
            page.on(Page.PageEvent.FrameDetached, (Consumer<Frame>) frame -> System.out.println("framedetached:" + frame.id()));
            page.on(Page.PageEvent.FrameAttached, (Consumer<Frame>) frame -> System.out.println("frameattached:" + frame.id()));
            page.on(Page.PageEvent.Console, (Consumer<ConsoleMessage>) event -> System.out.println("console:" + event.text()));
            page.on(Page.PageEvent.Metrics, (Consumer<PageMetrics>) event -> System.out.println("metrics:" + event.getTitle()));
            page.on(Page.PageEvent.Close, ignored -> System.out.println("setClose"));
            page.on(Page.PageEvent.Error, (Consumer<JvppeteerException>) event -> System.out.println("error:" + event.getMessage()));
            page.on(Page.PageEvent.Popup, (Consumer<Page>) page1 -> System.out.println("popup:" + page1.url()));
            page.on(Page.PageEvent.WorkerCreated, (Consumer<WebWorker>) worker -> System.out.println("workercreate:" + worker.url()));
            page.on(Page.PageEvent.WorkerDestroyed, (Consumer<WebWorker>) worker -> System.out.println("workerdestroy:" + worker.url()));
            //用csdn来测试弹窗
            page.goTo("https://cn.bing.com/search?q=translate&qs=n&form=QBRE&sp=-1&lq=0&pq=translate&sc=13-9&sk=&cvid=DD32A66465A246B8970383CB882529C3&ghsh=0&ghacc=0&ghpl=");
//        Response reload = page.reload();
//        System.out.println(Arrays.toString(reload.content()));

            //触发 page close事件
            page.close();
            //等待5s看看效果
            Thread.sleep(5000);
        }
    }

    @Test
    public void test4() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            page.on(Page.PageEvent.Popup, (Consumer<Page>) page1 -> System.out.println("popup:" + page1.url()));
            page.on(Page.PageEvent.WorkerCreated, (Consumer<WebWorker>) worker -> System.out.println("workercreate:" + worker.url()));
            page.on(Page.PageEvent.WorkerDestroyed, (Consumer<WebWorker>) worker -> System.out.println("workerdestroy:" + worker.url()));
            page.goTo("https://www.baidu.com/?tn=68018901_16_pg", false);
            //等待hao123按钮出现
            page.waitForSelector("#s-top-left > a:nth-child(2)");
            //点击hao123按钮，打开一个新页面
            page.click("#s-top-left > a:nth-child(2)");

            //等待5s看看效果
            Thread.sleep(5000);
            //关闭浏览器
        }
    }
}
