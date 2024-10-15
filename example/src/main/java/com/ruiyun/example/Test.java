package com.ruiyun.example;

import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.FetcherOptions;
import com.ruiyun.jvppeteer.entities.GoToOptions;
import com.ruiyun.jvppeteer.entities.ImageType;
import com.ruiyun.jvppeteer.entities.LaunchOptions;
import com.ruiyun.jvppeteer.entities.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.entities.PDFOptions;
import com.ruiyun.jvppeteer.entities.PaperFormats;
import com.ruiyun.jvppeteer.entities.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.entities.RevisionInfo;
import com.ruiyun.jvppeteer.entities.ScreenshotOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        //采用默认配置下载浏览器，默认采用Chrome for Testing 浏览器。版本号Constant#Version
        RevisionInfo revisionInfo = Puppeteer.downloadBrowser();
        System.out.println("revisionInfo: " + revisionInfo);

        //下载指定版本的chrome for Testing浏览器
        RevisionInfo revisionInfo2 = Puppeteer.downloadBrowser("128.0.6613.137");
        System.out.println("revisionInfo2: " + revisionInfo2);

        //下载指定版本的ChromeDriver
        FetcherOptions fetcherOptions = new FetcherOptions();
        fetcherOptions.setProduct(Product.CHROMEDRIVER);
        fetcherOptions.setVersion("129.0.6668.100");
        RevisionInfo revisionInfo3 = Puppeteer.downloadBrowser(fetcherOptions);
        System.out.println("revisionInfo3: " + revisionInfo3);


        //下载指定版本的Chrome Headless Shell
        FetcherOptions fetcherOptions2 = new FetcherOptions();
        fetcherOptions2.setProduct(Product.CHROMEHEADLESSSHELL);
        fetcherOptions2.setVersion("129.0.6668.100");
        RevisionInfo revisionInfo4 = Puppeteer.downloadBrowser(fetcherOptions2);
        System.out.println("revisionInfo4: " + revisionInfo4);

        //下载指定版本的CHROMIUM
        FetcherOptions fetcherOptions3 = new FetcherOptions();
        fetcherOptions3.setProduct(Product.CHROMIUM);
        fetcherOptions3.setVersion("1368115");
        RevisionInfo revisionInfo5 = Puppeteer.downloadBrowser(fetcherOptions3);
        System.out.println("revisionInfo5: " + revisionInfo5);

        LaunchOptions launchOptions = new LaunchOptionsBuilder().build();
        launchOptions.setArgs(List.of("--no-sandbox"));
        launchOptions.setProduct(Product.CHROME);
        Browser browser = Puppeteer.launch(launchOptions);
        String version = browser.version();
        System.out.println("cdp协议获取到版本信息1：" + version);

        Page page = browser.newPage();
        GoToOptions goToOptions = new GoToOptions();
        goToOptions.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.NETWORKIDLE));
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg",goToOptions);
        PDFOptions pdfOptions = new PDFOptions();
        pdfOptions.setPath("baidu.pdf");
        pdfOptions.setOutline(true);//生成大纲
        pdfOptions.setFormat(PaperFormats.a4);//A4大小
        pdfOptions.setPrintBackground(true);//打印背景图形，百度一下这个蓝色按钮就显示出来了
        pdfOptions.setPreferCSSPageSize(false);
        pdfOptions.setScale(1.1);//缩放比例1.1
        page.pdf(pdfOptions);

        //打开一个页面
        Page page2 = browser.newPage();
        page2.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.png");
        //指定图片类型，path指定的名称中的后缀便不起作用了
        screenshotOptions.setType(ImageType.WEBP);
        //jpg可以设置这个选项
        screenshotOptions.setQuality(80.00);
        //全屏截图
        screenshotOptions.setFullPage(true);
        screenshotOptions.setCaptureBeyondViewport(true);
        page2.screenshot(screenshotOptions);

        browser.close();

        launchOptions.setProduct(Product.CHROMEHEADLESSSHELL);
        launchOptions.setHeadless(true);
        Browser browser2 = Puppeteer.launch(launchOptions);
        String version2 = browser2.version();
        System.out.println("cdp协议获取到版本信息2：" + version2);
        //关闭浏览器
        browser2.close();


        //启动CHROMIUM
        launchOptions.setProduct(Product.CHROMIUM);
        launchOptions.setHeadless(true);
        Browser browser3 = Puppeteer.launch(launchOptions);
        String version3 = browser3.version();
        System.out.println("cdp协议获取到版本信息3：" + version3);
        browser3.close();
    }
}
