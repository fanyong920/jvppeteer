package com.ruiyun.jvppeteer.util;

import java.util.Collection;

/**
 * 验证类工具
 */
public class ValidateUtil {

	/**
	 * 集合是否为空
	 * @param c 集合
	 * @return 结果
	 */
	public static boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}

	/**
	 * 集合是否不为空
	 * @param c 集合
	 * @return 结果
	 */
	public static boolean isNotEmpty(Collection<?> c) {
		return !ValidateUtil.isEmpty(c);
	}

	/**
	 * 断言参数是否
	 * @param condition 断言失败是false 会抛异常
	 * @param errorText 异常信息提示
	 */
	public static void assertArg(boolean condition, String errorText) {
		if (!condition)
			throw new IllegalArgumentException(errorText);
	}

}
