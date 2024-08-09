package com.ruiyun.jvppeteer.util;

import com.ruiyun.jvppeteer.options.DisposableStackConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;

public class AsyncDisposableStack implements AutoCloseable{
    private boolean disposed = false;

    private List<DisposableStackConsumer> stack  = new ArrayList<>();

    /**
     * Returns a value indicating whether this stack has been disposed.
     */
    public boolean getDisposed() {
        return disposed;
    }
    /**
     * Disposes each resource in the stack in the reverse order that they were added.
     */
    void dispose() {
        if (this.disposed) {
            return;
        }
        this.disposed = true;
        ListIterator<DisposableStackConsumer> consumerListIterator = this.stack.listIterator(this.stack.size());
        while (consumerListIterator.hasPrevious()) {
            DisposableStackConsumer consumer = consumerListIterator.previous();
            consumer.execute();
        }
    }
    /**
     * Adds a callback to be invoked when the stack is disposed.
     */
    public void defer(DisposableStackConsumer consumer){
        this.stack.add(consumer);
    }

    public void setDisposed(boolean disposed) {
        this.disposed = disposed;
    }

    @Override
    public void close() throws Exception {
        CompletableFuture.runAsync(this::dispose);
    }
}
