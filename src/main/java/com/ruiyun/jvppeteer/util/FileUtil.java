package com.ruiyun.jvppeteer.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
	
	public static String createProfileDir(String prefix){
		try {
			return Files.createTempDirectory(prefix).toRealPath().toString();
		} catch (Exception e) {
			throw new RuntimeException("create temp profile dir fail:",e);
		}
		
	}
	
	public static boolean assertDir(String executablePath){
		
		return	Files.exists(Paths.get(executablePath));
		
	}
}
