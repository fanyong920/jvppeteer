package com.ruiyun.jvppeteer.events.browser.definition;

/**
 * publish event
 *
 * @author fff
 */
@FunctionalInterface
public interface BrowserEventPublisher {

    void publishEvent(String method,Object event);
}
