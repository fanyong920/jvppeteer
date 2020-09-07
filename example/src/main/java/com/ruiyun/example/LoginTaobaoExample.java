package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;

import java.util.ArrayList;
import java.util.List;

public class LoginTaobaoExample {
    public static void main(String[] args) throws Exception {
        ArrayList<String> argList = new ArrayList<>();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        String join = String.join(",", argList);
        System.out.println(join);
        LaunchOptions options = new LaunchOptionsBuilder()
                .withArgs(argList)
                .withHeadless(false)
                .build();
        //启动
        List<String> sss = new ArrayList<>();
        sss.add("--enable-automation");
        options.setIgnoreDefaultArgs(sss);
        Browser browser = Puppeteer.launch(options);
        com.ruiyun.jvppeteer.core.page.Page pages = browser.newPage();
        pages.setDefaultNavigationTimeout(300 * 1000);//设置5分钟的超时时间

//        pages.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");

        //设置参数防止检测
        pages.evaluateOnNewDocument("() =>{ Object.defineProperties(navigator,{ webdriver:{ get: () => undefined } }) }");
        pages.evaluateOnNewDocument("() =>{ window.navigator.chrome = { runtime: {},  }; }");
        pages.evaluateOnNewDocument("() =>{ Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] }); }");
        pages.evaluateOnNewDocument("() =>{ Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5,6], }); }");

        String url = "https://login.taobao.com/member/login.jhtml";
        pages.goTo(url);
        //等待元素加载完成，输入账号密码并提交
        pages.waitForSelector("#fm-login-id");
        pages.type("#fm-login-id", "2", 100);
        Thread.sleep(1000);
        pages.type("#fm-login-password", "password", 100);
        Thread.sleep(1000);
        pages.click(".fm-button");
    }
}
