package com.ruiyun.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.FileChooser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PageFileChooserExample {

    public static void main(String[] args) throws InterruptedException, ExecutionException, UnsupportedEncodingException {
        String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        //String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        page.goTo("https://sm.ms/");
        ClickOptions clickOptions = new ClickOptions();
        Future<FileChooser> fileChooserFuture = page.waitForFileChooser(30000);
        page.click("#smfile",clickOptions);
        FileChooser fileChooser = fileChooserFuture.get();
        List<String> paths = new ArrayList<>();
        paths.add("C:\\Users\\fanyong\\Pictures\\Saved Pictures\\IMG_20180820_174915.jpg");
        fileChooser.accept(paths);
    }
}
