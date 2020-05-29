package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.JSHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.console.Location;

import java.util.ArrayList;

public class PageSomeFunctionExample {
    public static void main(String[] args) throws Exception {
        String path = new String("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe".getBytes(), "UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
       // String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).withDumpio(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        //关闭缓存，默认是开启的
        page.setCacheEnabled(false);
        //切换绕过页面的内容安全策略，默认是true
        page.setBypassCSP(false);
        page.setGeolocation(30.31667,59.95);

        page.setJavaScriptEnabled(true);
        page.goTo("https://www.meituan.com/");

        //点击 深圳 按钮
        page.tap("#react > div > div.citylist > p > a:nth-child(4)");

        String title = page.title();
        System.out.println("title6666:"+title);
        //启用离线模式，启用后不能连接互联网
//        page.setOfflineMode(true);

        //在这里将抛错 net::ERR_INTERNET_DISCONNECTED
       // page.goTo("https://item.taobao.com/item.htm?id=541605195654");

    }
}
