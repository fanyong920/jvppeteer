package com.ruiyun.test;

import java.util.ArrayList;

import org.junit.Test;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;

public class LaunchExample {
	
	@Test
	public void test1() {
		Puppeteer puppeteer  = new Puppeteer();
		ArrayList<String> arrayList = new ArrayList<>();
		LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).build();
		arrayList.add("--no-sandbox");
		arrayList.add("--disable-setuid-sandbox");
		puppeteer.launch(options);
	}
	
}
