package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.TouchHandle;
import com.ruiyun.jvppeteer.api.core.Touchscreen;
import com.ruiyun.jvppeteer.bidi.entities.PointerCommonProperties;

public class BidiTouchscreen extends Touchscreen {
    private final BidiPage page;
    public BidiTouchscreen(BidiPage page) {
        super();
        this.page = page;
    }

    @Override
    public TouchHandle touchStart(double x, double y, ObjectNode origin) {
        long id = this.idGenerator.getAndIncrement();
        PointerCommonProperties properties = new PointerCommonProperties();
        properties.setWidth(1L);
        properties.setHeight(1L);
        properties.setPressure(0.5);
        properties.setAltitudeAngle(Math.PI/2);
        BidiTouchHandle touch = new BidiTouchHandle(this.page, this, String.valueOf(id), x, y, properties);
         touch.start(origin);
        this.touches.add(touch);
        return touch;
    }


}
