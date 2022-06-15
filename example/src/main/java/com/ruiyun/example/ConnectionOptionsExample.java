package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.ConnectionOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;

import java.util.ArrayList;

/**
 * @author sage.xue
 * @date 2022/6/15 15:20
 */
public class ConnectionOptionsExample {

    public static void main(String[] args) throws Exception {
        ArrayList<String> arrayList = new ArrayList<>();

        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.setSessionWaitingResultTimeout(100000);

        LaunchOptions options = new LaunchOptionsBuilder()
                .withArgs(arrayList)
                .withHeadless(true)
                .withConnectionOptions(connectionOptions)
                .build();

        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        page.setDefaultTimeout(1000 * 60);
        page.goTo("https://www.baidu.com", true);

        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setType("png");
        screenshotOptions.setFullPage(Boolean.TRUE);
        screenshotOptions.setPath("D:\\Development\\Logs\\test-1234.png");
        String base64Str = page.screenshot(screenshotOptions);
        System.out.println("==== "+base64Str);

        browser.close();
    }

}
