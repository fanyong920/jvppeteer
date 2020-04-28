package com.ruiyun.jvppeteer.options;

import java.util.List;

public class ChromeArgOptions extends Timeoutable {
	/**
	 * 是否是无厘头
	 * <br/>
     * Whether to run browser in headless mode.
     * @default true unless the devtools option is true.
     */
    private boolean headless = true;
    /**
     *其他参数，在下面的连接可以看到
     *  https://peter.sh/experiments/chromium-command-line-switches/
     * <br/>
     * Additional arguments to pass to the browser instance.
     * The list of Chromium flags can be found here.
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
     * Whether to auto-open a DevTools panel for each tab.
     * If this option is true, the headless option will be set false.
     */
    private boolean devtools;
    
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
    
    
}
