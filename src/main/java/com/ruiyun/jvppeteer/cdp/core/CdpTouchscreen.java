package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.TouchHandle;
import com.ruiyun.jvppeteer.api.core.Touchscreen;
import com.ruiyun.jvppeteer.cdp.entities.TouchPoint;

public class CdpTouchscreen extends Touchscreen {
    private CDPSession client;
    private final CdpKeyboard keyboard;

    public CdpTouchscreen(CDPSession client, CdpKeyboard keyboard) {
        super();
        this.client = client;
        this.keyboard = keyboard;
    }

    public void updateClient(CDPSession client) {
        this.client = client;
        this.touches.forEach(t -> t.updateClient(client));
    }

    /**
     * 调度 touchstart 事件。
     *
     * @param x tap的水平位置。
     * @param y tap的垂直位置。
     */
    @Override
    public TouchHandle touchStart(double x, double y, ObjectNode origin) {
        long id = this.idGenerator.getAndIncrement();
        TouchPoint touchPoint = new TouchPoint();
        touchPoint.setX(Math.round(x));
        touchPoint.setY(Math.round(y));
        touchPoint.setRadiusX(0.5);
        touchPoint.setRadiusY(0.5);
        touchPoint.setForce(0.5);
        touchPoint.setId(id);
        CdpTouchHandle touch = new CdpTouchHandle(this.client, this, this.keyboard, touchPoint);
        touch.start();
        this.touches.add(touch);
        return touch;
    }


}
