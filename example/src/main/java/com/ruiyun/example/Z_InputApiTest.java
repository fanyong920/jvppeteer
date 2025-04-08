package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ClickOptions;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import java.util.function.Consumer;
import org.junit.Test;


import static com.ruiyun.example.A_LaunchTest.LAUNCHOPTIONS;

public class Z_InputApiTest {
    /**
     * 双击按钮测试
     */
    @Test
    public void test11() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        //设置html页面，有三个按钮，点击后打印出按钮的文本
        page.setContent("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Button Example</title>\n" +
                "    <style>\n" +
                "        .button-class {\n" +
                "            /* 你可以在这里添加CSS样式 */\n" +
                "            background-color: #4CAF50;\n" +
                "            /* Green */\n" +
                "            border: none;\n" +
                "            color: white;\n" +
                "            padding: 15px 32px;\n" +
                "            text-align: center;\n" +
                "            text-decoration: none;\n" +
                "            display: inline-block;\n" +
                "            font-size: 16px;\n" +
                "            margin: 4px 2px;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <div class=\"button\">\n" +
                "        <!-- 使用相同的类名 -->\n" +
                "        <button class=\"button-class\">按钮1</button>\n" +
                "    </div>\n" +
                "</body>\n" +
                "\n" +
                "</html>");
        page.evaluate("() => {\n" +
                "    (globalThis).count = 0;\n" +
                "    const button = document.querySelector('button');\n" +
                "    button.addEventListener('dblclick', () => {\n" +
                "      (globalThis).count++;\n" +
                "    });\n" +
                "  }\n");
        ElementHandle button = page.$(".button-class");
        ClickOptions clickOptions = new ClickOptions();
        clickOptions.setClickCount(2);
        button.click(clickOptions);
        button.click(clickOptions);
        Object count = page.evaluate("count");
        System.out.println("count = "+ count);
        browser.close();
    }
}
