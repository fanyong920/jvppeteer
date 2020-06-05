package com.ruiyun.jvppeteer.events;

import java.util.EventListener;
/**
 * listener event then handler it
 * @author fff
 *
 *
 */

@FunctionalInterface
public interface BrowserListener<T> extends EventListener {

	void onBrowserEvent(T event);
}
