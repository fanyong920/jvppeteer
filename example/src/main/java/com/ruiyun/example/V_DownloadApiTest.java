package com.ruiyun.example;

import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.FetcherOptions;
import com.ruiyun.jvppeteer.entities.RevisionInfo;
import org.junit.Test;

public class V_DownloadApiTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
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
        fetcherOptions3.setVersion("1366415");
        RevisionInfo revisionInfo5 = Puppeteer.downloadBrowser(fetcherOptions3);
        System.out.println("revisionInfo5: " + revisionInfo5);

        launchOptions.setProduct(Product.CHROME);
        Browser browser = Puppeteer.launch(launchOptions);
        String version = browser.version();
        System.out.println("cdp协议获取到版本信息1：" + version);
        browser.close();

        launchOptions.setProduct(Product.CHROMEHEADLESSSHELL);
        launchOptions.setHeadless(true);
        Browser browser2 = Puppeteer.launch(launchOptions);
        String version2 = browser2.version();
        System.out.println("cdp协议获取到版本信息2：" + version2);
        browser2.close();


        //启动CHROMIUM
        launchOptions.setProduct(Product.CHROMIUM);
        launchOptions.setHeadless(true);
        Browser browser3 = Puppeteer.launch(launchOptions);
        String version3 = browser3.version();
        System.out.println("cdp协议获取到版本信息3：" + version3);
        browser3.close();


        //CHROMEDRIVER不适应程序的启动逻辑，这里只是验证是否能正确找到浏览器版本
        launchOptions.setProduct(Product.CHROMEDRIVER);
        launchOptions.setHeadless(true);
        Browser browser4 = Puppeteer.launch(launchOptions);
        String version4 = browser4.version();
        System.out.println("cdp协议获取到版本信息4：" + version4);
        browser4.close();


    }

}