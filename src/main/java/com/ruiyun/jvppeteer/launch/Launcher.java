package com.ruiyun.jvppeteer.launch;

import com.ruiyun.jvppeteer.core.Environment;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.options.BrowserOptions;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;

import java.io.IOException;
import java.util.List;

public interface Launcher {
	
	Environment env = System::getenv;
	
	Browser launch(LaunchOptions options) throws IOException;

	List<String> defaultArgs(ChromeArgOptions options);
	
	String resolveExecutablePath(String chromeExecutable) throws IOException;
	
	Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, ConnectionTransport transport);

	String executablePath() throws IOException;
}
