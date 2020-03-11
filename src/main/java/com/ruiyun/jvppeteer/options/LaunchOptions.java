package com.ruiyun.jvppeteer.options;


import com.ruiyun.jvppeteer.Environment;

public class LaunchOptions extends BrowserOptions {

	public LaunchOptions() {
		
	}
	/**
	 * 可运行 Chromium 或 Chrome 可执行文件的路径，而不是绑定的 Chromium。如果 executablePath 是一个相对路径，那么他相对于 当前工作路径 解析
	 * <br/>
	 * Path to a Chromium executable to run instead of bundled Chromium. If
	 * executablePath is a relative path, then it is resolved relative to current
	 * working directory.
	 */
	private String executablePath;
	
	/**
	 * 忽略所有的默认参数,这个选项请谨慎使用。默认为 false。
	 * 
	 * <br/>
	 * Do not use `puppeteer.defaultArgs()` for launching Chromium.
	 * @default false
	 */
	private boolean ignoreDefaultArgs;
	/**
	 * 过滤掉给定的默认参数。
	 * <br/>
	 */
	private String[] ignoreAllDefaultArgs;
	
	/**
	 * Ctrl-C 关闭浏览器进程。默认是 true
	 * Close chrome process on Ctrl-C.
	 * @default true
	 */
	private boolean handleSIGINT = true;
	
	/**
	 * 关闭 SIGTERM 上的浏览器进程。默认是 true
	 * Close chrome process on SIGTERM.
	 * @default true
	 */
	private boolean handleSIGTERM = true;
	
	/**
	 * 关闭 SIGHUP 上的浏览器进程。默认是 true 
	 * <br/>
	 * Close chrome process on SIGHUP.
	 * @default true
	 */
	private boolean handleSIGHUP = true;
	
	/**
	 * 是否将浏览器进程标准输出和标准错误输入到 process.stdout 和 process.stderr 中。默认是 false。
	 * <br/>
	 * Whether to pipe browser process stdout and stderr into process.stdout and
	 * process.stderr.
	 * @default false
	 */
	private boolean dumpio ;
	  
	 /**
	  * 指定浏览器可见的环境变量。默认是 System.getEnv()
	  * <br/>
	  * Specify environment variables that will be visible to Chromium.
	  * @default `process.env`.
	  */
	private Environment env;
	
	/**
	 * 通过管道而不是WebSocket连接到浏览器。默认是 false
	 * Connects to the browser over a pipe instead of a WebSocket.
	 * @default false
	 */
	private boolean pipe;
	
	/**
	 * 启动的产品：chrome or firefox
	 */
	private String product;

	public String getExecutablePath() {
		return executablePath;
	}

	public void setExecutablePath(String executablePath) {
		this.executablePath = executablePath;
	}

	public boolean isIgnoreDefaultArgs() {
		return ignoreDefaultArgs;
	}

	public void setIgnoreDefaultArgs(boolean ignoreDefaultArgs) {
		this.ignoreDefaultArgs = ignoreDefaultArgs;
	}

	public String[] getIgnoreAllDefaultArgs() {
		return ignoreAllDefaultArgs;
	}

	public void setIgnoreAllDefaultArgs(String[] ignoreAllDefaultArgs) {
		this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
	}

	public boolean isHandleSIGINT() {
		return handleSIGINT;
	}

	public void setHandleSIGINT(boolean handleSIGINT) {
		this.handleSIGINT = handleSIGINT;
	}

	public boolean isHandleSIGTERM() {
		return handleSIGTERM;
	}

	public void setHandleSIGTERM(boolean handleSIGTERM) {
		this.handleSIGTERM = handleSIGTERM;
	}

	public boolean isHandleSIGHUP() {
		return handleSIGHUP;
	}

	public void setHandleSIGHUP(boolean handleSIGHUP) {
		this.handleSIGHUP = handleSIGHUP;
	}

	public boolean isDumpio() {
		return dumpio;
	}

	public void setDumpio(boolean dumpio) {
		this.dumpio = dumpio;
	}

	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

	public boolean isPipe() {
		return pipe;
	}

	public void setPipe(boolean pipe) {
		this.pipe = pipe;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}
	 
	 
}
