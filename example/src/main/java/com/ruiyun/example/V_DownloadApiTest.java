package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.common.ChromeReleaseChannel;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.FetcherOptions;
import com.ruiyun.jvppeteer.cdp.entities.RevisionInfo;
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
        fetcherOptions.setProduct(Product.Chromedriver);
        fetcherOptions.setVersion("129.0.6668.100");
        RevisionInfo revisionInfo3 = Puppeteer.downloadBrowser(fetcherOptions);
        System.out.println("revisionInfo3: " + revisionInfo3);


        //下载指定版本的Chrome Headless Shell
        FetcherOptions fetcherOptions2 = new FetcherOptions();
        fetcherOptions2.setProduct(Product.Chrome_headless_shell);
        fetcherOptions2.setVersion("129.0.6668.100");
        RevisionInfo revisionInfo4 = Puppeteer.downloadBrowser(fetcherOptions2);
        System.out.println("revisionInfo4: " + revisionInfo4);

        //下载指定版本的CHROMIUM
        FetcherOptions fetcherOptions3 = new FetcherOptions();
        fetcherOptions3.setProduct(Product.Chromium);
        fetcherOptions3.setVersion("1366415");
        RevisionInfo revisionInfo5 = Puppeteer.downloadBrowser(fetcherOptions3);
        System.out.println("revisionInfo5: " + revisionInfo5);

        launchOptions.setProduct(Product.Chrome);
        Browser cdpBrowser = Puppeteer.launch(launchOptions);
        String version = cdpBrowser.version();
        System.out.println("cdp协议获取到版本信息1：" + version);
        cdpBrowser.close();

        launchOptions.setProduct(Product.Chrome_headless_shell);
        launchOptions.setHeadless(true);
        Browser cdpBrowser2 = Puppeteer.launch(launchOptions);
        String version2 = cdpBrowser2.version();
        System.out.println("cdp协议获取到版本信息2：" + version2);
        cdpBrowser2.close();


        //启动CHROMIUM
        launchOptions.setProduct(Product.Chromium);
        launchOptions.setHeadless(true);
        Browser cdpBrowser3 = Puppeteer.launch(launchOptions);
        String version3 = cdpBrowser3.version();
        System.out.println("cdp协议获取到版本信息3：" + version3);
        cdpBrowser3.close();

        //CHROMEDRIVER不适应程序的启动逻辑，这里只是验证是否能正确找到浏览器版本
        launchOptions.setProduct(Product.Chromedriver);
        launchOptions.setHeadless(true);
        Browser cdpBrowser4 = Puppeteer.launch(launchOptions);
        String version4 = cdpBrowser4.version();
        System.out.println("cdp协议获取到版本信息4：" + version4);
        cdpBrowser4.close();

    }

    @Test
    public void test4() throws Exception {

        //下载 CHROMIUM 的 CANARY 下的最新版本
        FetcherOptions fetcherOptions1 = new FetcherOptions();
        fetcherOptions1.setChannel(ChromeReleaseChannel.LATEST);//CHROMIUM 只支持 latest
        fetcherOptions1.setProduct(Product.Chromium);
        RevisionInfo revisionInfo1 = Puppeteer.downloadBrowser(fetcherOptions1);
        System.out.println("revisionInfo1: " + revisionInfo1);

        //下载 CHROME 的 114.0.5708 milestone 下的最新版本
        FetcherOptions fetcherOptions2 = new FetcherOptions();
        fetcherOptions2.setProduct(Product.Chrome);
        fetcherOptions2.setMilestone("117");
        RevisionInfo revisionInfo2 = Puppeteer.downloadBrowser(fetcherOptions2);
        System.out.println("revisionInfo2: " + revisionInfo2);

        //下载 CHROMEDRIVER 的 117 build 下的最新版本
        FetcherOptions fetcherOptions3 = new FetcherOptions();
        fetcherOptions3.setProduct(Product.Chromedriver);
        fetcherOptions3.setBuild("132.0.6784");
        RevisionInfo revisionInfo3 = Puppeteer.downloadBrowser(fetcherOptions3);
        System.out.println("revisionInfo3: " + revisionInfo3);

    }

}
