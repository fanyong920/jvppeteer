package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

import java.util.ArrayList;

public class PageOnExample {
    public static void main(String[] args) throws Exception {


        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        /*CDPSession cdpSession = page.target().createCDPSession();
        Map<String, Object> params = new HashMap<>();
        JsonNode send = cdpSession.send("Browser.getWindowForTarget", params, true);
        JsonNode windowId = send.get("windowId");
        System.out.println(windowId.asText());*/
        //browser.close();

       Browser browser2 = Puppeteer.launch(options);

        browser.close();
        browser2.close();
    }
}
