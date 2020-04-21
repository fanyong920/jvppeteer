package com.ruiyun.jvppeteer.events.browser.definition;

import java.util.EventObject;

/**
 * event
 * @author fff
 *
 */
public abstract class BrowserEvent extends EventObject {

	private static final long serialVersionUID = -7244839099664970173L;
	
	/**
	 * 
	 * @param source ÊÂ¼þÔ´
	 */
	public BrowserEvent(Object source) {
		super(source);
	}

}
