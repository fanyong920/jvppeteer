package com.ruiyun.jvppeteer.protocol.promise;

@FunctionalInterface
public interface Promise<T> {

    T apply(Object...args);

}
