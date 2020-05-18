package com.ruiyun.jvppeteer.options;

public class Timeoutable {
	
	/**
	 * 最大导航时间是30000ms,0表示无限等待
	 * <br/>
     * Maximum navigation time in milliseconds, pass 0 to disable timeout.
     * @default 30000
     */
	private int timeout = 30000;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
}
