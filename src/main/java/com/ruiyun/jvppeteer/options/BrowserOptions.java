package com.ruiyun.jvppeteer.options;

public class BrowserOptions extends ChromeArgOptions {

	/**
	 *
   * <br/>
   * Whether to ignore HTTPS errors during navigation.
   * 
   * 默认是false
   */
	private boolean ignoreHTTPSErrors;
	/**
	   *     800x600
	 * <br/>
	 * Sets a consistent viewport for each page. Defaults to an 800x600 viewport. null disables the default viewport.
	 */
	private Viewport viewport = new Viewport();
	/**
	 *
	 *  <br/>
	 *  Slows down Puppeteer operations by the specified amount of milliseconds.
	 *  Useful so that you can see what is going on.
	 */
	private int slowMo;
	/**
	 * 浏览器与CDP的连接配置
	 */
	private ConnectionOptions connectionOptions = new ConnectionOptions();

	public BrowserOptions() {
		super();
	}

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

	public ConnectionOptions getConnectionOptions() {
		return connectionOptions;
	}

	public void setConnectionOptions(ConnectionOptions connectionOptions) {
		this.connectionOptions = connectionOptions;
	}
}
