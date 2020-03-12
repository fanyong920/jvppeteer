package com.ruiyun.jvppeteer.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class StringUtil {
	
	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	public static boolean isNotEmpty(String s) {
		return !StringUtil.isEmpty(s);
	}
	
	public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
	
	public static boolean isNotBlank(String str) {
        return !StringUtil.isBlank(str);
    }
	
	
	public static int getTimestamp() {
		synchronized (StringUtil.class) {
			int nano = LocalDateTime.now().getNano();
			return nano;
		}
	}
	
	
}
