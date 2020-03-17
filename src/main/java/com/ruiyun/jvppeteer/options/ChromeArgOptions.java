package com.ruiyun.jvppeteer.options;

import java.util.List;

public class ChromeArgOptions {
	/**
	 * 是否以 无头模式 运行浏览器。默认是 true，除非 devtools 选项是 true
	 * <br/>
     * Whether to run browser in headless mode.
     * @default true unless the devtools option is true.
     */
    private boolean headless = true;
    /**
     * 
     *  传递给浏览器实例的其他参数  可以看这里：
     *  https://peter.sh/experiments/chromium-command-line-switches/
     * <br/>
     * Additional arguments to pass to the browser instance.
     * The list of Chromium flags can be found here.
     */
    private List<String> args ;
    /**
     * 用户数据目录 路径
     * <br/>
     * Path to a User Data Directory.
     */
    private String userDataDir;
    /**
     * 是否为每个选项卡自动打开DevTools面板。如果这个选项是 true，headless 选项将会设置成 false。
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
