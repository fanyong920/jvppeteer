package com.ruiyun.jvppeteer.options;

public class Timeoutable {
	
	/**
	 * 等待浏览器实例启动的最长时间（以毫秒为单位）。默认是 30000 (30 秒). 通过 0 来禁用超时。
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
