package com.ruiyun.jvppeteer.options;

import java.util.List;

public class BrowserLaunchArgumentOptions extends Timeoutable {
	private boolean headlessShell;
	/**
	 * 是否是无厘头
	 * <br/>
     * 默认是 true
     */
    private boolean headless = true;
    /**
     *其他参数，点击
     *  <a href="https://peter.sh/experiments/chromium-command-line-switches/">这里 </a>可以看到参数
     * <br/>
     */
    private List<String> args ;
    /**
     * 用户数据存储的目录
     * <br/>
     * Path to a User Data Directory.
     */
    private String userDataDir;
    /**
     * 是否打开devtool,也就是F12打开的开发者工具
     * <br/>
     */
    private boolean devtools;
	/**
	 * Specify the debugging port number to use
	 */
	private int debuggingPort;
    
	public boolean getHeadless() {
		return headless;
	}
	
	public void setHeadless(boolean headless) {
		this.headless = headless;
	}
	
	public List<String> getArgs() {
		return args;
	}
	
	public void setArgs(List<String> args) {
		this.args = args;
	}
	
	public String getUserDataDir() {
		return userDataDir;
	}
	
	public void setUserDataDir(String userDataDir) {
		this.userDataDir = userDataDir;
	}
	
	public boolean getDevtools() {
		return devtools;
	}
	
	public void setDevtools(boolean devtools) {
		this.devtools = devtools;
	}

	public int getDebuggingPort() {
		return debuggingPort;
	}

	public void setDebuggingPort(int debuggingPort) {
		this.debuggingPort = debuggingPort;
	}

	public boolean getHeadlessShell() {
		return headlessShell;
	}

	public void setHeadlessShell(boolean headlessShell) {
		this.headlessShell = headlessShell;
	}
}