package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.SerializedAXNode;
import com.ruiyun.jvppeteer.cdp.entities.SnapshotOptions;
import com.ruiyun.jvppeteer.common.Constant;
import org.junit.Test;

public class W_AccessibilityApiTest extends A_LaunchTest {

    @Test
    public void test3() throws Exception {
        Browser browser = Puppeteer.launch(launchOptions);
        Page page = browser.newPage();
        page.setContent("<!DOCTYPE html>\n" +
                "      <html lang=\"en\">\n" +
                "      <head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "        <title>Accessible name + aria-expanded puppeteer bug</title>\n" +
                "        <style>\n" +
                "          [aria-expanded=\"false\"] + * {\n" +
                "            display: none;\n" +
                "          }\n" +
                "        </style>\n" +
                "      </head>\n" +
                "      <body>\n" +
                "        <button hidden>Show</button>\n" +
                "        <p>Some content</p>\n" +
                "        <script>\n" +
                "          const button = document.querySelector('button');\n" +
                "          button.removeAttribute('hidden')\n" +
                "          button.setAttribute('aria-expanded', 'false');\n" +
                "          button.addEventListener('click', function() {\n" +
                "            button.setAttribute('aria-expanded', button.getAttribute('aria-expanded') !== 'true')\n" +
                "            if (button.getAttribute('aria-expanded') == 'true') {\n" +
                "              button.textContent = 'Hide'\n" +
                "            } else {\n" +
                "              button.textContent = 'Show'\n" +
                "            }\n" +
                "          })\n" +
                "        </script>\n" +
                "      </body>\n" +
                "      </html>");
        ElementHandle button = page.$("button");
        SerializedAXNode snapshot = page.accessibility().snapshot(new SnapshotOptions(true, button, false));
        System.out.println(snapshot);
        button.click();
        ElementHandle elementHandle = page.waitForSelector("aria/Hide");
        System.out.println(elementHandle);
    }

}
