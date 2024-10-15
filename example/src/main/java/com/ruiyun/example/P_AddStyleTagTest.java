package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.FrameAddStyleTagOptions;
import com.ruiyun.jvppeteer.entities.ScreenshotOptions;
import org.junit.Test;

public class P_AddStyleTagTest extends A_LaunchTest {

    @Test
    public void test4() throws Exception {

        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        FrameAddStyleTagOptions options = new FrameAddStyleTagOptions();
        //修改一下百度一下按钮的颜色
        options.setContent("#head_wrapper .s_btn{cursor:pointer;width:108px;height:44px;line-height:45px;line-height:44px\\9;padding:0;background:0 0;background-color:#b75014;border-radius:0 10px 10px 0;font-size:17px;color:#fff;box-shadow:none;font-weight:400;border:none;outline:0}");
        page.addStyleTag(options);
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.png");
        screenshotOptions.setFullPage(true);
        page.screenshot(screenshotOptions);
        page.$("#su").screenshot("baidu2.png");
        browser.close();
    }

    @Test
    public void test5() throws Exception {

        Browser browser = Puppeteer.launch(launchOptions);
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
