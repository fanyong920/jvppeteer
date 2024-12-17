package com.ruiyun.example;

import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.cdp.entities.RevisionInfo;

public class Test {
    public static void main(String[] args) throws Exception {
        LaunchOptions launchOptions = LaunchOptions.builder().
                //有界面模式 true未无界面
                        headless(true)
                //手动配置chrome执行路径
//            .withExecutablePath("C:\\Users\\fanyong\\Desktop\\chrome-win-131\\chrome-win\\chrome.exe").withDebuggingPort(9222)
//            .withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").withDebuggingPort(9222)
                .build();
        //采用默认配置下载浏览器，默认采用Chrome for Testing 浏览器。版本号Constant#Version
        RevisionInfo revisionInfo = Puppeteer.downloadBrowser();
        System.out.println("revisionInfo: " + revisionInfo);
//
//        //下载指定版本的chrome for Testing浏览器
//        RevisionInfo revisionInfo2 = Puppeteer.downloadBrowser("128.0.6613.137");
//        System.out.println("revisionInfo2: " + revisionInfo2);

        //下载指定版本的ChromeDriver
//        FetcherOptions fetcherOptions = new FetcherOptions();
//        fetcherOptions.setProduct(Product.Firefox);
//        RevisionInfo revisionInfo3 = Puppeteer.downloadBrowser(fetcherOptions);
//        System.out.println("revisionInfo3: " + revisionInfo3);


        //下载指定版本的Chrome Headless Shell
//        FetcherOptions fetcherOptions2 = new FetcherOptions();
//        fetcherOptions2.setProduct(Product.Chrome_headless_shell);
//        fetcherOptions2.setVersion("129.0.6668.100");
//        RevisionInfo revisionInfo4 = Puppeteer.downloadBrowser(fetcherOptions2);
//        System.out.println("revisionInfo4: " + revisionInfo4);
//
//        //下载指定版本的CHROMIUM
//        FetcherOptions fetcherOptions3 = new FetcherOptions();
//        fetcherOptions3.setProduct(Product.Chromium);
//        fetcherOptions3.setVersion("1366415");
//        RevisionInfo revisionInfo5 = Puppeteer.downloadBrowser(fetcherOptions3);
//        System.out.println("revisionInfo5: " + revisionInfo5);
//
//        launchOptions.setProduct(Product.Chrome);
//        Browser cdpBrowser = Puppeteer.launch(launchOptions);
//        String version = cdpBrowser.version();
//        System.out.println("cdp协议获取到版本信息1：" + version);
//        cdpBrowser.close();
//
//        launchOptions.setProduct(Product.Chrome_headless_shell);
//        launchOptions.setHeadless(true);
//        Browser cdpBrowser2 = Puppeteer.launch(launchOptions);
//        String version2 = cdpBrowser2.version();
//        System.out.println("cdp协议获取到版本信息2：" + version2);
//        cdpBrowser2.close();
//
//
//        //启动CHROMIUM
//        launchOptions.setProduct(Product.Chromium);
//        launchOptions.setHeadless(true);
//         Browser cdpBrowser3 = Puppeteer.launch(launchOptions);
//        String version3 = cdpBrowser3.version();
//        System.out.println("cdp协议获取到版本信息3：" + version3);
//        cdpBrowser3.close();
    }
}
