package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.TouchHandle;
import com.ruiyun.jvppeteer.bidi.entities.InputId;
import com.ruiyun.jvppeteer.bidi.entities.PointerCommonProperties;
import com.ruiyun.jvppeteer.bidi.entities.PointerParameters;
import com.ruiyun.jvppeteer.bidi.entities.PointerType;
import com.ruiyun.jvppeteer.bidi.entities.SourceActions;
import com.ruiyun.jvppeteer.bidi.entities.SourceActionsType;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BidiTouchHandle extends TouchHandle {
    private boolean started;
    private final double x;
    private final double y;
    private final String bidiId;
    private final BidiPage page;
    private final BidiTouchscreen touchScreen;
    private final PointerCommonProperties properties;

    public BidiTouchHandle(BidiPage page, BidiTouchscreen touchScreen, String id, double x, double y, PointerCommonProperties properties) {
        this.page = page;
        this.touchScreen = touchScreen;
        this.x = Math.round(x);
        this.y = Math.round(y);
        this.properties = properties;
        this.bidiId = InputId.Finger.getId() +"_"+ id;
    }
    public void start(ObjectNode origin){
        if(started){
           throw new JvppeteerException("Touch has already started");
        }
        SourceActions sourceActions = new SourceActions();
        sourceActions.setType(SourceActionsType.Pointer);
        sourceActions.setId(this.bidiId);
        sourceActions.setParameters(new PointerParameters(PointerType.Touch));
        List<SourceActions> actions = new ArrayList<>();
        SourceActions pointerMoveActions = new SourceActions();
        pointerMoveActions.setType(SourceActionsType.PointerMove);
        pointerMoveActions.setX((int) this.x);
        pointerMoveActions.setY((int) this.y);
        pointerMoveActions.setOrigin(origin);
        actions.add(pointerMoveActions);

        SourceActions pointerDownActions = new SourceActions();
        pointerDownActions.setType(SourceActionsType.PointerDown);
        pointerDownActions.setButton(0L);
        pointerDownActions.setWidth(this.properties.getWidth());
        pointerDownActions.setHeight(this.properties.getHeight());
        pointerDownActions.setPressure(this.properties.getPressure());
        pointerDownActions.setAltitudeAngle(this.properties.getAltitudeAngle());
        pointerDownActions.setAzimuthAngle(this.properties.getAzimuthAngle());
        pointerDownActions.setTwist(this.properties.getTwist());
        pointerDownActions.setTangentialPressure(this.properties.getTangentialPressure());
        actions.add(pointerDownActions);

        sourceActions.setActions(actions);
        this.page.mainFrame().browsingContext.performActions(Collections.singletonList(sourceActions));
        this.started = true;
    }
    @Override
    public void move(double x, double y) {
        double newX = Math.round(x);
        double newY = Math.round(y);
        SourceActions sourceActions = new SourceActions();
        sourceActions.setType(SourceActionsType.Pointer);
        sourceActions.setId(this.bidiId);
        sourceActions.setParameters(new PointerParameters(PointerType.Touch));

        List<SourceActions> actions = new ArrayList<>();
        SourceActions pointerMoveActions = new SourceActions();
        pointerMoveActions.setType(SourceActionsType.PointerMove);
        pointerMoveActions.setX((int) newX);
        pointerMoveActions.setY((int) newY);
        pointerMoveActions.setWidth(this.properties.getWidth());
        pointerMoveActions.setHeight(this.properties.getHeight());
        pointerMoveActions.setPressure(this.properties.getPressure());
        pointerMoveActions.setAltitudeAngle(this.properties.getAltitudeAngle());
        pointerMoveActions.setAzimuthAngle(this.properties.getAzimuthAngle());
        pointerMoveActions.setTwist(this.properties.getTwist());
        pointerMoveActions.setTangentialPressure(this.properties.getTangentialPressure());
        actions.add(pointerMoveActions);
        sourceActions.setActions(actions);

        this.page.mainFrame().browsingContext.performActions(Collections.singletonList(sourceActions));
    }

    @Override
    public void end() {
        SourceActions sourceActions = new SourceActions();
        sourceActions.setType(SourceActionsType.Pointer);
        sourceActions.setId(this.bidiId);
        sourceActions.setParameters(new PointerParameters(PointerType.Touch));

        List<SourceActions> actions = new ArrayList<>();
        SourceActions actions1 = new SourceActions();
        actions1.setType(SourceActionsType.PointerUp);
        actions1.setButton(0L);
        actions.add(actions1);
        sourceActions.setActions(actions);

        this.page.mainFrame().browsingContext.performActions(Collections.singletonList(sourceActions));
        this.touchScreen.removeHandle(this);
    }
}
