package com.ruiyun.jvppeteer.util;

import java.util.Collection;

/**
 * 验证类工具
 */
public class ValidateUtil {
	
	public static final boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}
	
	public static final boolean isNotEmpty(Collection<?> c) {
		return !ValidateUtil.isEmpty(c);
	}

	public static final void notNull(Object object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
	}

	public static final void assertBoolean(boolean condition, String errorText) {
		if (!condition)
			throw new RuntimeException(errorText);
	}

}
