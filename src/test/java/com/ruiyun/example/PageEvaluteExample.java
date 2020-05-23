package com.ruiyun.example;

import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PageEvaluteExample {

    public static void main(String[] args) throws IOException {
        //String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();
        String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");

       ThreadPoolExecutor executor = new ThreadPoolExecutor(4,4,40,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        CompletionService service = new ExecutorCompletionService(executor);
        List<Future> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Future submit = service.submit(() -> {
                //定义执行的方法 ，在java这里不能像nodejs一样直接书写js代码，这里以字符串代替，可以在vs code上编辑代码后再粘贴过来即可。
                String pageFunction = "() => {\n" +
                        "    return {\n" +
                        "      width: document.documentElement.clientWidth,\n" +
                        "      height: document.documentElement.clientHeight,\n" +
                        "      deviceScaleFactor: window.devicePixelRatio\n" +
                        "    };\n" +
                        "  }";
                Object result = page.evaluate(pageFunction, PageEvaluateType.FUNCTION/**指明pageFunction字符串是一个方法,而不是单单一个字符串*/);
                System.out.println("result:" + Constant.OBJECTMAPPER.writeValueAsString(result));
                return true;
            });
            futureList.add(submit);
        }

        for (int i = 0; i < 20; i++) {
            futureList.get(i);
        }



    }
}
