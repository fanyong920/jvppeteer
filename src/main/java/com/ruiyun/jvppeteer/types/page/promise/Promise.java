package com.ruiyun.jvppeteer.types.page.promise;

@FunctionalInterface
public interface Promise<T> {

    T apply(Object...args);

}
