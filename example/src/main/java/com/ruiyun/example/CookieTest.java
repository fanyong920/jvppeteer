package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieData;
import com.ruiyun.jvppeteer.cdp.entities.CookieParam;
import com.ruiyun.jvppeteer.cdp.entities.CookieSameSite;
import com.ruiyun.jvppeteer.cdp.entities.DeleteCookiesRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.ruiyun.example.LaunchTest.LAUNCHOPTIONS;
import static org.junit.Assert.assertTrue;

public class CookieTest {

    @Test
    public void test1() throws Exception {
        //should properly report "Default" sameSite cookie
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com");
        CookieParam cookieParam = new CookieParam();
        cookieParam.setName("a");
        cookieParam.setValue("b");
        cookieParam.setSameSite(CookieSameSite.Default);
        page.setCookie(cookieParam);
        List<Cookie> cookies = page.cookies();
        // 检查cookie是否被添加
        boolean cookieExists = false;
        for (Cookie c : cookies) {
            if ("a".equals(c.getName())) {
                cookieExists = true;
                break;
            }
        }
        assertTrue("Cookie 'a' should exist", cookieExists);
        List<DeleteCookiesRequest> deleteCookiesParameters = new ArrayList<>();
        for (Cookie cookie : cookies) {
            deleteCookiesParameters.add(new DeleteCookiesRequest(cookie.getName(), null, cookie.getDomain(), cookie.getPath()));
        }
        page.deleteCookie(deleteCookiesParameters.toArray(new DeleteCookiesRequest[0]));
        List<Cookie> cookies1 = page.cookies();
        boolean cookieExists1 = false;
        for (Cookie c : cookies1) {
            if ("a".equals(c.getName())) {
                cookieExists1 = true;
                break;
            }
        }
        assertTrue("Cookie 'a' should exist", cookieExists1);
    }
}
