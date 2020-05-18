package com.ruiyun.jvppeteer.launch;

import com.ruiyun.jvppeteer.Environment;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.options.BrowserOptions;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;

import java.util.List;

public interface Launcher {
	
	Environment env = System::getenv;
	
	Browser launch(LaunchOptions options);
	
	String defaultArgs(ChromeArgOptions options, List<String> chromeArguments);
	
	String resolveExecutablePath(String chromeExecutable);
	
	Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, ConnectionTransport transport);

	String executablePath();
}
