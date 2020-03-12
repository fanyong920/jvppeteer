package com.ruiyun.jvppeteer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Constant {
	
	String[] PRODUCT_ENV = {"PUPPETEER_PRODUCT","java_config_puppeteer_product","java_package_config_puppeteer_product"};
	
	String[] EXECUTABLE_ENV = {"PUPPETEER_EXECUTABLE_PATH","java_config_puppeteer_executable_path","java_package_config_puppeteer_executable_path"};
	
	String PUPPETEER_CHROMIUM_REVISION_ENV = "PUPPETEER_CHROMIUM_REVISION";
	
	List<String> DEFAULT_ARGS = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{addAll(Arrays.asList( 
					"--disable-background-networking",
	                "--disable-background-timer-throttling",
	                "--disable-breakpad",
	                "--disable-browser-side-navigation",
	                "--disable-client-side-phishing-detection",
	                "--disable-default-apps",
	                "--disable-dev-shm-usage",
	                "--disable-extensions",
	                "--disable-features=site-per-process",
	                "--disable-hang-monitor",
	                "--disable-popup-blocking",
	                "--disable-prompt-on-repost",
	                "--disable-sync",
	                "--disable-translate",
	                "--metrics-recording-only",
	                "--no-first-run",
	                "--safebrowsing-disable-auto-update",
	                "--enable-automation",
	                "--password-store=basic",
	                "--use-mock-keychain"));}
	};
}
