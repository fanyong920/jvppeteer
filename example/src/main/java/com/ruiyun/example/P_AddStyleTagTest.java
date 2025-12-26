package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.FrameAddStyleTagOptions;
import com.ruiyun.jvppeteer.cdp.entities.ScreenshotOptions;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class P_AddStyleTagTest {

    @Test
    public void test4() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        //打开一个页面
        Page page = browser.newPage();
        FrameAddStyleTagOptions options = new FrameAddStyleTagOptions();
        options.setPath("E:\\puppeteer\\test\\assets\\injectedstyle.css");
        page.addStyleTag(options);
        Object evaluate = page.evaluate(
                "window.getComputedStyle(document.querySelector('body')).getPropertyValue('background-color')"
        );
        System.out.println("evaluate=" + evaluate);
        browser.close();
    }

    @Test
    public void test5() throws Exception {

        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        FrameAddStyleTagOptions options = new FrameAddStyleTagOptions();
        //读取指定路径下的style样式，写进去页面
        options.setPath("C:\\Users\\fanyong\\Desktop\\test.css");
        page.addStyleTag(options);
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.png");
        screenshotOptions.setFullPage(true);
        page.screenshot(screenshotOptions);
//        page.$("#su").screenshot("baidu2.png");
        browser.close();
    }
}
