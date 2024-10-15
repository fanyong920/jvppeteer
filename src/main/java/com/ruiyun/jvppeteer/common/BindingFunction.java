package com.ruiyun.jvppeteer.common;

import java.util.List;

@FunctionalInterface
public interface BindingFunction {
    Object bind(List<Object> args);
}