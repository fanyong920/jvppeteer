package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.entities.*;
import org.junit.Test;

public class Q_ScreenshotTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {

        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.png");
        screenshotOptions.setOmitBackground(true);
        //全屏截图
        screenshotOptions.setFullPage(true);
        //截图的更多
        screenshotOptions.setCaptureBeyondViewport(true);
        page.screenshot(screenshotOptions);
        browser.close();
    }

    @Test
    public void test4() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.png");
        //指定图片类型，path指定的名称中的后缀便不起作用了
        screenshotOptions.setType(ImageType.JPEG);
        //jpg可以设置这个选项
        screenshotOptions.setQuality(80.00);
        //全屏截图
        screenshotOptions.setFullPage(true);

        page.screenshot(screenshotOptions);
        browser.close();
    }

    @Test
    public void test5() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        //打开一个页面
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.jpeg");
        //指定图片类型，path指定的名称中的后缀便不起作用了
        screenshotOptions.setType(ImageType.WEBP);
        //jpg可以设置这个选项
        screenshotOptions.setQuality(80.00);
        //全屏截图
        screenshotOptions.setFullPage(true);

        page.screenshot(screenshotOptions);
        browser.close();
    }
    //某个元素截图
    @Test
    public void test6() throws Exception {

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
}
