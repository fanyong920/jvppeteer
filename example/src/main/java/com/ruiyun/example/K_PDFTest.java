package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.GoToOptions;
import com.ruiyun.jvppeteer.entities.PDFMargin;
import com.ruiyun.jvppeteer.entities.PDFOptions;
import com.ruiyun.jvppeteer.entities.PaperFormats;
import com.ruiyun.jvppeteer.entities.PuppeteerLifeCycle;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

public class K_PDFTest extends A_LaunchTest {
    /**
     * 要打印成什么样子的pdf，自己手动在浏览器按Ctrl+p，弹出的窗口就是pdf样式，，再把各个选项点一下，看一下预览效果
     * 然后再回来写代码
     */
    @Test
    public void test2() throws Exception {
        setPdfOptions();
        //launchOptions.setHeadlessShell(true);//chrome-headless-shell浏览器才能用这个参数，这个是旧的headless模式
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        GoToOptions goToOptions = new GoToOptions();
        goToOptions.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.NETWORKIDLE));
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg",goToOptions);
        PDFOptions pdfOptions = new PDFOptions();
        pdfOptions.setPath("baidu.pdf");
        pdfOptions.setOutline(true);//生成大纲
        pdfOptions.setFormat(PaperFormats.a4);//A4大小
        pdfOptions.setPrintBackground(true);//打印背景图形，百度一下这个蓝色按钮就显示出来了
        pdfOptions.setPreferCSSPageSize(false);
        pdfOptions.setScale(1.1);//缩放比例1.1
        page.pdf(pdfOptions);
        //关闭浏览器
        browser.close();
    }

    private void setPdfOptions() {
        //pdf必须配置headless = true
        launchOptions.setHeadless(true);
        ArrayList<String> args = new ArrayList<>();//添加一些额外的启动参数
        args.add("--no-sandbox");//pdf必须添加这个参数,不然无法打印，具体看这里https://github.com/puppeteer/puppeteer/issues/12470
        launchOptions.setArgs(args);
    }

    @Test
    public void test3() throws Exception {
        setPdfOptions();
        //launchOptions.setHeadlessShell(true);//chrome-headless-shell浏览器才能用这个参数，这个是旧的headless模式
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        GoToOptions goToOptions = new GoToOptions();
        //这个网页要设置页面加载到NETWORKIDLE状态才能打印，不然打印空白
        goToOptions.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.NETWORKIDLE));
        page.goTo("https://ysotmain.ysclass.net/zhxy/#/redirect/login",goToOptions);
        PDFOptions pdfOptions = new PDFOptions();
        pdfOptions.setPath("login.pdf");
        pdfOptions.setOutline(true);//生成大纲
        pdfOptions.setPrintBackground(true);//打印背景图形
        pdfOptions.setPreferCSSPageSize(false);
        PDFMargin pdfMargin = new PDFMargin();
        pdfMargin.setTop("1cm");//px,in,cm,mm等单位都可以，会自动转换
        pdfMargin.setBottom("1cm");
        pdfMargin.setLeft("1cm");
        pdfMargin.setRight("1cm");
        pdfOptions.setMargin(pdfMargin);//有页边距才能显示页脚
        pdfOptions.setDisplayHeaderFooter(true);//打印页眉页脚
        page.pdf(pdfOptions);
        //关闭浏览器
        browser.close();
    }

    @Test
    public void test4() throws Exception {
        setPdfOptions();
        //launchOptions.setHeadlessShell(true);//chrome-headless-shell浏览器才能用这个参数，这个是旧的headless模式
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        GoToOptions goToOptions = new GoToOptions();
        //这个网页要设置页面加载到NETWORKIDLE状态才能打印，不然打印空白
        goToOptions.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.NETWORKIDLE));
        page.goTo("https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=68018901_16_pg&wd=github%20issue%E6%8F%92%E5%85%A5%E4%BB%A3%E7%A0%81&fenlei=256&rsv_pq=0xbd5e4b7d00111880&rsv_t=6f17bbxW8QaU8EIJyxwmrz7SMP%2BoYZZ%2FmyCT8UNsYdh1vYqTbeQ6agOLXO7x%2Fs0MSCPjSI0&rqlang=en&rsv_dl=tb&rsv_enter=1&rsv_sug3=55&rsv_sug1=43&rsv_sug7=101&rsv_sug2=0&rsv_btype=i&inputT=21300&rsv_sug4=21887",goToOptions);
        PDFOptions pdfOptions = new PDFOptions();
        pdfOptions.setFooterTemplate("<div style=\"box-sizing: border-box; width: 100%; height: 40px; display: flex; justify-content: space-between; align-items: center; margin-bottom: -18px; padding: 0 40px; font-family: PingFangSC-Regular; font-size: 12px;\">\n" +
                "    <div style=\"color: #fafafa;\">测试页码页脚</div>\n" +
                "    <div style=\"display: flex; justify-content: space-between; align-items: center; width: 100px; color: #666666;\">\n" +
                "        <div style=\"font-family: PingFangSC-Semibold; font-weight: bold; color: #BFBFBF;\"><span>共</span> <span class=\"totalPages\"></span> <span>页</span></div>\n" +
                "        <div style=\"font-family: PingFangSC-Semibold; font-weight: bold; color: #BFBFBF;\"><span>第</span> <span class=\"pageNumber\"></span> <span>页</span></div>\n" +
                "    </div>\n" +
                "</div>");
        pdfOptions.setPath("pageNumber.pdf");
        pdfOptions.setOutline(true);//生成大纲
        pdfOptions.setPrintBackground(true);//打印背景图形
        pdfOptions.setPreferCSSPageSize(false);
        PDFMargin pdfMargin = new PDFMargin();
        pdfMargin.setTop("40");//px,in,cm,mm等单位都可以，会自动转换
        pdfMargin.setBottom("40");
        pdfMargin.setLeft("40");
        pdfMargin.setRight("40");
        pdfOptions.setMargin(pdfMargin);//有页边距才能显示页脚
        pdfOptions.setDisplayHeaderFooter(true);//打印页眉页脚
        page.pdf(pdfOptions);
        //关闭浏览器
        browser.close();
    }
}
