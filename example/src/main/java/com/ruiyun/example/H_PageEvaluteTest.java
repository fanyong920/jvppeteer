package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.PrimitiveValue;
import java.util.function.Consumer;
import org.junit.Test;

public class H_PageEvaluteTest extends A_LaunchTest {
    @Test
    public void test2() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            //监听devtool控制台输出
            page.on(PageEvents.Console, (Consumer<ConsoleMessage>) consoleMessage -> System.out.println("console=" + consoleMessage.text()));
            //在devtool 控制台打印输出
            page.evaluate("console.log('hello', 5, {foo: 'bar'})");
            //等待5s看看效果
            Thread.sleep(5000);
        }
    }

    /**
     * 传递特属值
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            //监听devtool控制台输出
            page.on(PageEvents.Console, (Consumer<ConsoleMessage>) consoleMessage -> System.out.println("console=" + consoleMessage.text()));
            //在devtool 控制台打印输出
            page.evaluate("async (arg1,arg2,arg3,arg4,arg5) =>{\n" +
                    "  console.log(null == undefined)\n" +
                    "  console.log(-0 == -0.0)\n" +
                    "  console.log(arg1 == null)\n" +
                    "  console.log(arg2 == undefined)\n" +
                    "  console.log(arg3 == -0.0)\n" +
                    "  console.log(arg4 == Infinity)\n" +
                    "  console.log(arg5 == -Infinity)\n" +
                    "}", PrimitiveValue.Null, PrimitiveValue.Undefined,Constant.Navigate_Zero,Constant.Infinity,Constant.Navigate_Infinity);
            //等待5s看看效果
            Thread.sleep(5000);
        }
    }
}
