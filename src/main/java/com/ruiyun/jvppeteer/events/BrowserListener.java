package com.ruiyun.jvppeteer.events;

import java.util.EventListener;
/**
 * listener event then handler it
 * @author fff
 *
 * @param <T>
 */

@FunctionalInterface
public interface BrowserListener<T> extends EventListener {

	void onBrowserEvent(T event);
}
