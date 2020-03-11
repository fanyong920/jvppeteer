package com.ruiyun.jvppeteer;

public interface Constant {
	
	Environment env = System::getenv;
	
	String[] PRODUCT_ENV = {"PUPPETEER_PRODUCT","java_config_puppeteer_product","java_package_config_puppeteer_product"};
	
	String[] DEFAULT_ARGS = {
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
	                "--use-mock-keychain"
			};
}
