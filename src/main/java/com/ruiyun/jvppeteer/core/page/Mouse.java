package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Mouse {

    private static final int MULTI_THREAD_THRESHOLD = 10;

    private final CDPSession client;

    private final Keyboard keyboard;

    private double x;

    private double y;

    private String button;

    public Mouse(CDPSession client, Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
        this.x = 0;
        this.y = 0;
        /* @type {'none'|'left'|'right'|'middle'} */
        this.button = "none";
    }

    public void move(double x, double y) {
        this.move(x, y, 1);
    }

    public void move(double x, double y, int steps) {
        if (steps == 0) {
            steps = 1;
        }
        double fromX = this.x, fromY = this.y;
        this.x = x;
        this.y = y;
        for (int i = 1; i <= steps; i++) {
            stepRun(steps, fromX, fromY, i);
        }
    }

    private void stepRun(double steps, double fromX, double fromY, int i) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mouseMoved");
        params.put("button", this.button);
        BigDecimal divide = new BigDecimal(i).divide(new BigDecimal(steps), 17, BigDecimal.ROUND_HALF_UP);
        params.put("x", divide.multiply(new BigDecimal((this.x - fromX))).add(new BigDecimal(fromX)).doubleValue());
        params.put("y", divide.multiply(new BigDecimal((this.y - fromY))).add(new BigDecimal(fromY)).doubleValue());
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    public void click(int x, int y, ClickOptions options) throws InterruptedException {
        if (options.getDelay() != 0) {
            this.move(x, y, 0);
            this.down(options);
            if (options.getDelay() > 0) {
                Thread.sleep(options.getDelay());
            }
        } else {
            this.move(x, y, 0);
            this.down(options);
        }
        this.up(options);
    }

    public void up() {
        this.up(new ClickOptions());
    }

    public void up(ClickOptions options) {
        String button = "left";
        int clickCount = 1;
        this.button = "none";
        if (StringUtil.isNotEmpty(options.getButton())) {
            button = options.getButton();
        }
        if (options.getClickCount() != 0) {
            clickCount = options.getClickCount();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mouseReleased");
        params.put("button", button);
        params.put("x", this.x);
        params.put("y", this.y);
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("clickCount", clickCount);
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    public void down() {
        this.down(new ClickOptions());
    }

    public void down(ClickOptions options) {
        String button = "left";
        int clickCount = 1;
        if (StringUtil.isNotEmpty(options.getButton())) {
            button = options.getButton();
        }
        if (options.getClickCount() != 0) {
            clickCount = options.getClickCount();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mousePressed");
        params.put("button", button);
        params.put("x", this.x);
        params.put("y", this.y);
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("clickCount", clickCount);
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    public int buttonNameToButton(String buttonName) {
        if ("left".equals(buttonName))
            return 0;
        if ("middle".equals(buttonName))
            return 1;
        if ("right".equals(buttonName))
            return 2;
        throw new IllegalArgumentException("Unkown ButtonName: " + buttonName);
    }

    /**
     * 触发一个鼠标滚轮事件
     * @param deltaX
     * @param deltaY
     */
    public void wheel(double deltaX, double deltaY) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mouseWheel");
        params.put("x", this.x);
        params.put("y", this.y);
        params.put("deltaX", deltaX);
        params.put("deltaY", deltaY);
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("pointerType", "mouse");
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    /**
     * 触发一个鼠标滚轮事件
     */
    public void wheel() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mouseWheel");
        params.put("x", this.x);
        params.put("y", this.y);
        params.put("deltaX", 0.00);
        params.put("deltaY", 0.00);
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("pointerType", "mouse");
        this.client.send("Input.dispatchMouseEvent", params, true);
    }
}
