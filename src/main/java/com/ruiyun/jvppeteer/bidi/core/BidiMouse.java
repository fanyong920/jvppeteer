package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.Mouse;
import com.ruiyun.jvppeteer.bidi.entities.InputId;
import com.ruiyun.jvppeteer.bidi.entities.SourceActions;
import com.ruiyun.jvppeteer.bidi.entities.SourceActionsType;
import com.ruiyun.jvppeteer.cdp.entities.DragData;
import com.ruiyun.jvppeteer.cdp.entities.MouseClickOptions;
import com.ruiyun.jvppeteer.cdp.entities.MouseMoveOptions;
import com.ruiyun.jvppeteer.cdp.entities.MouseOptions;
import com.ruiyun.jvppeteer.cdp.entities.MouseWheelOptions;
import com.ruiyun.jvppeteer.cdp.entities.Point;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BidiMouse extends Mouse {
    private final BidiPage page;
    private Point lastMovePoint = new Point(0, 0);

    public BidiMouse(BidiPage page) {
        super();
        this.page = page;
    }

    @Override
    public void reset() {
        this.lastMovePoint = new Point(0, 0);
        this.page.mainFrame().browsingContext.releaseActions();
    }

    @Override
    public void move(double x, double y, MouseMoveOptions options) {
        if (Objects.isNull(options.getSteps())) {
            options.setSteps(0);
        }
        Point from = this.lastMovePoint;
        Point to = new Point(Math.round(x), Math.round(y));
        List<SourceActions> actions = new ArrayList<>();
        for (int i = 1; i < options.getSteps(); i++) {
            SourceActions pointerMoveActions = new SourceActions();
            pointerMoveActions.setType(SourceActionsType.PointerMove);
            pointerMoveActions.setX(Double.valueOf(from.getX() + (to.getX() - from.getX()) * ((double) i / options.getSteps())).intValue());
            pointerMoveActions.setY(Double.valueOf(from.getY() + (to.getY() - from.getY()) * ((double) i / options.getSteps())).intValue());
            pointerMoveActions.setOrigin(options.getOrigin());
            actions.add(pointerMoveActions);
        }
        SourceActions pointerMoveActions = new SourceActions();
        pointerMoveActions.setType(SourceActionsType.PointerMove);
        pointerMoveActions.setX((int) to.getX());
        pointerMoveActions.setY((int) to.getY());
        pointerMoveActions.setOrigin(options.getOrigin());
        actions.add(pointerMoveActions);
        this.lastMovePoint = to;

        SourceActions sourceActions = new SourceActions();
        sourceActions.setActions(actions);
        sourceActions.setId(InputId.Mouse.getId());
        sourceActions.setType(SourceActionsType.Pointer);
        this.page.mainFrame().browsingContext.performActions(Collections.singletonList(sourceActions));

    }

    @Override
    public void down(MouseOptions options) {
        List<SourceActions> actions = new ArrayList<>();
        SourceActions pointerUpActions = new SourceActions();
        pointerUpActions.setType(SourceActionsType.PointerDown);
        pointerUpActions.setButton(getBidiButton(StringUtil.isEmpty(options.getButton()) ? "left" : options.getButton()));
        actions.add(pointerUpActions);
        SourceActions pointerActions = new SourceActions();
        pointerActions.setType(SourceActionsType.Pointer);
        pointerActions.setId(InputId.Mouse.getId());
        pointerActions.setActions(actions);
        this.page.mainFrame().browsingContext.performActions(Collections.singletonList(pointerActions));
    }

    @Override
    public void up(MouseOptions options) {
        List<SourceActions> actions = new ArrayList<>();
        SourceActions pointerUpActions = new SourceActions();
        pointerUpActions.setType(SourceActionsType.PointerUp);
        pointerUpActions.setButton(getBidiButton(StringUtil.isEmpty(options.getButton()) ? "left" : options.getButton()));
        actions.add(pointerUpActions);
        SourceActions pointerActions = new SourceActions();
        pointerActions.setType(SourceActionsType.Pointer);
        pointerActions.setId(InputId.Mouse.getId());
        pointerActions.setActions(actions);
        this.page.mainFrame().browsingContext.performActions(Collections.singletonList(pointerActions));
    }

    @Override
    public void click(double x, double y, MouseClickOptions options) {
        List<SourceActions> actions = new ArrayList<>();
        SourceActions pointerMoveActions = new SourceActions();
        pointerMoveActions.setType(SourceActionsType.PointerMove);
        pointerMoveActions.setX((int) Math.round(x));
        pointerMoveActions.setY((int) Math.round(y));
        pointerMoveActions.setOrigin(options.getOrigin());
        actions.add(pointerMoveActions);

        SourceActions pointerDownAction = new SourceActions();
        pointerDownAction.setType(SourceActionsType.PointerDown);
        pointerDownAction.setButton(getBidiButton(StringUtil.isEmpty(options.getButton()) ? "left" : options.getButton()));
        SourceActions pointerUpAction = new SourceActions();
        pointerUpAction.setType(SourceActionsType.PointerUp);
        pointerUpAction.setButton(pointerDownAction.getButton());
        for (int i = 1; i < options.getCount(); i++) {
            actions.add(pointerDownAction);
            actions.add(pointerUpAction);
        }
        actions.add(pointerDownAction);
        if (options.getDelay() > 0) {
            SourceActions delayAction = new SourceActions();
            delayAction.setType(SourceActionsType.Pause);
            delayAction.setDuration((long) options.getDelay());
            actions.add(delayAction);
        }
        actions.add(pointerUpAction);

        SourceActions sourceActions = new SourceActions();
        sourceActions.setActions(actions);
        sourceActions.setId(InputId.Mouse.getId());
        sourceActions.setType(SourceActionsType.Pointer);
        this.page.mainFrame().browsingContext.performActions(Collections.singletonList(sourceActions));
    }

    @Override
    public void wheel(MouseWheelOptions options) {
        SourceActions sourceActions = new SourceActions();
        sourceActions.setType(SourceActionsType.Wheel);
        sourceActions.setId(InputId.Wheel.getId());
        List<SourceActions> actions = new ArrayList<>();
        SourceActions actions1 = new SourceActions();
        actions1.setType(SourceActionsType.Scroll);
        if (Objects.nonNull(this.lastMovePoint)) {
            actions1.setX((int) this.lastMovePoint.getX());
            actions1.setY((int) this.lastMovePoint.getY());
        }
        actions1.setDeltaX((int) options.getDeltaX());
        actions1.setDeltaY((int) options.getDeltaY());
        actions.add(actions1);
        sourceActions.setActions(actions);
        this.page.mainFrame().browsingContext.performActions(Collections.singletonList(sourceActions));
    }

    private Long getBidiButton(String button) {
        switch (button) {
            case "left":
                return 0L;
            case "middle":
                return 1L;
            case "right":
                return 2L;
            case "back":
                return 3L;
            case "forward":
                return 4L;
            default:
                throw new IllegalArgumentException("unknown button " + button);
        }
    }

    @Override
    public DragData drag(Point start, Point target) {
       throw new UnsupportedOperationException();
    }

    @Override
    public void dragEnter(Point target, DragData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dragOver(Point target, DragData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drop(Point target, DragData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dragAndDrop(Point start, Point target, int delay) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
