package com.ruiyun.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;

public class PageEvaluteExample {

    public static void main(String[] args) throws JsonProcessingException, UnsupportedEncodingException {
        String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        //String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        //定义执行的方法
        String pageFunction = "() => {\n" +
                "    return {\n" +
                "      width: document.documentElement.clientWidth,\n" +
                "      height: document.documentElement.clientHeight,\n" +
                "      deviceScaleFactor: window.devicePixelRatio\n" +
                "    };\n" +
                "  }";
        Object result = page.evaluate(pageFunction, PageEvaluateType.FUNCTION/**指明pageFunction字符串是一个方法,而不是单单一个字符串*/);
        //waifor tracingComplete
        System.out.println("result:"+ Constant.OBJECTMAPPER.writeValueAsString(result));
    }
}
