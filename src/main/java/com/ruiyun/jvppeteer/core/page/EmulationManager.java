package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.emulation.ScreenOrientation;
import com.ruiyun.jvppeteer.transport.CDPSession;

import java.util.HashMap;
import java.util.Map;

public class EmulationManager {

    private CDPSession client;

    private boolean emulatingMobile;

    private boolean hasTouch;

    public EmulationManager(CDPSession client) {
        this.client = client;
    }

    public boolean emulateViewport(Viewport viewport) {
        boolean mobile = viewport.getIsMobile();
        int width = viewport.getWidth();
        int height = viewport.getHeight();
        Number deviceScaleFactor = 1;
        if (viewport.getDeviceScaleFactor() != null && viewport.getDeviceScaleFactor().intValue() != 0) {
            deviceScaleFactor = viewport.getDeviceScaleFactor();
        }

        ScreenOrientation screenOrientation = new ScreenOrientation();
        if (viewport.getIsLandscape()) {
            screenOrientation.setAngle(90);
            screenOrientation.setType("'landscapePrimary");
        } else {
            screenOrientation.setAngle(0);
            screenOrientation.setType("portraitPrimary");
        }

        boolean hasTouch = viewport.getHasTouch();

        Map<String, Object> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("width", width);
        params.put("height", height);
        params.put("deviceScaleFactor", deviceScaleFactor);
        params.put("screenOrientation", screenOrientation);
        this.client.send("Emulation.setDeviceMetricsOverride", params);
        params.clear();
        params.put("enabled", hasTouch);
        this.client.send("Emulation.setTouchEmulationEnabled", params);
        boolean reloadNeeded = this.emulatingMobile != mobile || this.hasTouch != hasTouch;
        this.emulatingMobile = mobile;
        this.hasTouch = hasTouch;
        return reloadNeeded;
    }
}
