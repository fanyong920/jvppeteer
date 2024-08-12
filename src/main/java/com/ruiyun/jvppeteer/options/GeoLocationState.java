package com.ruiyun.jvppeteer.options;

public class GeoLocationState extends ActiveProperty {

    public GeolocationOptions geoLocation;

    public GeoLocationState(boolean active, GeolocationOptions geoLocation) {
        super(active);
        this.geoLocation = geoLocation;
    }

    public GeolocationOptions getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeolocationOptions geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public String toString() {
        return "GeoLocationState{" +
                "geoLocation=" + geoLocation +
                ", active=" + active +
                '}';
    }
}
