package com.ruiyun.jvppeteer.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)  // 注解将在运行时保留
@Target(ElementType.METHOD)//引用于方法上
public @interface BindIsolatedHandle {
}
