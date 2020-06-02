package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.protocol.webAuthn.Credentials;

import java.util.ArrayList;

public class PageAuthenticateExample {
    public static void main(String[] args) throws Exception {
         String  path ="D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();

        //如果没有加验证，那么goTo方法将会报错
        page.authenticate(new Credentials("guest","guest"));
        //因为这个网站需要验证
        page.goTo("https://jigsaw.w3.org/HTTP/Basic/");

    }
}
