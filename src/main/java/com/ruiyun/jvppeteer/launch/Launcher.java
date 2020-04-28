package com.ruiyun.jvppeteer.launch;

import java.util.List;

import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.Environment;
import com.ruiyun.jvppeteer.browser.Browser;
import com.ruiyun.jvppeteer.options.BrowserOptions;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;

public interface Launcher extends Constant {
	
	Environment env = System::getenv;
	
	String PROFILE_PREFIX = "puppeteer_dev_chrome_profile-";
	
	Browser launch(LaunchOptions options);
	
	String defaultArgs(ChromeArgOptions options, List<String> chromeArguments);
	
	String resolveExecutablePath(String chromeExecutable);
	
	Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, ConnectionTransport transport);

	String executablePath();
}
