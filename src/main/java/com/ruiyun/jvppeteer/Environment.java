package com.ruiyun.jvppeteer;

/**
 * 环境变量的接口:可以使用System:getEnv来实现
 */
@FunctionalInterface
public interface Environment {

	/**
	 * 根据name获取环境变量中的值
	 * @param name
	 * @return
	 */
	String getEnv(String name);
	
}
