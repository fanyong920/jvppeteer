package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.common.QueryHandler;
import com.ruiyun.jvppeteer.util.GetQueryHandler;
import java.util.List;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.getBrowser;

public class Z_SelectorTest {
    /**
     * xpath选择器
     */
    @Test
    public void test1() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.setContent("<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>示例页面</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>这是一个标题</h1>\n" +
                "    <h2>这是一个二级标题</h2>\n" +
                "    <p>这是一些段落文本。</p>\n" +
                "</body>\n" +
                "</html>");
        ElementHandle h2Handle = page.$("::-p-xpath(//h2)");
        System.out.println(h2Handle.evaluate("node => node.textContent"));
        browser.close();
    }


    //文本选择器
    @Test
    public void test2() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.setContent("< !DOCTYPE html >\n" +
                "  <html lang=\"zh-CN\">\n" +
                "    <head>\n" +
                "      <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "          <title>示例页面</title>\n" +
                "        </head>\n" +
                "        <body>\n" +
                "          <div id=\"checkout\">Div Checkout<p>\n" +
                "            Checkout mini\n" +
                "          </p></div>\n" +
                "          <p>这是一些段落文本。</p>\n" +
                "        </body>\n" +
                "      </html>");
        ElementHandle textHandle = page.$("div ::-p-text(mini)");
        System.out.println(textHandle.evaluate("node => node.textContent"));
        browser.close();
    }

    //aria选择器
    @Test
    public void test3() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.setContent("< !DOCTYPE html >\n" +
                "  <html lang=\"zh-CN\">\n" +
                "    <head>\n" +
                "      <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "          <title>示例页面</title>\n" +
                "        </head>\n" +
                "        <body>\n" +
                "        <button aria-label=\"Click me\" role=\"button\">Click me</button>\n" +
                "          <p>这是一些段落文本。</p>\n" +
                "        </body>\n" +
                "      </html>");
        ElementHandle ariaHandle = page.$("::-p-aria([name=\"Click me\"][role=\"button\"])");
        System.out.println(ariaHandle.evaluate("node => node.textContent"));
        browser.close();
    }

    //pierce选择器
    @Test
    public void test4() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        ElementHandle pierceHandle = page.$("& >>> div");
        if (pierceHandle != null) {
            System.out.println(pierceHandle.evaluate("node => node.textContent"));
        }
        browser.close();
    }

    /**
     * xpath选择器
     */
    @Test
    public void test5() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.goTo("http://www.baidu.com");
        List<ElementHandle> $$ = page.$$("xpath=//span[text()='换一换']");
        System.out.println($$.get(0).evaluate("node => node.textContent"));
        Thread.sleep(3000);
        $$.get(0).click();
        browser.close();
    }

    /**
     * 深度组合器
     */
    @Test
    public void test6() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.goTo("https://www.bilibili.com/video/BV18FZbYUE3r/?spm_id_from=333.1007.tianma.3-2-8.click&vd_source=e01673c7341a338bf0bbf8bb26a8fe2e");
        ElementHandle handle = page.waitForSelector("#commentapp > bili-comments >>> div > #feed > bili-comment-thread-renderer:nth-child(1) >>> #comment >>> #content > bili-rich-text >>> #contents > span:nth-child(4)");
        System.out.println(handle.evaluate("node => node.textContent"));
        browser.close();
    }

    /**
     * 自定义选择器
     */
    @Test
    public void test7() throws Exception {
        GetQueryHandler.registerCustomQueryHandler("reactComponent", new QueryHandler() {
            @Override
            public String querySelector() {
                return "(elementOrDocument, selector) => {\n" +
                        "    // Dummy example just delegates to querySelector but you can find your\n" +
                        "    // React component because this callback runs in the page context.\n" +
                        "    return elementOrDocument.querySelector(`[id=\"${CSS.escape(selector)}\"]`);\n" +
                        "  }";
            }

            @Override
            public String querySelectorAll() {
                return "(elementOrDocument, selector) => {\n" +
                        "    // Dummy example just delegates to querySelector but you can find your\n" +
                        "    // React component because this callback runs in the page context.\n" +
                        "    return elementOrDocument.querySelectorAll(`[id=\"${CSS.escape(selector)}\"]`);\n" +
                        "  }";
            }
        });
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.goTo("https://www.bilibili.com/video/BV18FZbYUE3r/?spm_id_from=333.1007.tianma.3-2-8.click&vd_source=e01673c7341a338bf0bbf8bb26a8fe2e");
        WaitForSelectorOptions options = new WaitForSelectorOptions();
        options.setTimeout(3000);
        //页面没有这个seletoc,这里会报错，仅测试registerCustomQueryHandler是否可用
        ElementHandle handle = page.waitForSelector("::-p-reactComponent(MyComponent)",options);
        System.out.println(handle.evaluate("node => node.textContent"));
        browser.close();
    }
}
