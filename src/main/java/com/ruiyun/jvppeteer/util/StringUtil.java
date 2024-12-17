package com.ruiyun.jvppeteer.util;

public class StringUtil {

	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
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
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

	public static boolean isNotBlank(String str) {
        return !StringUtil.isBlank(str);
    }

}
