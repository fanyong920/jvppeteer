package com.ruiyun.jvppeteer;

@FunctionalInterface
public interface Environment {
	
	String getEnv(String name);
	
}
