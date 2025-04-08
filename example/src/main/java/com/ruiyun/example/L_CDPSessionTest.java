package com.ruiyun.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class L_CDPSessionTest{
    /**
     * 要打印成什么样子的pdf，自己手动在浏览器按Ctrl+p，弹出的窗口就是pdf样式，，再把各个选项点一下，看一下预览效果
     * 然后再回来写代码
     */
    @Test
    public void test2() throws Exception {

        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            Target target = browser.target();//browser对应的target
            CDPSession session = target.createCDPSession();
            System.out.println("session id:" + session.id());
            //这个命令是浏览器级别的session才能请求，所以要从browser的target创建session，如果是page级别的请求，就从page的target创建session
            //具体的protocol命令，请看https://chromedevtools.github.io/devtools-protocol/tot/SystemInfo/
            JsonNode send = session.send("SystemInfo.getProcessInfo");
            System.out.println("processInfo: " + send);
            String version = browser.version();
            System.out.println("version1: " + version);
            //断开连接
            session.detach();
            String version2 = browser.version();
            System.out.println("version2: " + version2);
            //打开任务管理器对比一下进程id
            Thread.sleep(5000);
        }
    }


    @Test
    public void test3() throws Exception {

        try (Browser browser = Puppeteer.launch(LAUNCHOPTIONS)) {
            Target target = browser.target();//browser对应的target
            CDPSession session = target.createCDPSession();
            JsonNode res = session.send("Browser.getVersion");
            System.out.println("version: " + res);
        }
    }

    @Test
    public void test4() throws Exception {

        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        CDPSession cdpSession = page.createCDPSession();
        Connection connection = cdpSession.connection();
        cdpSession.detached();
        System.out.println(connection == cdpSession.connection());//true


        CDPSession cdpSession1 = page.createCDPSession();

        System.out.println(cdpSession1.detached());
        cdpSession1.detach();
        System.out.println(cdpSession1.detached());
    }


}
