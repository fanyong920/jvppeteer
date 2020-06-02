package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.protocol.network.Cookie;
import com.ruiyun.jvppeteer.protocol.network.DeleteCookiesParameters;

import java.util.ArrayList;
import java.util.List;

public class PageCookieExample {
    public static void main(String[] args) throws Exception{
        //String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");

      String  path ="D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        List<Cookie> cookies = page.cookies();
        for (Cookie cookie : cookies) {
            System.out.println(cookie);
        }
        List<DeleteCookiesParameters> deleteCookiesParametersLits = new ArrayList<>();
        deleteCookiesParametersLits.add(new DeleteCookiesParameters("BAIDUID",null,".baidu.com","/"));
        page.deleteCookie(deleteCookiesParametersLits);
        System.out.println("====================删除cookies====================");
        List<Cookie> cookies2 = page.cookies();
        for (Cookie cookie : cookies2) {
            System.out.println(cookie);
        }
    }
}
