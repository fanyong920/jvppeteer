package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Target;
import com.ruiyun.jvppeteer.entities.GoToOptions;
import com.ruiyun.jvppeteer.entities.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.entities.TargetType;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class B_PageGotoTest extends A_LaunchTest {
    /**
     * 新建一个页面，并打开网址
     */
    @Test
    public void test2() throws IOException {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            GoToOptions options = new GoToOptions();
            List<PuppeteerLifeCycle> waitUntil = new ArrayList<>();
            //页面加载到 networkidle状态下 goto才算完成
            waitUntil.add(PuppeteerLifeCycle.NETWORKIDLE);
            options.setWaitUntil(waitUntil);
            page.goTo("https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Map", options);
            //不添加waitUntil参数，默认是load
            page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        }
    }

    //用打开浏览器就有的页面 去打开网址
    @Test
    public void test3() throws IOException {
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            List<Target> targets = browser.targets();
            Target target = targets.stream().filter(t -> t.type().equals(TargetType.PAGE)).findFirst().orElse(null);
            if (target == null) {
                return;
            }
            Page page = target.page();
            page.goTo("https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Map");
            //不添加waitUntil参数，默认是load
            page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        }
    }
}
