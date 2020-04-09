package com.ruiyun.jvppeteer.options;

public class BrowserOptions extends ChromeArgOptions {

	/**
	 * ?????????????? HTTPS ????. ????? false
   * <br/>
   * Whether to ignore HTTPS errors during navigation.
   * 
   * @default false
   */
	private boolean ignoreHTTPSErrors;
	/**
	   *     ?????????????????????§³??????? 800x600?????? null ??????????????
	 * <br/>
	 * Sets a consistent viewport for each page. Defaults to an 800x600 viewport. null disables the default viewport.
	 */
	private Viewport viewport = new Viewport();
	/**
	 *  ?? Puppeteer ????????????????????????????????³²???????????????
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
	
	public Viewport getViewport() {
		return viewport;
	}
	
	public void setViewport(Viewport viewport) {
		this.viewport = viewport;
	}
	
	public int getSlowMo() {
		return slowMo;
	}
	
	public void setSlowMo(int slowMo) {
		this.slowMo = slowMo;
	}
	  
}
