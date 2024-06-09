package com.ruiyun.example;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import org.junit.Test;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

public class LaunchExample {
	
	@Test
	public void test1() throws Exception {
		//自动下载722234版本的浏览器，第一次下载后不会再下载,内置有下载链接，下载链接可能失效。
		BrowserFetcher.downloadIfNotExist();
		LaunchOptions launchOptions = new LaunchOptionsBuilder().withIgnoreDefaultArgs(Collections.singletonList("--enable-automation")).withHeadless(false).build();
		Browser browser = Puppeteer.launch(launchOptions);
		Page page = browser.newPage();
		page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
		// 做一些其他操作
		browser.close();
	}

	public static void main(String[] args) {
		Method[] declaredMethods = LaunchExample.class.getDeclaredMethods();
		for (Method declaredMethod : declaredMethods) {

			System.out.println(declaredMethod.toGenericString());
		}

	}
	
}
