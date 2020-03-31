package com.ruiyun.test;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.browser.Browser;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.page.Page;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class CreatNewPageTest {

    @Test
    public void newPage() throws UnsupportedEncodingException {
        String path = new String("D:\\软件\\nodeproject\\crawlItem-puppeteer-pool\\node_modules\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"utf-8");
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        System.out.println(page);
    }
}
