package com.ruiyun.jvppeteer.launch;

import java.util.List;

import com.ruiyun.jvppeteer.types.browser.Browser;
import com.ruiyun.jvppeteer.options.BrowserOptions;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;

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
	public String defaultArgs(ChromeArgOptions options, List<String> chromeArguments) {
		return null;
	}


	@Override
	public String resolveExecutablePath(String chromeExecutable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, ConnectionTransport transport) {
		return null;
	}

	@Override
	public String executablePath() {
		return null;
	}


}
