package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageEntry;

import java.util.Collections;
import java.util.List;

public class CoverageExample {
    public static void main(String[] args) throws Exception {
        //ExecutablePath是指定chrome启动路径
        LaunchOptions launchOptions = new LaunchOptionsBuilder().withIgnoreDefaultArgs(Collections.singletonList("--enable-automation")).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.coverage().startJSCoverage();//会导致不能页面不能加载完成
        page.coverage().startCSSCoverage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");

        List<CoverageEntry> coverageEntries1 = page.coverage().stopJSCoverage();
        System.out.println("1 = "+coverageEntries1);
        List<CoverageEntry> coverageEntries = page.coverage().stopCSSCoverage();
        System.out.println("2 = "+coverageEntries);
        // 做一些其他操作
        browser.close();
    }
}
