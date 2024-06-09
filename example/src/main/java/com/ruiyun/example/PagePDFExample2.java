package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

import java.util.ArrayList;

public class PagePDFExample2 {

    public static void main(String[] args) throws Exception {
        //自动下载默认版本的浏览器，第一次下载后不会再下载,出错就多试几次 Constant.VERSION可以查看默认版本
        BrowserFetcher.downloadIfNotExist();

        ArrayList<String> argList = new ArrayList<>();
        //生成pdf必须在无厘头模式下才能生效
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(argList).withHeadless(true)/*.withExecutablePath(path)*/.build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        page.pdf("test.pdf");
    }
}
