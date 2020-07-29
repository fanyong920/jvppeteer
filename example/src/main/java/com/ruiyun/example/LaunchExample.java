package com.ruiyun.example;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import org.junit.Test;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

public class LaunchExample {
	
	@Test
	public void test1() throws Exception {
		ArrayList<String> argList = new ArrayList<>();

		//自动下载，第一次下载后不会再下载
		BrowserFetcher.downloadIfNotExist(null);

		//.withPipe(true) 不可用，切记不要加上这个参数
		LaunchOptions options = new LaunchOptionsBuilder().withArgs(argList).withHeadless(false).build();
		argList.add("--no-sandbox");
		argList.add("--disable-setuid-sandbox");
		Puppeteer.launch(options);
	}

	public static void main(String[] args) {
		Method[] declaredMethods = LaunchExample.class.getDeclaredMethods();
		for (Method declaredMethod : declaredMethods) {

			System.out.println(declaredMethod.toGenericString());
		}

	}
	
}
