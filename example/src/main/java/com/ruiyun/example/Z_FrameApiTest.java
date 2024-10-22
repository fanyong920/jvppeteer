package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.ElementHandle;
import com.ruiyun.jvppeteer.core.Frame;
import com.ruiyun.jvppeteer.core.Page;
import org.junit.Test;

public class Z_FrameApiTest extends A_LaunchTest {

    @Test
    public void test2() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.setContent("<div id=\"shadow-host\"></div>\n" +
                "                        <script>\n" +
                "                          const host = document.getElementById('shadow-host');\n" +
                "                          const shadowRoot = host.attachShadow({ mode: 'closed' });\n" +
                "                          const frame = document.createElement('iframe');\n" +
                "                          frame.srcdoc = '<p>Inside frame</p>';\n" +
                "                          frame.src = 'https://example.org';\n" +
                "                          shadowRoot.appendChild(frame);\n" +
                "                        </script>"

        );
        //Frame frame = page.waitForFrame(frame1 -> frame1.url().equals("https://example.org"));
        Frame frame = page.frames().get(1);


        ElementHandle frameElement = frame.frameElement();
        Object evaluate = frameElement.evaluate("el => {\n" +
                "                          return el.tagName.toLocaleLowerCase();\n" +
                "                        }");
        System.out.println("iframe".equals(evaluate));
    }
}
