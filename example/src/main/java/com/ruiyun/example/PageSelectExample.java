package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.Viewport;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面原始select标签选择动作
 *  
 * @author YuChen
 * @date 2020/10/13 14:26
 **/
 
public class PageSelectExample {

    public static void main(String[] args) throws Exception {
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Viewport viewport = new Viewport();
        viewport.setHeight(1080);
        viewport.setWidth(1920);
        options.setViewport(viewport);
        //获取当前classPath
        String classPath = PageSelectExample.class.getResource("/").toString();
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo(classPath+"/testSelect.html");
        List<String> cc = new ArrayList<>();
        cc.add("3");
        Thread.sleep(3000);
        page.select("#select_01",cc);
    }
}
