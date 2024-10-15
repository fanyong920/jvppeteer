package com.ruiyun.jvppeteer.launch;

import com.ruiyun.jvppeteer.common.Environment;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.entities.ConnectOptions;
import com.ruiyun.jvppeteer.entities.LaunchOptions;

import java.io.IOException;
import java.util.List;

public interface Launcher {
	
	Environment env = System::getenv;
	
	Browser launch(LaunchOptions options) throws IOException;

	List<String> defaultArgs(LaunchOptions options);
	
	String lookForExecutablePath(String chromeExecutable,String preferredRevision) throws IOException;
	
	Browser connect(ConnectOptions options) throws IOException, InterruptedException;

	String executablePath();
}
