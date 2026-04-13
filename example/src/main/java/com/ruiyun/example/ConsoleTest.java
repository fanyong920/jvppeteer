package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.util.Helper;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.ruiyun.example.LaunchTest.LAUNCHOPTIONS;
import static org.junit.Assert.assertTrue;

public class ConsoleTest {


    @Test
    public void test1() throws Exception {
        //should work on script call right after navigation
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        page.once(PageEvents.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue("SOME_LOG_MESSAGE".equals(message.text()));
        });
        page.goTo(
                // Firefox prints warn if <!DOCTYPE html> is not present
                "data:text/html,<!DOCTYPE html><script>console.log('SOME_LOG_MESSAGE');</script>"
        );
        Helper.justWait(1000);
        page.close();

        //should work for console.trace
        page = browser.newPage();
        page.once(PageEvents.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue(ConsoleMessageType.trace.equals(message.type()));
            assertTrue("calling console.trace".equals(message.text()));
        });
        page.evaluate("() => {\n" +
                "                console.trace('calling console.trace');\n" +
                "      }");
        Helper.justWait(1000);
        page.close();

        //should work for console.dir
        page = browser.newPage();
        page.once(PageEvents.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue(ConsoleMessageType.dir.equals(message.type()));
            assertTrue("calling console.dir".equals(message.text()));
        });
        page.evaluate("() => {\n" +
                "        console.dir('calling console.dir');\n" +
                "      }");
        Helper.justWait(1000);
        page.close();


        //should work for console.warn
        page = browser.newPage();
        page.once(PageEvents.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue(ConsoleMessageType.warn.equals(message.type()));
            assertTrue("calling console.warn".equals(message.text()));
        });
        page.evaluate("() => {\n" +
                "        console.dir('calling console.warn');\n" +
                "      }");
        Helper.justWait(1000);
        page.close();


        //should work for console.error
        page = browser.newPage();
        page.once(PageEvents.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue(ConsoleMessageType.error.equals(message.type()));
            assertTrue("calling console.error".equals(message.text()));
        });
        page.evaluate("() => {\n" +
                "        console.dir('calling console.error');\n" +
                "      }");
        Helper.justWait(1000);
        page.close();

        //should work for console.log with promise
        page = browser.newPage();
        page.once(PageEvents.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue(ConsoleMessageType.log.equals(message.type()));
            assertTrue(Arrays.asList("[promise Promise]","JSHandle@promise").contains(message.text()));
        });
        page.evaluate("() => {\n" +
                "        console.log(Promise.resolve('should not wait until resolved!'));\n" +
                "      }");
        Helper.justWait(1000);
        page.close();
        browser.close();
    }
}
