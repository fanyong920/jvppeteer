package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Dialog;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.entities.PageMetrics;
import org.junit.Test;

import java.util.function.Consumer;

public class G_PageContentTest extends A_LaunchTest {

    @Test
    public void test2() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            //点击确认框的确定按钮
            // dialog.accept("确定");
            //解除对话框，就是关闭对话框
            page.on(Page.PageEvent.Dialog, (Consumer<Dialog>) Dialog::dismiss);
            page.on(Page.PageEvent.Console, (Consumer<ConsoleMessage>) consoleMessage -> System.out.println("console=" + consoleMessage.text()));
            //手动设置页面内容
            page.setContent("<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Confirm Example</title>\n" +
                    "    <script>\n" +
                    "        document.addEventListener('DOMContentLoaded', function() {\n" +
                    "            setTimeout(function() {\n" +
                    "                var result = confirm('这是一个确认对话框，请选择确定或取消');\n" +
                    "                if (result) {\n" +
                    "                    console.log('用户点击了确定');\n" +
                    "                } else {\n" +
                    "                    console.log('用户点击了取消');\n" +
                    "                }\n" +
                    "            }, 2000);\n" +
                    "        });\n" +
                    "    </script>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h1>欢迎来到示例页面</h1>\n" +
                    "</body>\n" +
                    "</html>\n");
        }
    }

    @Test
    public void test3() throws Exception {
        //启动浏览器
        try (Browser browser = Puppeteer.launch(launchOptions)) {
            //打开一个页面
            Page page = browser.newPage();
            page.on(Page.PageEvent.Metrics, (Consumer<PageMetrics>) System.out::println);
            //手动设置页面内容
            String content = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Console TimeStamp Example</title>\n" +
                    "            <script>\n" +
                    "            document.addEventListener('DOMContentLoaded', function() {\n" +
                    "        setTimeout(function() {\n" +
                    "            console.timeStamp('Page Loaded');\n" +
                    "        }, 1000);\n" +
                    "    });\n" +
                    "    </script>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h1>欢迎来到示例页面</h1>\n" +
                    "</body>\n" +
                    "</html>\n";
            page.setContent(content);
            //我们获取设置后的页面内容
            String content1 = page.content();
            System.out.println("通过api获取的页面内容： " + content1);
        }
    }
}
