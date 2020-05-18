package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class Mouse {

    private CDPSession client;

    private Keyboard keyboard;

    private int x;

    private int y;

    private String button;

    public Mouse(CDPSession client, Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
        this.x = 0;
        this.y = 0;
        /* @type {'none'|'left'|'right'|'middle'} */
        this.button = "none";
    }

    public void move(int x, int y) {
        this.move(x,y,1);
    }

    public void move(int x, int y, int steps) {
        if (steps == 0) {
            steps = 1;
        }

        int fromX = this.x, fromY = this.y;
        this.x = x;
        this.y = y;
        Map<String, Object> params = new HashMap<>();

        for (int i = 1; i <= steps; i++) {
            params.clear();
            params.put("type", "mouseMoved");
            params.put("button", this.button);
            params.put("x", fromX + (this.x - fromX) * (i / steps));
            params.put("y", fromY + (this.y - fromY) * (i / steps));
            params.put("modifiers", this.keyboard.getModifiers());
            this.client.send("Input.dispatchMouseEvent", params, true);
        }
    }

    public void click(int x, int y, ClickOptions options) throws InterruptedException {
        if (options.getDelay() != 0) {
            this.move(x, y, 0);
            this.down(options);
            Thread.sleep(options.getDelay());
            this.up(options);
        } else {
            this.move(x, y, 0);
            this.down(options);
            this.up(options);
        }
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
}
