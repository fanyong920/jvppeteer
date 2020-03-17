package com.ruiyun.jvppeteer.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
	
	public static String createProfileDir(String prefix){
		try {
			return Files.createTempDirectory(prefix).toRealPath().toString();
		} catch (Exception e) {
			throw new RuntimeException("create temp profile dir fail:",e);
		}
		
	}
	
	public static boolean assertFile(String executablePath){
		Path path = Paths.get(executablePath);
		return Files.isRegularFile(path) && Files.isReadable(path) && Files.isExecutable(path);
	}
}
