package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.VisionDeficiency;
import com.ruiyun.jvppeteer.util.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PagePDFExample3 {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> arrayList = new ArrayList<>();
        //生成pdf必须在无厘头模式下才能生效
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        Page page2 = browser.newPage();
        page2.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        Page page3 = browser.newPage();
        page3.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        Helper.commonExecutor().submit(() -> {
            page2.emulateVisionDeficiency(VisionDeficiency.DEUTERANOPIA);
            try {
                page2.pdf("deuteranopia.pdf");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Helper.commonExecutor().submit(() -> {
            page3.emulateVisionDeficiency(VisionDeficiency.BLURREDVISION);
            try {
                page3.pdf("blurred-vision.pdf");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        page.emulateVisionDeficiency(VisionDeficiency.ACHROMATOPSIA);
        page.pdf("achromatopsia.pdf");


//        page.close();
//        browser.close();
    }
}
