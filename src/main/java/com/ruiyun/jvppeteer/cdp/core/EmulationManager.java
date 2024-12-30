package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.ClientProvider;
import com.ruiyun.jvppeteer.cdp.entities.CpuThrottlingState;
import com.ruiyun.jvppeteer.cdp.entities.DefaultBackgroundColorState;
import com.ruiyun.jvppeteer.cdp.entities.EmulatedState;
import com.ruiyun.jvppeteer.cdp.entities.GeoLocationState;
import com.ruiyun.jvppeteer.cdp.entities.GeolocationOptions;
import com.ruiyun.jvppeteer.cdp.entities.IdleOverridesState;
import com.ruiyun.jvppeteer.cdp.entities.JavascriptEnabledState;
import com.ruiyun.jvppeteer.cdp.entities.MediaFeature;
import com.ruiyun.jvppeteer.cdp.entities.MediaFeaturesState;
import com.ruiyun.jvppeteer.cdp.entities.MediaTypeState;
import com.ruiyun.jvppeteer.cdp.entities.RGBA;
import com.ruiyun.jvppeteer.cdp.entities.ScreenOrientation;
import com.ruiyun.jvppeteer.cdp.entities.TimezoneState;
import com.ruiyun.jvppeteer.cdp.entities.Updater;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.cdp.entities.ViewportState;
import com.ruiyun.jvppeteer.cdp.entities.VisionDeficiency;
import com.ruiyun.jvppeteer.cdp.entities.VisionDeficiencyState;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class EmulationManager implements ClientProvider  {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmulationManager.class);
    final List<EmulatedState<?>> states = new ArrayList<>();
    final Set<CDPSession> secondaryClients = new HashSet<>();
    private volatile CDPSession client;
    private boolean emulatingMobile = false;
    private boolean hasTouch = false;
    private final EmulatedState<ViewportState> viewportState = new EmulatedState<>(new ViewportState(false, null), this, applyViewport);
    private final EmulatedState<IdleOverridesState> idleOverridesState = new EmulatedState<>(new IdleOverridesState(false), this, emulateIdleState);
    private final EmulatedState<TimezoneState> timezoneState = new EmulatedState<>(new TimezoneState(false), this, emulateTimezone);
    private final EmulatedState<VisionDeficiencyState> visionDeficiencyState = new EmulatedState<>(new VisionDeficiencyState(false), this, emulateVisionDeficiency);
    private final EmulatedState<CpuThrottlingState> cpuThrottlingState = new EmulatedState<>(new CpuThrottlingState(false), this, emulateCpuThrottling);
    private final EmulatedState<MediaFeaturesState> mediaFeaturesState = new EmulatedState<>(new MediaFeaturesState(false, null), this, emulateMediaFeatures);
    private final EmulatedState<MediaTypeState> mediaTypeState = new EmulatedState<>(new MediaTypeState(false, null), this, emulateMediaType);
    private final EmulatedState<GeoLocationState> geoLocationState = new EmulatedState<>(new GeoLocationState(false, null), this, setGeolocation);
    private final EmulatedState<DefaultBackgroundColorState> defaultBackgroundColorState = new EmulatedState<>(new DefaultBackgroundColorState(false, null), this, setDefaultBackgroundColor);
    private final EmulatedState<JavascriptEnabledState> javascriptEnabledState = new EmulatedState<>(new JavascriptEnabledState(false, true), this, setJavaScriptEnabled);

    public EmulationManager(CDPSession client) {
        this.client = client;
    }

    public void updateClient(CDPSession client) {
        this.client = client;
        this.secondaryClients.remove(client);
    }

    @Override
    public void registerState(EmulatedState<?> state) {
        this.states.add(state);
    }

    @Override
    public List<CDPSession> clients() {
        List<CDPSession> cdpSessionList = new ArrayList<>();
        cdpSessionList.add(this.client);
        cdpSessionList.addAll(this.secondaryClients);
        return cdpSessionList;
    }

    public void registerSpeculativeSession(CdpCDPSession _client){
        this.secondaryClients.add(_client);
        client.once(ConnectionEvents.CDPSession_Disconnected, event -> this.secondaryClients.remove(_client)
        );
        // We don't await here because we want to register all state changes before
        // the target is unpaused.
        this.states.forEach(EmulatedState::send);
    }

    public boolean javascriptEnabled() {
        return this.javascriptEnabledState.state.javaScriptEnabled;
    }

    public boolean emulateViewport(Viewport viewport) {
        if(viewport == null && !this.viewportState.getState().getActive()){
            return false;
        }
        if(viewport != null){
            this.viewportState.setState(new ViewportState(true,viewport));
        }else {
            this.viewportState.setState(new ViewportState(false,null));
        }
        boolean mobile = false;
        boolean hasTouch = false;
        if(viewport != null) {
            mobile = viewport.getIsMobile();
            hasTouch = viewport.getHasTouch();
        }
        boolean reloadNeeded = this.emulatingMobile != mobile || this.hasTouch != hasTouch;
        this.emulatingMobile = mobile;
        this.hasTouch = hasTouch;
        return reloadNeeded;
    }

    private static final Updater<ViewportState> applyViewport = (client, viewportState) -> {
        if (viewportState.getViewport() == null) {
            client.send("Emulation.setDeviceMetricsOverride");
            client.send("Emulation.setTouchEmulationEnabled", new HashMap<String, Object>() {{
                put("enabled", false);
            }});
            return;
        }
        Viewport viewport = viewportState.getViewport();
        boolean mobile = viewport.getIsMobile();
        int width = viewport.getWidth();
        int height = viewport.getHeight();
        double deviceScaleFactor = viewport.getDeviceScaleFactor() == null ? (double) viewport.getDeviceScaleFactor() : 1;
        ScreenOrientation screenOrientation;
        if (viewport.getIsLandscape()) {
            screenOrientation = new ScreenOrientation(90, "landscapePrimary");
        } else {
            screenOrientation = new ScreenOrientation(0, "portraitPrimary");
        }
        boolean hasTouch = viewport.getHasTouch();
        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put("mobile", mobile);
            params.put("width", width);
            params.put("height", height);
            params.put("deviceScaleFactor", deviceScaleFactor);
            params.put("screenOrientation", screenOrientation);
            client.send("Emulation.setDeviceMetricsOverride", params);
        } catch (Exception err) {
            if (err.getMessage().contains("Target does not support metrics override")) {
                LOGGER.error("jvppeteer:error", err);
            }
            throw err;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("enabled", hasTouch);
        client.send("Emulation.setTouchEmulationEnabled", params);
    };

    public void emulateIdleState(IdleOverridesState.Overrides overrides) {
        this.idleOverridesState.setState(new IdleOverridesState(true,overrides));
    }

    private static final Updater<IdleOverridesState> emulateIdleState = (client, idleStateState) ->
    {
        if (!idleStateState.getActive()) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        if (idleStateState.getOverrides() != null) {
            params.put("isUserActive", idleStateState.getOverrides().isUserActive);
            params.put("isScreenUnlocked", idleStateState.getOverrides().isScreenUnlocked);
            client.send("Emulation.setIdleOverride", params);
        } else {
            client.send("Emulation.clearIdleOverride");
        }
    };

    private static final Updater<TimezoneState> emulateTimezone = (client, timezoneState) -> {
        if (!timezoneState.getActive()) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("timezoneId", StringUtil.isEmpty(timezoneState.timezoneId) ? "" : timezoneState.timezoneId);
        try {
            client.send("Emulation.setTimezoneOverride", params);
        } catch (Exception error) {
            if (error.getMessage().contains("Invalid timezone")) {
                throw new IllegalArgumentException("Invalid timezone ID : " + timezoneState.timezoneId);
            }
            throw error;
        }
    };

    public void emulateTimezone(String timezoneId){
        this.timezoneState.setState(new TimezoneState(true,timezoneId));
    }

    private static final Updater<VisionDeficiencyState> emulateVisionDeficiency = (client, visionDeficiency) -> {
        if (!visionDeficiency.getActive()) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", visionDeficiency.visionDeficiency.getValue());
        client.send("Emulation.setEmulatedVisionDeficiency", params);
    };

    public void emulateVisionDeficiency(VisionDeficiency type){
        this.visionDeficiencyState.setState(new VisionDeficiencyState(true,type));
    }

    private static final Updater<CpuThrottlingState> emulateCpuThrottling = (client, state) -> {
        if (!state.getActive()) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("rate", state.getFactor() == null ? 1 : state.getFactor());
        client.send("Emulation.setCPUThrottlingRate", params);
    };

    public void emulateCPUThrottling(double factor){
        ValidateUtil.assertArg(factor >= 1,"Throttling rate should be greater or equal to 1");
        this.cpuThrottlingState.setState(new CpuThrottlingState(true,factor));
    }

    private static final Updater<MediaFeaturesState> emulateMediaFeatures = (client, state) -> {
        if (!state.getActive()) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("features", state.mediaFeatures);
        client.send("Emulation.setEmulatedMedia", params);
    };

    public void emulateMediaFeatures(List<MediaFeature> features) {
        if(features != null && !features.isEmpty()){
            for (MediaFeature mediaFeature : features) {
                String name = mediaFeature.getName();
                Pattern pattern = Pattern.compile("^(?:prefers-(?:color-scheme|reduced-motion)|color-gamut)$");
                ValidateUtil.assertArg(pattern.matcher(name).find(),"Unsupported media feature: " + name);
            }
        }
        this.mediaFeaturesState.setState(new MediaFeaturesState(true,features));
    }


    private static final Updater<MediaTypeState> emulateMediaType = (client, state) -> {
        if (!state.getActive()) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("media", state.getType() == null  ? "" : state.getType().getType());
        client.send("Emulation.setEmulatedMedia", params);
    };

    public void emulateMediaType(MediaType type){
        this.mediaTypeState.setState(new MediaTypeState(true,type));
    }

    private static final Updater<GeoLocationState> setGeolocation = (client, state) -> {
        if (!state.active) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        if (state.geoLocation != null) {
            params.put("longitude", state.getGeoLocation().getLongitude());
            params.put("latitude", state.getGeoLocation().getLatitude());
            params.put("accuracy", state.getGeoLocation().getAccuracy());
            client.send("Emulation.setGeolocationOverride", params);
        } else {
            client.send("Emulation.setGeolocationOverride");
        }

    };

    public  void setGeolocation(GeolocationOptions options) {
        if (options.getLongitude() < -180 || options.getLongitude() > 180) {
            throw new IllegalArgumentException("Invalid longitude " + options.getLongitude() + ": precondition -180 <= LONGITUDE <= 180 failed.");
        }
        if (options.getLatitude() < -90 || options.getLatitude() > 90) {
            throw new IllegalArgumentException("Invalid latitude " + options.getLatitude() + ": precondition -90 <= LATITUDE <= 90 failed.");
        }
        if (options.getAccuracy() < 0) {
            throw new IllegalArgumentException("Invalid accuracy " + options.getAccuracy() + ": precondition 0 <= ACCURACY failed.");
        }
        this.geoLocationState.setState(new GeoLocationState(true,new GeolocationOptions(options.getLongitude(), options.getLatitude(), options.getAccuracy())));
    }


    private static final Updater<DefaultBackgroundColorState>  setDefaultBackgroundColor = (client, state) -> {
        if (!state.getActive()) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("color", state.getColor());
        client.send("Emulation.setDefaultBackgroundColorOverride", params);
    };

    /**
     * Resets default white background
     */
    public void resetDefaultBackgroundColor() {
        this.defaultBackgroundColorState.setState(new DefaultBackgroundColorState(true,null));
    }

    /**
     * Hides default white background
     */
    public void setTransparentBackgroundColor() {
        this.defaultBackgroundColorState.setState(new DefaultBackgroundColorState(true,new RGBA(0,0,0,0)));
    }

    private static final Updater<JavascriptEnabledState>  setJavaScriptEnabled = (client, state) -> {
        if (!state.active) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("value", !state.getJavaScriptEnabled());
        client.send("Emulation.setScriptExecutionDisabled", params);
    };

    public void setJavaScriptEnabled(boolean enabled){
        this.javascriptEnabledState.setState(new JavascriptEnabledState(true,enabled));
    }
}
