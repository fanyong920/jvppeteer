package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.TouchHandle;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.TouchPoint;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CdpTouchHandle extends TouchHandle {
    private boolean started;
    private final CdpTouchscreen touchScreen;
    private final TouchPoint touchPoint;
    private CDPSession client;
    private final CdpKeyboard keyboard;

    public CdpTouchHandle(CDPSession client, CdpTouchscreen touchScreen, CdpKeyboard keyboard, TouchPoint touchPoint) {
        this.client = client;
        this.touchScreen = touchScreen;
        this.keyboard = keyboard;
        this.touchPoint = touchPoint;
    }

    public void start() {
        if (this.started) {
            throw new JvppeteerException("Touch has already started");
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "touchStart");
        List<TouchPoint> touchPoints = new ArrayList<>();
        touchPoints.add(this.touchPoint);
        params.put("touchPoints", touchPoints);
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params);
        this.started = true;
    }

    public void updateClient(CDPSession client) {
        this.client = client;
    }

    @Override
    public void move(double x, double y) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "touchMove");
        List<TouchPoint> touchPoints = new ArrayList<>();
        this.touchPoint.setX(Math.round(x));
        this.touchPoint.setY(Math.round(y));
        touchPoints.add(this.touchPoint);
        params.put("touchPoints", touchPoints);
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params);
    }

    @Override
    public void end() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "touchEnd");
        List<TouchPoint> touchPoints = new ArrayList<>();
        touchPoints.add(this.touchPoint);
        params.put("touchPoints", touchPoints);
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params);
        this.touchScreen.removeHandle(this);
    }
}
