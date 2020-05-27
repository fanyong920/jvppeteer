package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;

import java.util.ArrayList;

/**
 * 监听页面事件的例子
 */
public class PageEventListenerExample {

    public static void main(String[] args) throws Exception {
        String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        //监听事件
        page.onClose((ignore) ->{
            System.out.println("页面关闭了...");
        });
        page.onConsole(consoleMessage -> {
            System.out.println("页面有打印事件，打印的信息是"+consoleMessage);
        });
        page.onLoad((ignore) -> {
            System.out.println("页面加载完成...");
        });
        page.onDialg((dialog) -> {
            System.out.println("页面有弹窗事件..."+dialog);
        });
        page.onError((error) -> {
            System.out.println("页面有错误事件..."+error.getMessage());
        });
        page.onMetrics((metrics) -> {
            System.out.println("页面有metrics事件..."+metrics);
        });
        page.onFrameattached((frame) -> {
            System.out.println("页面有Frameattached事件..."+frame.getName());
        });
        page.onFramenavigated((frame) -> {
            System.out.println("页面有Framenavigated事件..."+frame.getName());
        });
        page.onFramedetached((frame) -> {
            System.out.println("页面有Framedetached事件..."+frame.getName());
        });
        page.onPageerror(exception -> {
            System.out.println("页面有pageerror事件..."+exception.getMessage());
        });
        page.onPopup((popup) -> {
            System.out.println("页面有popup事件..."+popup.getMessage());
        });
        page.onRequest((request) -> {
            System.out.println("页面有请求发出..."+request.url());
        });
        page.onRequestfailed((request) -> {
            System.out.println("页面发出的请求失败了..."+request.url());
        });
        page.onRequestfinished((request) -> {
            System.out.println("页面的请求结束了..."+request.url());
        });
        page.onResponse((response) -> {
            System.out.println("页面请求["+response.url()+"]对应的响应是..."+response.status());
        });
        page.onWorkercreated((worker) -> {
            System.out.println("页面上有个worker被创建了..."+worker.url());
        });
        page.onWorkerdestroyed((worker) -> {
            System.out.println("页面上有个worker消失了..."+worker.url());
        });
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        page.close();
    }
}
