package com.ruiyun.jvppeteer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;

public interface Constant {
	
	String[] PRODUCT_ENV = {"PUPPETEER_PRODUCT","java_config_puppeteer_product","java_package_config_puppeteer_product"};
	
	String[] EXECUTABLE_ENV = {"PUPPETEER_EXECUTABLE_PATH","java_config_puppeteer_executable_path","java_package_config_puppeteer_executable_path"};
	
	String PUPPETEER_CHROMIUM_REVISION_ENV = "PUPPETEER_CHROMIUM_REVISION";
	
	long DEFAULT_PAYLOAD  = 256 * 1024 * 1024;
	
	String INCOMING_BUFFER_SIZE_PROPERTY = "org.glassfish.tyrus.incomingBufferSize";
	
	String[] PROBABLE_CHROME_EXECUTABLE_PATH =
		      new String[] {
		        "/usr/bin/chromium",
		        "/usr/bin/chromium-browser",
		        "/usr/bin/google-chrome-stable",
		        "/usr/bin/google-chrome",
		        "/Applications/Chromium.app/Contents/MacOS/Chromium",
		        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
		        "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary",
		        "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe"
		      };
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
	
	Map<String,WebSocketTransport> WS_HASH_MAP = new ConcurrentHashMap<>();
	
	public static final ObjectMapper OBJECTMAPPER = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).setSerializationInclusion(Include.NON_NULL);
	
	String RECV_MESSAGE_METHOD_PROPERTY = "method";
	String RECV_MESSAGE_PARAMS_PROPERTY = "params";
	String RECV_MESSAGE_ID_PROPERTY = "id";
	String RECV_MESSAGE_TARGETINFO_PROPERTY = "targetInfo";
	String RECV_MESSAGE_TYPE_PROPERTY = "type";
}
