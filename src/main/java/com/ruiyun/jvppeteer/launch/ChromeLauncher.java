package com.ruiyun.jvppeteer.launch;

import com.ruiyun.jvppeteer.Browser;
import com.ruiyun.jvppeteer.Launcher;
import com.ruiyun.jvppeteer.options.LaunchOptions;

public class ChromeLauncher implements Launcher {
	
	
	private boolean isPuppeteerCore;
	
	
	public ChromeLauncher(boolean isPuppeteerCore) {
		super();
		this.isPuppeteerCore = isPuppeteerCore;
	}


	@Override
	public Browser launch(LaunchOptions options) {
		
		return null;
	}

}
