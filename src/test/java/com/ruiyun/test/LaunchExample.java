package com.ruiyun.test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Test;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;

public class LaunchExample {
	
	@Test
	public void test1() {
		ArrayList<String> arrayList = new ArrayList<>();
		LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
		arrayList.add("--no-sandbox");
		arrayList.add("--disable-setuid-sandbox");
		Puppeteer.launch(options);
	}

	public static void main(String[] args) {
		Method[] declaredMethods = LaunchExample.class.getDeclaredMethods();
		for (Method declaredMethod : declaredMethods) {

			System.out.println(declaredMethod.toGenericString());
		}

	}
	
}
