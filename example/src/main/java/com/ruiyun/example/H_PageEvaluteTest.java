package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.entities.ConsoleMessage;
import org.junit.Test;

import java.util.function.Consumer;

public class H_PageEvaluteTest extends A_LaunchTest {
    @Test
    public void test2() throws Exception {
        //启动浏览器
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        //监听devtool控制台输出
        page.on(Page.PageEvent.Console, (Consumer<ConsoleMessage>) consoleMessage -> System.out.println("console=" + consoleMessage.text()));
        //在devtool 控制台打印输出
        page.evaluate("() => console.log('hello', 5, {foo: 'bar'})");
        //等待5s看看效果
        Thread.sleep(5000);
        //关闭浏览器
        browser.close();
    }
}
