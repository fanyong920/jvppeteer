package com.ruiyun.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.ElementHandle;
import com.ruiyun.jvppeteer.core.Frame;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.util.Helper;
import org.junit.Test;

import java.util.function.Supplier;

public class Z_FrameApiTest extends A_LaunchTest {

    @Test
    public void test2() throws Exception {
        Browser browser = getBrowser();
        Page page = browser.newPage();
        page.setContent("""
                <div id="shadow-host"></div>
                        <script>
                          const host = document.getElementById('shadow-host');
                          const shadowRoot = host.attachShadow({ mode: 'closed' });
                          const frame = document.createElement('iframe');
                          frame.srcdoc = '<p>Inside frame</p>';
                          frame.src = 'https://example.org';
                          shadowRoot.appendChild(frame);
                        </script>
                """);
        //Frame frame = page.waitForFrame(frame1 -> frame1.url().equals("https://example.org"));
        Frame frame = page.frames().get(1);



        ElementHandle frameElement = frame.frameElement();
        Object evaluate = frameElement.evaluate("""
                el => {
                          return el.tagName.toLocaleLowerCase();
                        }""");
        System.out.println("iframe".equals(evaluate));
    }
}
