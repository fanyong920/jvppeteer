package com.ruiyun.jvppeteer.events.application.definition;

import java.util.EventObject;

/**
 * event
 * @author fff
 *
 */
public abstract class ApplicationEvent extends EventObject {

	private static final long serialVersionUID = 1648486240814391405L;

	/**
	 *
	 * @param source
	 */
	public ApplicationEvent(Object source) {
		super(source);
	}

}
