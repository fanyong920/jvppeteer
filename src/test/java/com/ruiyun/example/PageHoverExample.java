package com.ruiyun.example;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PageHoverExample {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(), "UTF-8");

        // String  path ="D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");

        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();

        page.goTo("https://blog.csdn.net/pythonw/article/details/80263428");

        page.hover("#mainBox > main > div.template-box > span:nth-child(3)");
    }
}
