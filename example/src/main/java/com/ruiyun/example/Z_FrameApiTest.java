package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class Z_FrameApiTest extends A_LaunchTest {

    @Test
    public void test() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.setContent("<div id=\"shadow-host\"></div>\n" +
                "        <script>\n" +
                "          const host = document.getElementById('shadow-host');\n" +
                "          const shadowRoot = host.attachShadow({ mode: 'closed' });\n" +
                "          const frame = document.createElement('iframe');\n" +
                "          frame.srcdoc = '<p>Inside frame</p>';\n" +
                "          shadowRoot.appendChild(frame);\n" +
                "        </script>");
        System.out.println("frame size:" + page.frames().size());
        Frame frame = page.frames().get(1);
        JSHandle frameElement = frame.frameElement();
        Object evaluate = frameElement.evaluate("el => {\n" +
                "          return el.tagName.toLocaleLowerCase();\n" +
                "        }");
        System.out.println("iframe".equals(evaluate));
    }

    @Test
    public void test1() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        Frame frame = page.mainFrame();
        Object evaluate = frame.evaluate("arr => {\n" +
                "        return arr;\n" +
                "      }", Collections.singletonList(Arrays.asList("1", "2", "3")));
        System.out.println(evaluate);
        browser.close();
    }
}
