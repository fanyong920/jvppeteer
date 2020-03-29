package com.ruiyun.jvppeteer.events.browser.definition;

import java.util.EventListener;
/**
 * listener event then handler it
 * @author fff
 *
 * @param <E>
 */

@FunctionalInterface
public interface BrowserListener  <E extends BrowserEvent> extends EventListener {

	void onBrowserEvent(E event);
}
