package com.ruiyun.jvppeteer.launch;

import com.ruiyun.jvppeteer.Browser;
import com.ruiyun.jvppeteer.Launcher;
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

}
