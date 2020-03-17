package com.ruiyun.jvppeteer;

import java.util.List;

import com.ruiyun.jvppeteer.browser.Browser;
import com.ruiyun.jvppeteer.options.LaunchOptions;

public interface Launcher extends Constant {
	
	Environment env = System::getenv;
	
	String PROFILE_PREFIX = "puppeteer_dev_chrome_profile-";
	
	Browser launch(LaunchOptions options);
	
	String defaultArgs(LaunchOptions options, List<String> chromeArguments);
	
	String resolveExecutablePath(String chromeExecutable);
	
	Browser connect(Object object);
}
