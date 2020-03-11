package com.ruiyun.jvppeteer;

import com.ruiyun.jvppeteer.options.LaunchOptions;

public interface Launcher extends Constant {

	Browser launch(LaunchOptions options);

	
}
