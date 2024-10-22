package com.ruiyun.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Target;
import com.ruiyun.jvppeteer.transport.CDPSession;
import org.junit.Test;

public class L_CDPSessionTest extends A_LaunchTest {

    @Test
    public void test2() throws Exception {

        Browser browser = Puppeteer.launch(launchOptions);
        Target target = browser.target();//browser对应的target
        CDPSession session = target.session();//已经附属到target的session
        if (session == null) {
            session = target.createCDPSession();
        }
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
        Thread.sleep(50000);
        //关闭浏览器
        browser.close();
    }





}
