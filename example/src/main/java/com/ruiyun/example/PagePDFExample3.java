package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.util.Helper;
import javafx.scene.control.TableRow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class PagePDFExample3 {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        //自动下载，第一次下载后不会再下载
//        BrowserFetcher.downloadIfNotExist(null);
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        //生成pdf必须在无厘头模式下才能生效
//        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
//        ArrayList<String> arrayList = new ArrayList<>();
        //生成pdf必须在无厘头模式下才能生效
//        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        List<PuppeteerLifeCycle> list = new ArrayList<>();
        list.add(PuppeteerLifeCycle.NETWORKIDLE2);
        list.add(PuppeteerLifeCycle.LOAD);
        list.add(PuppeteerLifeCycle.DOMCONTENTLOADED);
        list.add(PuppeteerLifeCycle.NETWORKIDLE);
        GoToOptions goToOptions = new GoToOptions();
        goToOptions.setWaitUntil(list);

        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",goToOptions,true);
        Page page2 = browser.newPage();
        page2.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",goToOptions,true);
        Page page3 = browser.newPage();
        page3.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3",goToOptions,true);
        ForkJoinPool.commonPool().submit(() -> {
            //page2.emulateVisionDeficiency(VisionDeficiency.DEUTERANOPIA);
            page2.pdf("2.pdf");
        });
        ForkJoinPool.commonPool().submit(() -> {
            //page3.emulateVisionDeficiency(VisionDeficiency.BLURREDVISION);
            page3.pdf("3.pdf");
        });
        //page.emulateVisionDeficiency(VisionDeficiency.ACHROMATOPSIA);
        page.pdf("1.pdf");


//        page.close();
//        browser.close();
    }
}
