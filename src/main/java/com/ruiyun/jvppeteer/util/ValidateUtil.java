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
	public static final boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}

	/**
	 * 集合是否不为空
	 * @param c 集合
	 * @return 结果
	 */
	public static final boolean isNotEmpty(Collection<?> c) {
		return !ValidateUtil.isEmpty(c);
	}

	/**
	 * 判断
	 * @param object 要判空的对象
	 * @param message 提示信息
	 */
	public static final void notNull(Object object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
	}

	/**
	 * 断言参数是否
	 * @param condition 断言失败是false 会抛异常
	 * @param errorText 异常信息提示
	 */
	public static final void assertArg(boolean condition, String errorText) {
		if (!condition)
			throw new IllegalArgumentException(errorText);
	}

}
