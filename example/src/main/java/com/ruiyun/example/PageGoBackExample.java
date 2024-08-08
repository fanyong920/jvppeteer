package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.GoToOptions;
import com.ruiyun.jvppeteer.options.PuppeteerLifeCycle;

import java.util.ArrayList;
import java.util.Collections;

public class PageGoBackExample {

    public static void main(String[] args) throws Exception {

        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");

        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();

        GoToOptions navigateOptions = new GoToOptions();
        //dom加载完毕就算导航完成
        navigateOptions.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.valueOf("domcontentloaded")));
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",navigateOptions);
        page.goTo("https://detail.tmall.com/item.htm?id=616839388072",navigateOptions);

        page.goBack();
//        browser.close();
    }
}
