package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Touchscreen {
    protected final List<TouchHandle> touches = new ArrayList<>();
    protected final AtomicLong idGenerator = new AtomicLong(1);

    public Touchscreen() {
    }

    /**
     * 调度 touchstart 和 touchend 事件。
     *
     * @param x tap的水平位置。
     * @param y tap的垂直位置。
     */
    public void tap(double x, double y) {
        TouchHandle touch = this.touchStart(x, y, null);
        touch.end();
    }

    /**
     * 调度 touchstart 事件。
     *
     * @param x      tap的水平位置。
     * @param y      tap的垂直位置。
     * @param origin origin
     */
    public abstract TouchHandle touchStart(double x, double y, ObjectNode origin);

    /**
     * 调度 touchMove 事件。
     *
     * @param x 移动的水平位置。
     * @param y 移动的垂直位置。
     */
    public void touchMove(double x, double y) {
        TouchHandle touch = this.touches.get(0);
        if (Objects.isNull(touch)) {
            throw new NullPointerException("Must start a new Touch first");
        }
        touch.move(x, y);
    }

    /**
     * 调度 touchend 事件。
     */
    public void touchEnd() {
        TouchHandle touch = this.touches.remove(0);
        if (Objects.isNull(touch)) {
            throw new NullPointerException("Must start a new Touch first");
        }
        touch.end();
    }

    public void removeHandle(TouchHandle handle) {
        this.touches.remove(handle);
    }
}
