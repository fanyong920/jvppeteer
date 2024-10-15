package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.TouchPoint;
import com.ruiyun.jvppeteer.transport.CDPSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Touchscreen {

    private CDPSession client;

    private final Keyboard keyboard;

    public Touchscreen(CDPSession client, Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
    }

    /**
     * 调度 touchstart 和 touchend 事件。
     *
     * @param x tap的水平位置。
     * @param y tap的垂直位置。
     */
    public void tap(double x, double y) {
        this.touchStart(x, y);
        this.touchEnd();
    }

    /**
     * 调度 touchstart 事件。
     *
     * @param x tap的水平位置。
     * @param y tap的垂直位置。
     */
    public void touchStart(double x, double y) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "touchStart");
        List<TouchPoint> touchPoints = new ArrayList<>();
        TouchPoint touchPoint = new TouchPoint();
        touchPoint.setX(Math.round(x));
        touchPoint.setY(Math.round(y));
        touchPoint.setRadiusX(0.5);
        touchPoint.setRadiusY(0.5);
        touchPoint.setForce(0.5);
        touchPoints.add(touchPoint);
        params.put("touchPoints", touchPoints);
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params);
    }

    /**
     * 调度 touchMove 事件。
     *
     * @param x 移动的水平位置。
     * @param y 移动的垂直位置。
     */
    public void touchMove(double x, double y) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "touchMove");
        List<TouchPoint> touchPoints = new ArrayList<>();
        TouchPoint touchPoint = new TouchPoint();
        touchPoint.setX(Math.round(x));
        touchPoint.setY(Math.round(y));
        touchPoint.setRadiusX(0.5);
        touchPoint.setRadiusY(0.5);
        touchPoint.setForce(0.5);
        touchPoints.add(touchPoint);
        params.put("touchPoints", touchPoints);
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params);
    }

    /**
     * 调度 touchend 事件。
     */
    public void touchEnd() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "touchEnd");
        List<TouchPoint> touchPoints = new ArrayList<>();
        params.put("touchPoints", touchPoints);
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params);
    }

    public void updateClient(CDPSession client) {
        this.client = client;
    }

}
