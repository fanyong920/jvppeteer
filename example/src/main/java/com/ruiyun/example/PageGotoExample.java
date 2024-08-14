package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.WaitForOptions;

import java.util.ArrayList;

public class PageGotoExample {

    public static void main(String[] args) throws Exception {


        ArrayList<String> argList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(argList).withHeadless(false).withExecutablePath("C:\\Users\\fanyong\\Desktop\\chrome-win64\\chrome-win64\\chrome.exe").build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Browser browser2 = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/");
//        page.reload(new WaitForOptions());
        browser.close();

//        Page page1 = browser2.newPage();
//        page1.goTo("https://www.taobao.com/about/");
//        System.out.println(page1.$eval("#content > div.right-info > div > div.inner.taobao-intro > p","p => p.value",new ArrayList<>()));
    }
}
