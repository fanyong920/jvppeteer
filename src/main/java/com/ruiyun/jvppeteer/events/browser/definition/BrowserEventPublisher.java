package com.ruiyun.jvppeteer.events.browser.definition;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * publish event
 *
 * @author fff
 */
@FunctionalInterface
public interface BrowserEventPublisher {

    void publishEvent(String method,Object event);
}
