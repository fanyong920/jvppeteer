package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;

import java.util.ArrayList;
import java.util.Collections;

public class ResponseInterceptionExample {
    public static void main(String[] args) throws Exception {
        String path = new String("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe".getBytes(), "UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        //String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
       page.onResponse((response) -> {
           try {
               System.out.println(response.buffer());
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           //System.out.println(new String(response.buffer()));
       });
        PageNavigateOptions options1 = new PageNavigateOptions();
        //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
        options1.setWaitUntil(Collections.singletonList("domcontentloaded"));
        page.goTo("https://item.taobao.com/item.htm?id=541605195654",options1);

    }
}
