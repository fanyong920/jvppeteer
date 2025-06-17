package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class SetGeoLocationOverrideOptions {
    private GeolocationCoordinates coordinates;

    private GeolocationPositionError error;

    private List<String> contexts;

    private List<String> userContexts;

    public GeolocationCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(GeolocationCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public GeolocationPositionError getError() {
        return error;
    }

    public void setError(GeolocationPositionError error) {
        this.error = error;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public void setContexts(List<String> contexts) {
        this.contexts = contexts;
    }

    public List<String> getUserContexts() {
        return userContexts;
    }

    public void setUserContexts(List<String> userContexts) {
        this.userContexts = userContexts;
    }
}
