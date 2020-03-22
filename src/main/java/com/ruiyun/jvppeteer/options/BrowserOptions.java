package com.ruiyun.jvppeteer.options;

public class BrowserOptions extends ChromeArgOptions {

	/**
	 * 是否在导航期间忽略 HTTPS 错误. 默认是 false
   * <br/>
   * Whether to ignore HTTPS errors during navigation.
   * 
   * @default false
   */
	private boolean ignoreHTTPSErrors;
	/**
	   *     为每个页面设置一个默认视口大小。默认是 800x600。如果为 null 的话就禁用视图口。
	 * <br/>
	 * Sets a consistent viewport for each page. Defaults to an 800x600 viewport. null disables the default viewport.
	 */
	private DefaultViewport defaultViewport = new DefaultViewport();
	/**
	 *  将 Puppeteer 操作减少指定的毫秒数。这样你就可以看清发生了什么，这很有用
	 *  <br/>
	 *  Slows down Puppeteer operations by the specified amount of milliseconds.
	 *  Useful so that you can see what is going on.
	 */
	private int slowMo;
	  
	public boolean getIgnoreHTTPSErrors() {
		return ignoreHTTPSErrors;
	}
	
	public void setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
		this.ignoreHTTPSErrors = ignoreHTTPSErrors;
	}
	
	public DefaultViewport getDefaultViewport() {
		return defaultViewport;
	}
	
	public void setDefaultViewport(DefaultViewport defaultViewport) {
		this.defaultViewport = defaultViewport;
	}
	
	public int getSlowMo() {
		return slowMo;
	}
	
	public void setSlowMo(int slowMo) {
		this.slowMo = slowMo;
	}
	  
}
