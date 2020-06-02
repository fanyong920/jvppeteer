package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.FileChooser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PageFileChooserExample {

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
//        String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        PageNavigateOptions options1 = new PageNavigateOptions();
        options1.setWaitUntil(Arrays.asList("domcontentloaded"));
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        Future<FileChooser> fileChooserFuture = page.waitForFileChooser(30000);
        ElementHandle elementHandle = page.$("#form > span.bg.s_ipt_wr.quickdelete-wrap > span.soutu-btn");
        elementHandle.click();
        //点击选择文件的按钮
        ElementHandle button = page.$("#form > div > div.soutu-state-normal > div.upload-wrap > input");
        button.click();
        //等待一个选择文件的弹窗事件返回
        FileChooser fileChooser = fileChooserFuture.get();

        //选择本地的文件
        List<String> paths = new ArrayList<>();
        paths.add("C:\\Users\\howay\\Desktop\\sunway.png");
        fileChooser.accept(paths);

    }
}
