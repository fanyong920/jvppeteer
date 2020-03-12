package com.ruiyun.jvppeteer;

import com.ruiyun.jvppeteer.options.LaunchOptions;

public interface Launcher extends Constant {
	
	Environment env = System::getenv;
	
	String PROFILE_PREFIX = "puppeteer_dev_chrome_profile-";
	
	Browser launch(LaunchOptions options);

}
