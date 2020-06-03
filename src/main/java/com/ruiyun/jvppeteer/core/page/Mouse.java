package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

public class Mouse {

    private static final int MULTI_THREAD_THRESHOLD = 10;

    private final CDPSession client;

    private final Keyboard keyboard;

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

    public void move(int x, int y) throws ExecutionException, InterruptedException {
        this.move(x, y, 1);
    }

    public void move(int x, int y, int steps) throws ExecutionException, InterruptedException {
        if (steps == 0) {
            steps = 1;
        }
        int fromX = this.x, fromY = this.y;
        this.x = x;
        this.y = y;
        Map<String, Object> params = new HashMap<>();

        if (steps >= MULTI_THREAD_THRESHOLD) {
            List<Future<Boolean>> futures = new ArrayList<>(steps);
            CompletionService<Boolean> completionService = new ExecutorCompletionService<>(Helper.commonExecutor());
            for (int i = 1; i <= steps; i++) {
                int finalSteps = steps;
                int finalI = i;
                futures.add(completionService.submit(() -> {
                    stepRun(finalSteps, fromX, fromY, params, finalI);
                    return true;
                }));
            }
            for (Future<Boolean> future : futures) {
                future.get();
            }
        } else {
            for (int i = 1; i <= steps; i++) {
                stepRun(steps, fromX, fromY, params, i);
            }
        }
    }

    private void stepRun(int steps, int fromX, int fromY, Map<String, Object> params, int i) {
        params.clear();
        params.put("type", "mouseMoved");
        params.put("button", this.button);
        params.put("x", fromX + (this.x - fromX) * (i / steps));
        params.put("y", fromY + (this.y - fromY) * (i / steps));
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    public void click(int x, int y, ClickOptions options) throws InterruptedException, ExecutionException {
        if (options.getDelay() != 0) {
            this.move(x, y, 0);
            this.down(options);
            if(options.getDelay() > 0){
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
}
