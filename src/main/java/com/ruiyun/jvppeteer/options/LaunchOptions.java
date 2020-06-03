package com.ruiyun.jvppeteer.options;


import java.util.List;

import com.ruiyun.jvppeteer.core.Environment;

public class LaunchOptions extends BrowserOptions {

	public LaunchOptions() {
		super();
	}
	/**
	 * 设置chrome浏览器的路径
	 * <br/>
	 * Path to a Chromium executable to run instead of bundled Chromium. If
	 * executablePath is a relative path, then it is resolved relative to current
	 * working directory.
	 */
	private String executablePath;
	
	/**
	 * 默认是false
	 */
	private boolean ignoreAllDefaultArgs;

	private List<String> ignoreDefaultArgs;
	
	/**

	 * Close chrome process on Ctrl-C.
	 * 默认是true
	 */
	private boolean handleSIGINT = true;
	
	/**
	 * Close chrome process on SIGTERM.
	 * 默认是 true
	 */
	private boolean handleSIGTERM = true;
	
	/**
	 * Close chrome process on SIGHUP.
	 * 默认是 true
	 */
	private boolean handleSIGHUP = true;
	
	/**
	 * 将cheome的标准输出流输入流转换到java程序的标准输入输出,java默认已经将子进程的输入和错误流通过管道重定向了，现在这个参数暂时用不上
	 * <br/>
	 * Whether to pipe browser process stdout and stderr into process.stdout and
	 * process.stderr.
	 * 默认是 false
	 */
	private boolean dumpio ;
	  
	 /**
	  * ָSystem.getEnv()
	  * <br/>
	  * Specify environment variables that will be visible to Chromium.
	  * 默认是 `process.env`.
	  */
	private Environment env;
	
	/**
	 * ͨfalse代表使用websocket通讯，true代表使用websocket通讯
	 * Connects to the browser over a pipe instead of a WebSocket.
	 * 默认是  false
	 */
	private boolean pipe;
	
	/**
	 * chrome or firefox
	 */
	private String product;


	public String getExecutablePath() {
		return executablePath;
	}

	public void setExecutablePath(String executablePath) {
		this.executablePath = executablePath;
	}

	

	public boolean getIgnoreAllDefaultArgs() {
		return ignoreAllDefaultArgs;
	}

	public void setIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
		this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
	}

	public List<String> getIgnoreDefaultArgs() {
		return ignoreDefaultArgs;
	}

	public void setIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
		this.ignoreDefaultArgs = ignoreDefaultArgs;
	}

	public boolean getHandleSIGINT() {
		return handleSIGINT;
	}

	public void setHandleSIGINT(boolean handleSIGINT) {
		this.handleSIGINT = handleSIGINT;
	}

	public boolean getHandleSIGTERM() {
		return handleSIGTERM;
	}

	public void setHandleSIGTERM(boolean handleSIGTERM) {
		this.handleSIGTERM = handleSIGTERM;
	}

	public boolean getHandleSIGHUP() {
		return handleSIGHUP;
	}

	public void setHandleSIGHUP(boolean handleSIGHUP) {
		this.handleSIGHUP = handleSIGHUP;
	}

	public boolean getDumpio() {
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

	public boolean getPipe() {
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
