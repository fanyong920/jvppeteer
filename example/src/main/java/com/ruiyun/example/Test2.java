package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import java.util.ArrayList;

public class Test2 {
    public static void main(String[] args) throws Exception {
        Puppeteer.downloadBrowser();
        ArrayList<String> args1 = new ArrayList<>();//添加一些额外的启动参数
        args1.add("--no-sandbox");
        args1.add("--headless");
        args1.add("--disable-extensions");
        args1.add("--disable-popup-blocking");
        args1.add("--disable-web-security");
        args1.add("--disable-features=IsolateOrigins,site-per-process");
        args1.add("--ignore-certificate-errors");
        args1.add("--allow-insecure-localhost");
        args1.add("--remote-debugging-port=9555");
        LaunchOptions launchOptions = LaunchOptions.builder().args(args1).build();
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("http://mesdevgw.hnxg.com/qe/v1/get/tc-cert?certiNo=BV509600015&tenantId=1");
        page.screenshot("test.png");
        browser.close();
    }
}
