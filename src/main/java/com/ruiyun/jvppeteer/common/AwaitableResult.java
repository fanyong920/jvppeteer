package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.exception.JvppeteerException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AwaitableResult<T> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private T response;

    public static <T> AwaitableResult<T> create() {
        return new AwaitableResult<>();
    }

    public boolean isDone() {
        return this.response != null;
    }

    public void waiting() {
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            throw new JvppeteerException(e);
        }
    }

    public boolean waiting(int timeout, TimeUnit unit) {
        try {
            return this.latch.await(timeout, unit);
        } catch (InterruptedException e) {
            throw new JvppeteerException(e);
        }
    }

    public T waitingGetResult() {
        this.waiting();
        return this.response;
    }

    public T waitingGetResult(int timeout, TimeUnit unit) {
        if (timeout == 0) {
            return this.waitingGetResult();
        } else {
            this.waiting(timeout, unit);
            return this.response;
        }
    }


    public T get() {
        return this.response;
    }

    public void onSuccess(T result) {
        this.response = result;
        this.complete();
    }

    public void complete(T result) {
        this.response = result;
        if (this.latch.getCount() > 0) {
            this.latch.countDown();
        }
    }

    public void complete() {
        if (this.latch.getCount() > 0) {
            this.latch.countDown();
        }
    }
}
