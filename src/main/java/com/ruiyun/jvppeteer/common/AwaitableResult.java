package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AwaitableResult<T> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile T result;

    public static <T> AwaitableResult<T> create() {
        return new AwaitableResult<>();
    }

    public boolean isDone() {
        return this.result != null;
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
            if (timeout == 0) {
                this.waiting();
                return true;
            } else {
                return this.latch.await(timeout, unit);
            }
        } catch (InterruptedException e) {
            throw new JvppeteerException(e);
        }
    }

    public T waitingGetResult() {
        this.waiting();
        return this.result;
    }

    public T waitingGetResult(int timeout, TimeUnit unit) {
        if (timeout == 0) {
            return this.waitingGetResult();
        } else {
            boolean result = this.waiting(timeout, unit);
            if (!result) {
                throw new TimeoutException("Waiting for Result timeout of " + timeout + " ms exceeded");
            }
            return this.result;
        }
    }


    public T get() {
        return this.result;
    }

    public void onSuccess(T result) {
        this.result = result;
        this.complete();
    }

    public void complete(T result) {
        this.result = result;
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
