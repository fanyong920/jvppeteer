package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.JSHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Request;
import com.ruiyun.jvppeteer.core.page.Response;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.util.ArrayList;

public class PageWaitForExample {
    public static void main(String[] args) throws Exception {
        String path = new String("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe".getBytes(), "UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        // String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).withDumpio(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        //waitForResponse 必须异步
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",true);

        //JSHandle jsHandle = page.waitFor("#su", PageEvaluateType.STRING);
       // System.out.println("page.waitFor:"+jsHandle.toString());

        //Response response = page.waitForResponse("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
       // System.out.println("page.waitForResponse:"+response.status());

//        Request request = page.waitForRequest("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
//        System.out.println(request.requestId()+":"+request.resourceType());
        ElementHandle elementHandle = page.waitForSelector("#s-top-left > div > a");
        System.out.println(elementHandle.toString());
    }
}
