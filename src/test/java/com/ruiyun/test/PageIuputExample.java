package com.ruiyun.test;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PageIuputExample {

    public static void main(String[] args) throws InterruptedException, ExecutionException, UnsupportedEncodingException {
        //String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        ClickOptions clickOptions = new ClickOptions();
//        Future<FileChooser> fileChooserFuture = page.waitForFileChooser(30000);
//        Thread.sleep(3000);
        ElementHandle elementHandle = page.$("#su");
        elementHandle.type("我是樊勇",0);
        elementHandle.press("Enter",0,null);
//        FileChooser fileChooser = fileChooserFuture.get();
//        List<String> paths = new ArrayList<>();
//        paths.add("C:\\Users\\howay\\Desktop\\sunway.png");
//        fileChooser.accept(paths);
    }
}
