package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class B_PageGotoTest extends A_LaunchTest {
    /**
     * 新建一个页面，并打开网址
     */
    @Test
    public void test2() throws Exception {
        //启动浏览器
        try  {
            Browser browser = Puppeteer.launch(launchOptions);
            //打开一个页面
            Page page = browser.newPage();
            GoToOptions options = new GoToOptions();
            List<PuppeteerLifeCycle> waitUntil = new ArrayList<>();
            //页面加载到 networkidle状态下 goto才算完成
            waitUntil.add(PuppeteerLifeCycle.networkIdle);
            options.setWaitUntil(waitUntil);
            page.goTo("https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Map", options);
            //不添加waitUntil参数，默认是load
            page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
                    Thread.sleep(5000);
            System.out.println("完成了。。。");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //用打开浏览器就有的页面 去打开网址
    @Test
    public void test3() throws Exception {
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
