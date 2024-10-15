package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.entities.ClickOptions;
import com.ruiyun.jvppeteer.entities.DragData;
import com.ruiyun.jvppeteer.entities.DragInterceptedEvent;
import com.ruiyun.jvppeteer.entities.MouseClickOptions;
import com.ruiyun.jvppeteer.entities.MouseMoveOptions;
import com.ruiyun.jvppeteer.entities.MouseOptions;
import com.ruiyun.jvppeteer.entities.MouseState;
import com.ruiyun.jvppeteer.entities.MouseWheelOptions;
import com.ruiyun.jvppeteer.entities.Point;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.ruiyun.jvppeteer.util.Helper.justWait;

public class Mouse {

    private CDPSession client;
    private final Keyboard keyboard;
    private MouseState state = new MouseState();
    private final List<MouseState> transactions = new ArrayList<>();

    public Mouse(CDPSession client, Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
    }

    public Transaction createTransaction() {
        MouseState transaction = new MouseState();
        this.transactions.add(transaction);
        Transaction result = new Transaction();
        result.setUpdate(
                update -> {
                    transaction.setButtons(update.getButtons());
                    transaction.setPosition(update.getPosition());
                }
        );
        result.setCommit(() -> this.state = transaction);
        result.setRollback(() -> this.transactions.remove(transaction));
        return result;
    }

    public void withTransaction(Function<Consumer<MouseState>, Object> action) {
        Transaction transaction = this.createTransaction();
        try {
            action.apply(transaction.update);
            transaction.getCommit().run();
        } catch (Exception e) {
            transaction.getRollback().run();
            throw e;
        }

    }

    public void reset() {
        MouseButtonFlag[] values = MouseButtonFlag.values();
        for (MouseButtonFlag value : values) {
            if (value == MouseButtonFlag.None) {
                continue;
            }
            if ((this.state.getButtons() & value.getValue()) != 0) {
                ClickOptions clickOptions = new ClickOptions();
                switch (value) {
                    case Left: {
                        clickOptions.setButton("left");
                        break;
                    }
                    case Right: {
                        clickOptions.setButton("right");
                        break;
                    }
                    case Middle: {
                        clickOptions.setButton("middle");
                        break;
                    }
                    case Forward: {
                        clickOptions.setButton("forward");
                        break;
                    }
                    case Back: {
                        clickOptions.setButton("back");
                        break;
                    }
                }
                this.up(clickOptions);
            }
        }
        if (this.state.getPosition().getX() != 0 || this.state.getPosition().getY() != 0) {
            this.move(0, 0);
        }
    }

    public void move(double x, double y) {
        this.move(x, y, new MouseMoveOptions());
    }

    public void move(double x, double y, MouseMoveOptions options) {
        Point from = this.state.getPosition();
        Point to = new Point(x, y);
        for (int i = 1; i <= options.getSteps(); i++) {
            double finalI = i;
            this.withTransaction(updateState -> {
                        MouseState state = new MouseState();
                        Point position = new Point(from.getX() + (to.getX() - from.getX()) * (finalI / options.getSteps()), from.getY() + (to.getY() - from.getY()) * (finalI / options.getSteps()));
                        state.setPosition(position);
                        state.setButtons(this.state.getButtons());
                        updateState.accept(state);
                        return state;
                    }
            );
            Map<String, Object> params = ParamsFactory.create();
            params.put("type", "mouseMoved");
            params.put("buttons", this.state.getButtons());
            params.put("button", getButtonFromPressedButtons(this.state.getButtons()));
            params.put("modifiers", this.keyboard.getModifiers());
            params.put("x", this.state.getPosition().getX());
            params.put("y", this.state.getPosition().getY());
            this.client.send("Input.dispatchMouseEvent", params);
        }
    }

    private String getButtonFromPressedButtons(int buttons) {
        if ((buttons & MouseButtonFlag.Left.getValue()) != 0) {
            return "left";
        } else if ((buttons & MouseButtonFlag.Right.getValue()) != 0) {
            return "right";
        } else if ((buttons & MouseButtonFlag.Middle.getValue()) != 0) {
            return "middle";
        } else if ((buttons & MouseButtonFlag.Forward.getValue()) != 0) {
            return "forward";
        } else if ((buttons & MouseButtonFlag.Back.getValue()) != 0) {
            return "back";
        }
        return "none";
    }

    public void down() {
        this.down(new MouseOptions());
    }

    public void down(MouseOptions options) {
        MouseButtonFlag flag = this.getFlag(options.getButton());
        ValidateUtil.notNull(flag, "Unsupported mouse button: " + options.getButton());
        if ((this.state.getButtons() & flag.getValue()) != 0) {
            throw new JvppeteerException("Mouse button " + options.getButton() + " is already pressed");
        }
        this.withTransaction(updateState -> {
            MouseState state = new MouseState();
            state.setButtons((this.state.getButtons() | flag.getValue()));
            state.setPosition(this.state.getPosition());
            updateState.accept(state);
            return state;
        });
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "mousePressed");
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("clickCount", options.getClickCount());
        params.put("buttons", this.state.getButtons());
        params.put("button", options.getButton());
        params.put("x", this.state.getPosition().getX());
        params.put("y", this.state.getPosition().getY());
        this.client.send("Input.dispatchMouseEvent", params);
    }

    public void up() {
        this.up(new ClickOptions());
    }

    public void up(MouseOptions options) {
        MouseButtonFlag flag = this.getFlag(options.getButton());
        ValidateUtil.notNull(flag, "Unsupported mouse button: " + options.getButton());
        if ((this.state.getButtons() & flag.getValue()) == 0) {
            throw new JvppeteerException("Mouse button " + options.getButton() + " is not pressed.");
        }
        this.withTransaction(updateState -> {
            MouseState state = new MouseState();
            state.setButtons((this.state.getButtons() & ~flag.getValue()));
            state.setPosition(this.state.getPosition());
            updateState.accept(state);
            return state;
        });
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "mouseReleased");
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("clickCount", options.getClickCount());
        params.put("buttons", this.state.getButtons());
        params.put("button", options.getButton());
        params.put("x", this.state.getPosition().getX());
        params.put("y", this.state.getPosition().getY());
        this.client.send("Input.dispatchMouseEvent", params);
    }

    public void click(double x, double y, MouseClickOptions options) {
        if (options.getCount() < 1) {
            throw new JvppeteerException("Click must occur a positive number of times.");
        }
//        List<Runnable> actions = new ArrayList<>();
        this.move(x, y);
        if (options.getClickCount() == options.getCount()) {
            for (int i = 1; i < options.getCount(); ++i) {
                MouseOptions downOptions = new MouseOptions();
                downOptions.setClickCount(i);
                downOptions.setButton(options.getButton());
                this.down(downOptions);
                this.up(downOptions);
            }
        }
        MouseOptions downOptions = new MouseOptions();
        downOptions.setClickCount(options.getClickCount());
        downOptions.setButton(options.getButton());
        this.down(downOptions);
        if (options.getDelay() > 0) {
            justWait(options.getDelay());
        }
        this.up(downOptions);
    }

    private MouseButtonFlag getFlag(String button) {
        switch (button) {
            case "left":
                return MouseButtonFlag.Left;
            case "right":
                return MouseButtonFlag.Right;
            case "middle":
                return MouseButtonFlag.Middle;
            case "forward":
                return MouseButtonFlag.Forward;
            case "back":
                return MouseButtonFlag.Back;
        }
        return null;
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
     *
     * @param options 选项
     */
    public void wheel(MouseWheelOptions options) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "mouseWheel");
        params.put("pointerType", "mouse");
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("deltaX", options.getDeltaX());
        params.put("deltaY", options.getDeltaY());
        params.put("buttons", this.state.getButtons());
        params.put("x", this.state.getPosition().getX());
        params.put("y", this.state.getPosition().getY());

        this.client.send("Input.dispatchMouseEvent", params);
    }


    public void updateClient(CDPSession newSession) {
        this.client = newSession;
    }

    public MouseState getState() {
        return state;
    }

    public DragData drag(Point start, Point target) {
        AwaitableResult<DragData> waitableResult = AwaitableResult.create();
        this.client.once(CDPSession.CDPSessionEvent.Input_dragIntercepted, (event) -> waitableResult.onSuccess(((DragInterceptedEvent) event).getData()));
        this.move(start.getX(), start.getY());
        this.down();
        this.move(target.getX(), target.getY());
        return waitableResult.waitingGetResult();
    }

    public void dragEnter(Point target, DragData data) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "dragEnter");
        params.put("data", data);
        params.put("x", target.getX());
        params.put("y", target.getY());
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchDragEvent", params);
    }

    public void dragOver(Point target, DragData data) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "dragOver");
        params.put("data", data);
        params.put("x", target.getX());
        params.put("y", target.getY());
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchDragEvent", params);
    }

    public void drop(Point target, DragData data) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", "drop");
        params.put("data", data);
        params.put("x", target.getX());
        params.put("y", target.getY());
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchDragEvent", params);
    }

    public void dragAndDrop(Point start, Point target, int delay) throws InterruptedException {
        DragData data = this.drag(start, target);
        this.dragEnter(target, data);
        this.dragOver(target, data);
        if (delay > 0) {
            Thread.sleep(delay);
        }
        this.drop(target, data);
        this.up();
    }

    public enum MouseButtonFlag {
        None(0),
        Left(1),
        Right(1 << 1),
        Middle(1 << 2),
        Back(1 << 3),
        Forward(1 << 4);

        private final int value;

        MouseButtonFlag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static class Transaction {
        Consumer<MouseState> update;
        Runnable commit;
        Runnable rollback;

        public Transaction() {
        }

        public Transaction(Consumer<MouseState> update, Runnable rollback, Runnable commit) {
            this.update = update;
            this.rollback = rollback;
            this.commit = commit;
        }

        public Consumer<MouseState> getUpdate() {
            return update;
        }

        public void setUpdate(Consumer<MouseState> update) {
            this.update = update;
        }

        public Runnable getCommit() {
            return commit;
        }

        public void setCommit(Runnable commit) {
            this.commit = commit;
        }

        public Runnable getRollback() {
            return rollback;
        }

        public void setRollback(Runnable rollback) {
            this.rollback = rollback;
        }
    }
}
