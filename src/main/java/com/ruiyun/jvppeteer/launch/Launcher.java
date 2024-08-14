package com.ruiyun.jvppeteer.launch;

import com.ruiyun.jvppeteer.core.Environment;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.options.BrowserConnectOptions;
import com.ruiyun.jvppeteer.options.BrowserLaunchArgumentOptions;
import com.ruiyun.jvppeteer.options.ConnectOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;

import java.io.IOException;
import java.util.List;

public interface Launcher {
	
	Environment env = System::getenv;
	
	Browser launch(LaunchOptions options);

	List<String> defaultArgs(LaunchOptions options);
	
	String resolveExecutablePath(String chromeExecutable) throws IOException;
	
	Browser connect(ConnectOptions options);

	String executablePath() throws IOException;
}
