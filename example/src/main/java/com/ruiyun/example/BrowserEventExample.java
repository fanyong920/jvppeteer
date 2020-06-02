package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;

import java.util.ArrayList;

public class BrowserEventExample {
    public static void main(String[] args) throws Exception {
        String  path ="D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        browser.onDisconnected((s) ->{
            System.out.println("浏览器断开连接");
        });
        browser.onTrgetcreated((target) -> {
            System.out.println("target type: "+target.type());
        });
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        browser.close();
    }
}
