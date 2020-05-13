package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.transport.CDPSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Touchscreen {

    private CDPSession client;

    private Keyboard keyboard;

    public Touchscreen(CDPSession client, Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
    }

    public void tap(int x, int y) {
        // Touches appear to be lost during the first frame after navigation.
        // This waits a frame before sending the tap.
        // @see https://crbug.com/613219
        Map<String, Object> params = new HashMap<>();
        params.put("expression", "new Promise(x => requestAnimationFrame(() => requestAnimationFrame(x)))");
        params.put("awaitPromise", true);
        this.client.send("Runtime.evaluate", params, true);

        class TouchPoint {

            private long x;

            private long y;

            public TouchPoint(long x, long y) {
                this.x = Math.round(x);
                this.y = Math.round(y);
            }

            public long getX() {
                return x;
            }

            public void setX(long x) {
                this.x = x;
            }

            public long getY() {
                return y;
            }

            public void setY(long y) {
                this.y = y;
            }
        }

        TouchPoint touchPoint = new TouchPoint(x, y);
        List<TouchPoint> touchPoints = new ArrayList<>();
        touchPoints.add(touchPoint);
        params.clear();
        params.put("type", "touchStart");
        params.put("touchPoints", touchPoints);
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params, true);

        params.clear();
        params.put("type", "touchEnd");
        params.put("touchPoints", new ArrayList<>());
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchTouchEvent", params, true);
    }
}
