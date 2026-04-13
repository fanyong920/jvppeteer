package com.ruiyun.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.WebWorker;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.api.events.WebWorkerEvent;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.util.Helper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.ruiyun.example.LaunchTest.LAUNCHOPTIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WorkerTest {
    private WebWorker createWorker(Page page) throws JsonProcessingException {
        AwaitableResult<WebWorker> workerResult = AwaitableResult.create();
        page.on(PageEvents.WorkerCreated, (Consumer<WebWorker>) workerResult::complete);
        page.evaluate("() => {\n" +
                "        return new Worker(`data:text/javascript,1`);\n" +
                "      }");
        return workerResult.waitingGetResult();

    }

    @Test
    public void test1() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        WebWorker worker = createWorker(page);
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue((message.text().equals("hello 5 [object Object]") || message.text().equals("hello 5 JSHandle@object")/* WebDriver BiDi **/));
            assertEquals(ConsoleMessageType.log, message.type());
            assertEquals(3, message.args().size());
            try {
                assertEquals("hello", message.args().get(0).jsonValue());
                assertEquals(5, (int) (message.args().get(1).jsonValue()));
                System.out.println("console log1 done..." + message.args().get(2).jsonValue());
            } catch (JsonProcessingException e) {
                System.err.println(e.getMessage());
            }
        });
        worker.evaluate("() => {\n" +
                "          return console.log('hello', 5, {foo: 'bar'});\n" +
                "        }");
        Helper.justWait(1000);

        //should work for Error instances
        page = browser.newPage();
        worker = createWorker(page);
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue((message.text().equals("Error: test error")/* CDP expectation **/ || message.text().equals("JSHandle@error")/*BiDi current behavior **/));
            assertEquals(ConsoleMessageType.log, message.type());
            assertEquals(1, message.args().size());
            System.out.println("console log2 done...");
        });
        worker.evaluate("() => {\n" +
                "          return console.log(new Error('test error'));\n" +
                "        }");
        Helper.justWait(1000);

        //should return the first line of the error message in text()
        page = browser.newPage();
        worker = createWorker(page);
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue((message.text().equals("Error: test error")/* CDP expectation **/ || message.text().equals("JSHandle@error")/*BiDi current behavior **/));
            assertEquals(ConsoleMessageType.log, message.type());
            assertEquals(1, message.args().size());
            System.out.println("console log3 done...");
        });
        worker.evaluate("() => {\n" +
                "          return console.log(new Error('test error\\nsecond line'));\n" +
                "        }");
        Helper.justWait(1000);

        //should work for console.trace
        page = browser.newPage();
        worker = createWorker(page);
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) message -> {
            assertEquals("calling console.trace", message.text());
            assertEquals(ConsoleMessageType.trace, message.type());
            System.out.println("console trace done...");
        });
        worker.evaluate("() => {\n" +
                "          console.trace('calling console.trace');\n" +
                "        }");
        Helper.justWait(1000);

        //should work for console.dir
        page = browser.newPage();
        worker = createWorker(page);
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) message -> {
            assertEquals("calling console.dir", message.text());
            assertEquals(ConsoleMessageType.dir, message.type());
            System.out.println("console dir done...");
        });
        worker.evaluate("() => {\n" +
                "          console.dir('calling console.dir');\n" +
                "        }");
        Helper.justWait(1000);

        //should work for console.warn
        page = browser.newPage();
        worker = createWorker(page);
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) message -> {
            assertEquals("calling console.warn", message.text());
            assertEquals(ConsoleMessageType.warn, message.type());
            System.out.println("console warn done...");
        });
        worker.evaluate("() => {\n" +
                "          console.warn('calling console.warn');\n" +
                "        }");
        Helper.justWait(1000);

        //should work for console.error
        page = browser.newPage();
        worker = createWorker(page);
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) message -> {
            assertEquals("calling console.error", message.text());
            assertEquals(ConsoleMessageType.error, message.type());
            System.out.println("console error done...");
        });
        worker.evaluate("() => {\n" +
                "          console.error('calling console.error');\n" +
                "        }");
        Helper.justWait(1000);

        //should work for console.log with promise
        page = browser.newPage();
        worker = createWorker(page);
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) message -> {
            assertTrue((message.text().equals("[promise Promise]") || message.text().equals("JSHandle@promise")/* WebDriver BiDi **/));
            assertEquals(ConsoleMessageType.log, message.type());
            System.out.println("console promise done...");
        });
        worker.evaluate("() => {\n" +
                "          console.log(Promise.resolve('should not wait until resolved!'));\n" +
                "        }");
        Helper.justWait(1000);


        //should work for different console API calls with timing functions
        page = browser.newPage();
        worker = createWorker(page);
        List<ConsoleMessage> messages = new ArrayList<>();
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) messages::add);
        // All console events will be reported before `worker.evaluate` is finished.
        worker.evaluate("() => {\n" +
                "        // A pair of time/timeEnd generates only one Console API call.\n" +
                "        console.time('calling console.time');\n" +
                "        console.timeEnd('calling console.time');\n" +
                "      }");
        assertTrue(messages.get(0).text().contains("calling console.time"));
        assertEquals(ConsoleMessageType.timeEnd, messages.get(0).type());
        System.out.println("console time done...");
        Helper.justWait(1000);

        //should work for different console API calls with group functions
        page = browser.newPage();
        worker = createWorker(page);
        List<ConsoleMessage> messages2 = new ArrayList<>();
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) messages2::add);
        // All console events will be reported before `worker.evaluate` is finished.
        worker.evaluate("() => {\n" +
                "        console.group('calling console.group');\n" +
                "        console.groupEnd();\n" +
                "      }");
        assertEquals("calling console.group", messages2.get(0).text());
        Helper.justWait(2000);
        assertEquals(ConsoleMessageType.startGroup, messages2.get(0).type());
        assertEquals(ConsoleMessageType.endGroup, messages2.get(1).type());
        System.out.println("console group done...");

        //should return remote objects
        page = browser.newPage();
        worker = createWorker(page);
        AwaitableResult<ConsoleMessage> result1 = new AwaitableResult<>();
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) result1::complete);
        // All console events will be reported before `worker.evaluate` is finished.
        worker.evaluate("() => {\n" +
                "        globalThis.test = 1;\n" +
                "        console.log(1, 2, 3, globalThis);\n" +
                "      }");
        ConsoleMessage consoleMessage1 = result1.waitingGetResult();
        assertTrue((consoleMessage1.text().equals("1 2 3 [object DedicatedWorkerGlobalScope]") || consoleMessage1.text().equals("1 2 3 JSHandle@object")/* WebDriver BiDi **/));
        assertEquals(4, consoleMessage1.args().size());
        assertEquals(1, consoleMessage1.args().get(3).getProperty("test").jsonValue());
        System.out.println("console remote objects done...");

        //should have location and stack trace for console API calls
        page = browser.newPage();
        worker = createWorker(page);
        AwaitableResult<ConsoleMessage> result2 = new AwaitableResult<>();
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) result2::complete);
        // All console events will be reported before `worker.evaluate` is finished.
        worker.evaluate("() => {\n" +
                "          function consoleTrace() {\n" +
                "            console.trace('yellow');\n" +
                "          }\n" +
                "          consoleTrace();\n" +
                "        }");
        ConsoleMessage consoleMessage2 = result2.waitingGetResult();
        assertEquals("yellow", consoleMessage2.text());
        assertEquals(ConsoleMessageType.trace, consoleMessage2.type());
        assertNotNull(consoleMessage2.location().getUrl());
        assertFalse(consoleMessage2.stackTrace().isEmpty());

        //should not dispose handles when worker has listeners
        page = browser.newPage();
        worker = createWorker(page);
        AwaitableResult<ConsoleMessage> result3 = new AwaitableResult<>();
        worker.on(WebWorkerEvent.Console, (Consumer<ConsoleMessage>) result3::complete);
        // All console events will be reported before `worker.evaluate` is finished.
        worker.evaluate("() => {\n" +
                "          return console.log({foo: 'bar'});\n" +
                "        }");
        ConsoleMessage consoleMessage3 = result3.waitingGetResult();
        JSHandle handle = consoleMessage3.args().get(0);
        assertFalse(handle.disposed());
        assertEquals("{foo=bar}", handle.jsonValue().toString());
        handle.dispose();
        assertTrue(handle.disposed());
    }
}



