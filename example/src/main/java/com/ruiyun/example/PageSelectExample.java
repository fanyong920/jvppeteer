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
        page.setContent("<html>\n" +
                "\n" +
                "<head>\n" +
                "<title>我的第一个 HTML 页面</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "<p>body 元素的内容会显示在浏览器中。</p>\n" +
                "<p>title 元素的内容会显示在浏览器的标题栏中。</p>\n" +
                "</body>\n" +
                " <select class=\"dType\" id=\"dType\" style=\"width: 100px;height: 85px\">\n" +
                "            <option value=\"\">请选择</option>\n" +
                "            <option value=\"1\">男</option>\n" +
                "            <option value=\"2\">女</option>\n" +
                "    </select>\n" +
                "</html>");
        List<String> cc = new ArrayList<>();
        cc.add("1");
        Thread.sleep(3000);
        page.select("#dType",cc);
    }
}
