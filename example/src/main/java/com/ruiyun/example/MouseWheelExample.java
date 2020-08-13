package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Clip;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import org.junit.Test;

import java.util.ArrayList;

public class MouseWheelExample {

    @Test
    public void test1() throws Exception {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://mdn.mozillademos.org/en-US/docs/Web/API/Element/wheel_event$samples/Scaling_an_element_via_the_wheel?revision=1587366");

        ElementHandle elem = page.$("div");
        Clip boundingBox = elem.boundingBox();

        //鼠标移动到目标
        page.mouse().move(boundingBox.getX() + boundingBox.getWidth() / 2,
                boundingBox.getY() + boundingBox.getHeight() / 2);

        //开始鼠标滚动
        page.mouse().wheel(0.00,-100);

        //观察效果
        Thread.sleep(4000L);
    }
}
