package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.emulation.ScreenOrientation;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

public class EmulationManager {

    private CDPSession client;

    private boolean emulatingMobile;

    private boolean hasTouch;

    public EmulationManager(CDPSession client) {
        this.client = client;
    }

    public boolean emulateViewport(Viewport viewport) throws ExecutionException, InterruptedException {
        boolean mobile = viewport.getIsMobile() || false;
        double width = viewport.getWidth();
        double height = viewport.getHeight();
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
        boolean hasTouch = viewport.getHasTouch() || false;
        Number finalDeviceScaleFactor = deviceScaleFactor;
        CompletionService service = new ExecutorCompletionService(Helper.commonExecutor());
        service.submit(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("mobile", mobile);
            params.put("width", width);
            params.put("height", height);
            params.put("deviceScaleFactor", finalDeviceScaleFactor);
            params.put("screenOrientation", screenOrientation);
            this.client.send("Emulation.setDeviceMetricsOverride", params, true);
            return null;
        });
        service.submit(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("enabled", hasTouch);
            this.client.send("Emulation.setTouchEmulationEnabled", params, true);
            return null;
        });
        for (int i = 0; i < 2; i++) {
            service.take().get();
        }
        boolean reloadNeeded = this.emulatingMobile != mobile || this.hasTouch != hasTouch;
        this.emulatingMobile = mobile;
        this.hasTouch = hasTouch;
        return reloadNeeded;
    }
}
