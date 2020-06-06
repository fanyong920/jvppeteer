package com.ruiyun.example;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Test;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

public class LaunchExample {
	
	@Test
	public void test1() throws IOException {
		ArrayList<String> argList = new ArrayList<>();
		String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";
		LaunchOptions options = new LaunchOptionsBuilder().withArgs(argList).withHeadless(false).withPipe(true).withExecutablePath(path).build();
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
