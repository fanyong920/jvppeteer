package com.ruiyun.jvppeteer.launch;

import java.util.List;

import com.ruiyun.jvppeteer.browser.Browser;
import com.ruiyun.jvppeteer.options.LaunchOptions;

public class FirefoxLauncher implements Launcher {



	private boolean isPuppeteerCore;
	

	public FirefoxLauncher(boolean isPuppeteerCore) {
		super();
		this.isPuppeteerCore = isPuppeteerCore;
	}


	@Override
	public Browser launch(LaunchOptions options) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String defaultArgs(LaunchOptions options, List<String> chromeArguments) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String resolveExecutablePath(String chromeExecutable) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Browser connect(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

}
